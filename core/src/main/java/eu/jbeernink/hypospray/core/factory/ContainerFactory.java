package eu.jbeernink.hypospray.core.factory;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Singleton;

import eu.jbeernink.hypospray.core.Container;
import eu.jbeernink.hypospray.core.ContainerProvider;
import eu.jbeernink.hypospray.core.ProxyContextProducer;
import eu.jbeernink.hypospray.core.annotation.ProxyCreationContext;
import eu.jbeernink.hypospray.core.context.dependent.DependentScopeContext;
import eu.jbeernink.hypospray.core.context.singleton.SingletonScopeContext;
import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.discovery.ClassDiscoverer;
import eu.jbeernink.hypospray.core.discovery.MetaAnnotationRegistry;
import eu.jbeernink.hypospray.core.event.DummyEventInstance;
import eu.jbeernink.hypospray.core.extension.BuildCompatibleExtensionManager;
import eu.jbeernink.hypospray.core.inject.InjectionPointProducer;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter.BeanInfoConverter;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter.InterceptorInfoConverter;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedBean;
import eu.jbeernink.hypospray.core.inject.spi.ManagedContextual;
import eu.jbeernink.hypospray.core.inject.spi.ManagedInterceptor;
import eu.jbeernink.hypospray.core.inject.spi.builder.SyntheticComponentManager;
import eu.jbeernink.hypospray.core.inject.spi.factory.ContextualFactory;
import eu.jbeernink.hypospray.core.inject.spi.producer.FactoryBeanProducer;
import eu.jbeernink.hypospray.core.inject.spi.producer.StaticInvokerBeanFactory;
import eu.jbeernink.hypospray.core.inject.spi.producer.SupplierInjectionTarget;
import eu.jbeernink.hypospray.core.internal.Internal;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.types.reflection.ParameterizedTypeImpl;

/// Factory for creating [Container] instances.
public class ContainerFactory {

	private static final Comparator<ServiceLoader.Provider<ContextualFactory>> CONTEXTUAL_FACTORY_PRIORITY_COMPARATOR =
			comparingInt(p -> p.type().getAnnotation(Priority.class) instanceof Priority priority ? priority.value() :
					Integer.MAX_VALUE);

	private final BeanInfoConverter beanInfoConverter;
	private final InterceptorInfoConverter interceptorInfoConverter;
	private final BuildCompatibleExtensionManager extensionManager;

	ContainerFactory(BeanInfoConverter beanInfoConverter, InterceptorInfoConverter interceptorInfoConverter,
	                 BuildCompatibleExtensionManager extensionManager) {
		this.beanInfoConverter = beanInfoConverter;
		this.interceptorInfoConverter = interceptorInfoConverter;
		this.extensionManager = extensionManager;
	}

	public Container createContainer(ContainerSettings containerSettings) {
		var containerRegistry = new ContainerRegistry();
		containerRegistry.registerScope(Dependent.class, new DependentScopeContext(), false);
		containerRegistry.registerScope(Singleton.class, new SingletonScopeContext(), false);

		var creationalContextManager = new CreationalContextManager();

		containerRegistry.registerBean(DiscoveredBean.<InjectionPoint>newBuilder(InjectionPoint.class)
		                                             .addType(InjectionPoint.class)
		                                             .addQualifier(Default.Literal.INSTANCE)
		                                             .setScope(Dependent.class)
		                                             .setInjectionTarget(new FactoryBeanProducer<>(
				                                             new InjectableBeanContainer(creationalContextManager,
						                                             containerRegistry), new StaticInvokerBeanFactory<>(
				                                             (_, _) -> InjectionPointProducer.produceInjectionPoint(),
				                                             List.of(), new NoOpInvoker<>())))
		                                             .build());

		containerRegistry.registerBean(DiscoveredBean.<Function<String, Invoker<Void, Object>>>newBuilder(Function.class)
		                                             .addType(new ParameterizedTypeImpl(Function.class, null,
				                                             List.of(String.class,
						                                             new ParameterizedTypeImpl(Invoker.class, null,
								                                             List.of(Void.class, Object.class)))))
		                                             .addQualifier(ProxyCreationContext.Literal.INSTANCE)
		                                             .setInjectionTarget(new SupplierInjectionTarget<>(
				                                             ProxyContextProducer::produceInvokerFactory))
		                                             .build());

		registerUnmanagedInstances(creationalContextManager, containerRegistry);

		var container = new Container(creationalContextManager, containerRegistry);
		try {
			ContainerProvider.setContainer(container);

			List<ClassInformation<?>> discoveredClasses = performDiscovery(containerRegistry);

			List<ClassConfiguration<?>> discoveredClassConfigurations =
					discoveredClasses.stream().map(ClassConfiguration::fromClassInfo).collect(toUnmodifiableList());

			List<ClassConfiguration<?>> enhancedClasses = performEnhancement(discoveredClassConfigurations);
			performRegistration(enhancedClasses, containerRegistry, creationalContextManager);

			List<ManagedContextual<?>> synthesisedClasses =
					performSynthesis(() -> new InjectableBeanContainer(creationalContextManager, containerRegistry));
			performSyntheticClassRegistration(synthesisedClasses, containerRegistry, creationalContextManager);

			performValidation();

			container.getBeanContainer().getEvent().select(Startup.class).fire(new Startup());

			return container;
		} catch (Exception e) {
			container.close();
			throw e;
		}
	}

	private static void registerUnmanagedInstances(CreationalContextManager creationalContextManager,
	                                               ContainerRegistry containerRegistry) {
		var creationalContextBean = DiscoveredBean.newBuilder(CreationalContextManager.class)
		                                          .addType(CreationalContextManager.class)
		                                          .addQualifier(Internal.Literal.INSTANCE)
		                                          .setScope(Singleton.class)
		                                          .setInjectionTarget(
				                                          new SupplierInjectionTarget<>(() -> creationalContextManager))
		                                          .build();
		containerRegistry.registerBean(creationalContextBean);

		var containerRegistryBean = DiscoveredBean.newBuilder(ContainerRegistry.class)
		                                          .addType(ContainerRegistry.class)
		                                          .addQualifier(Internal.Literal.INSTANCE)
		                                          .setScope(Singleton.class)
		                                          .setInjectionTarget(
				                                          new SupplierInjectionTarget<>(() -> containerRegistry))
		                                          .build();
		containerRegistry.registerBean(containerRegistryBean);
	}

	private List<ClassInformation<?>> performDiscovery(ContainerRegistry containerRegistry) {
		var classDiscoverer = new ClassDiscoverer();
		classDiscoverer.add(InjectableBeanContainer.class);
		classDiscoverer.add(DummyEventInstance.class);

		var metaAnnotations = new MetaAnnotationRegistry(containerRegistry);
		extensionManager.discoverClasses(classDiscoverer, metaAnnotations);

		return classDiscoverer.getDiscoveredClasses();
	}

	private List<ClassConfiguration<?>> performEnhancement(List<ClassConfiguration<?>> discoveredClasses) {
		return extensionManager.performEnhancement(discoveredClasses);
	}

	private List<ManagedContextual<?>> performSynthesis(Supplier<BeanContainer> beanContainerSupplier) {
		var syntheticComponentManager = new SyntheticComponentManager(beanContainerSupplier);

		extensionManager.performSynthesis(syntheticComponentManager);

		return syntheticComponentManager.buildSyntheticContextuals();
	}

	private void performRegistration(List<ClassConfiguration<?>> enhancedClasses, ContainerRegistry containerRegistry,
	                                 CreationalContextManager creationalContextManager) {
		Set<ClassConfiguration<?>> unprocessedClasses =
				enhancedClasses.stream().filter(ContainerFactory::isNotVetoed).collect(toSet());

		var beanContainer = new InjectableBeanContainer(creationalContextManager, containerRegistry);
		ServiceLoader.load(ContextualFactory.class)
		             .stream()
		             .sorted(CONTEXTUAL_FACTORY_PRIORITY_COMPARATOR)
		             .map(ServiceLoader.Provider::get)
		             .map(c -> (ContextualFactory<? extends ManagedContextual<?>>) c)
		             .forEach(contextualFactory -> {
			             Set<ClassConfiguration<?>> processedClasses = new HashSet<>();

			             for (ClassConfiguration<?> unprocessedClass : unprocessedClasses) {
				             if (contextualFactory.isCandidate(unprocessedClass)) {
					             Set<? extends ManagedContextual<?>> contextuals =
							             contextualFactory.processCandidate(unprocessedClass,
									             () -> new InjectableBeanContainer(creationalContextManager, containerRegistry));

					             contextuals.forEach(
							             contextual -> registerContextual(containerRegistry, contextual, beanContainer));

					             processedClasses.add(unprocessedClass);
				             }
			             }

			             unprocessedClasses.removeAll(processedClasses);
		             });
	}

	private void performSyntheticClassRegistration(List<ManagedContextual<?>> syntheticContextuals,
	                                               ContainerRegistry containerRegistry,
	                                               CreationalContextManager creationalContextManager) {
		var beanContainer = new InjectableBeanContainer(creationalContextManager, containerRegistry);

		syntheticContextuals.forEach(contextual -> registerContextual(containerRegistry, contextual, beanContainer));
	}

	private void registerContextual(ContainerRegistry containerRegistry, ManagedContextual<?> contextual,
	                                InjectableBeanContainer beanContainer) {
		switch (contextual) {
			case ManagedBean<?> managedBean -> {
				containerRegistry.registerBean(managedBean);

				extensionManager.notifyRegistration(beanInfoConverter.apply(managedBean, beanContainer));
			}
			case ManagedInterceptor<?> managedInterceptor -> {
				containerRegistry.registerInterceptor(managedInterceptor);

				extensionManager.notifyRegistration(interceptorInfoConverter.apply(managedInterceptor));
			}
		}
	}

	private void performValidation() {
		extensionManager.performValidation();
	}

	public static ContainerFactory newInstance() {
		return new ContainerFactory(new BeanInfoConverter(), new InterceptorInfoConverter(),
				new BuildCompatibleExtensionManager(
						ServiceLoader.load(BuildCompatibleExtension.class).stream().map(ServiceLoader.Provider::get).toList()));
	}

	private static boolean isNotVetoed(ClassConfiguration<?> clazz) {
		return !clazz.isVetoed();
	}
}
