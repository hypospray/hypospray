package eu.jbeernink.hypospray.core.inject.spi.factory;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Inject;

import eu.jbeernink.hypospray.core.exception.DeploymentException;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.AnnotatedConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ConstructorConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.FieldConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.MethodConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ParameterConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.ConstructorInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.FieldInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.InitializerMethod;
import eu.jbeernink.hypospray.core.inject.spi.MethodInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.producer.BeanConstructor;
import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilder;
import eu.jbeernink.hypospray.model.reference.LateReference;

/// Factory to produce instances of [ClassBeanProducer] from [ClassConfiguration] instances.
public class ClassBeanProducerFactory {

	private final Supplier<? extends BeanContainer> beanContainerProvider;
	private final InvokerFactoryManager invokerFactoryManager;

	public ClassBeanProducerFactory(Supplier<? extends BeanContainer> beanContainerProvider,
	                                InvokerFactoryManager invokerFactoryManager) {
		this.beanContainerProvider = beanContainerProvider;
		this.invokerFactoryManager = invokerFactoryManager;
	}

	/// Create a [ClassBeanProducer] for the given [ClassConfiguration].
	///
	/// @param classConfiguration the class configuration to create a class bean producer from.
	/// @param <T>                the generic type of the class.
	/// @return a new class bean producer.
	public <T> ClassBeanProducer<T> createClassBeanProducer(ClassConfiguration<T> classConfiguration) {
		return new ClassBeanProducer<>(beanContainerProvider.get(), findSuitableConstructor(classConfiguration),
				getFieldInjectionPoints(classConfiguration), getInitializationMethods(classConfiguration),
				getPostConstructCallbackInvoker(classConfiguration).orElseGet(NoOpInvoker::new),
				getPreDestroyCallbackInvoker(classConfiguration).orElseGet(NoOpInvoker::new));
	}

	private <T> Set<InitializerMethod> getInitializationMethods(ClassConfiguration<T> classConfiguration) {
		return classConfiguration.methodConfigurations()
		                         .stream()
		                         .filter(method -> !method.info().isStatic())
		                         .filter(method -> method.hasAnnotation(Inject.class))
		                         .map(this::getInitializationMethod)
		                         .collect(toUnmodifiableSet());
	}

	private InitializerMethod getInitializationMethod(MethodConfiguration method) {
		MethodInformation methodInformation = method.info();
		Invoker<?, ?> invoker = invokerFactoryManager.getInvoker(methodInformation.declaringClass().name(),
				methodInformation.methodIdentifier());

		List<MethodInjectionPoint> injectionPoints = IntStream.range(0, method.parameters().size()).mapToObj(i -> {
			ParameterConfiguration parameterConfiguration = method.parameterConfigurations().get(i);

			return new MethodInjectionPoint(parameterConfiguration.info().type(),
					getInjectionPointQualifiers(parameterConfiguration), methodInformation.parameterInformation().get(i), i,
					new LateReference<>());
		}).toList();

		return new InitializerMethod(methodInformation, invoker, injectionPoints);
	}

	private Set<FieldInjectionPoint> getFieldInjectionPoints(ClassConfiguration<?> classConfiguration) {
		return classConfiguration.fieldConfigurations()
		                         .stream()
		                         .filter(field -> !field.info().isStatic())
		                         .filter(field -> field.hasAnnotation(Inject.class))
		                         .map(field -> new FieldInjectionPoint(field.info().type(),
				                         getInjectionPointQualifiers(field), field.info(), new LateReference<>(),
				                         getFieldSetter(classConfiguration, field)))
		                         .collect(toUnmodifiableSet());
	}

	public <T> BeanConstructor<T> findSuitableConstructor(ClassConfiguration<T> classConfiguration) {
		return classConfiguration.constructorConfigurations()
		                         .stream()
		                         .filter(constructor -> constructor.hasAnnotation(Inject.class))
		                         .collect(findOnly(() -> new DefinitionException(
				                         "Class may only have a single constructor annotated with @Inject: " +
				                         classConfiguration.info().name())))
		                         .or(() -> classConfiguration.constructorConfigurations()
		                                                     .stream()
		                                                     .filter(constructor -> constructor.parameterConfigurations()
		                                                                                       .isEmpty())
		                                                     .findFirst())
		                         .map(this::createBeanConstructor)
		                         .orElseThrow(
				                         () -> new IllegalArgumentException("No suitable constructor found for bean."));
	}

	private <T> BeanConstructor<T> createBeanConstructor(ConstructorConfiguration<T> constructor) {
		var constructorReference = new LateReference<Bean<?>>();

		List<ConstructorInjectionPoint> constructorInjectionPoints =
				IntStream.range(0, constructor.parameters().size()).mapToObj(i -> {
					ParameterConfiguration parameterConfiguration = constructor.parameterConfigurations().get(i);

					return new ConstructorInjectionPoint(parameterConfiguration.info().type(),
							getInjectionPointQualifiers(parameterConfiguration), i, parameterConfiguration.info(),
							constructorReference);
				}).toList();


		@SuppressWarnings("unchecked") Invoker<Void, T> constructorInvoker =
				(Invoker<Void, T>) invokerFactoryManager.getInvoker(constructor.info().declaringClass().name(),
						constructor.info().methodIdentifier());

		return new BeanConstructor<>(constructorInvoker, constructorInjectionPoints);
	}

	private Set<AnnotationInformation> getInjectionPointQualifiers(AnnotatedConfiguration injectionPoint) {
		Set<AnnotationInformation> qualifiers = injectionPoint.annotations()
		                                                      .stream()
		                                                      .filter(annotation -> beanContainerProvider.get()
		                                                                                                 .isQualifier(
				                                                                                                 annotation.declaration()
				                                                                                                           .classInstance()))
		                                                      .collect(toUnmodifiableSet());

		if (qualifiers.isEmpty()) {
			return Set.of(new SyntheticAnnotationInformationBuilder(
					ClassInformationSource.getInstance().getClassInformation(Default.class)).build());
		}

		return qualifiers;
	}

	@SuppressWarnings("unchecked")
	private <T> Invoker<T, Void> getFieldSetter(ClassConfiguration<T> classConfiguration, FieldConfiguration field) {
		return (Invoker<T, Void>) invokerFactoryManager.getInvoker(classConfiguration.info().name(),
				field.info().syntheticSetterMethodIdentifier());
	}

	private <T> Optional<Invoker<T, Void>> getPostConstructCallbackInvoker(ClassConfiguration<T> classConfiguration) {
		return findLifeCycleCallback(classConfiguration, PostConstruct.class);
	}

	private <T> Optional<Invoker<T, Void>> getPreDestroyCallbackInvoker(ClassConfiguration<T> classConfiguration) {
		return findLifeCycleCallback(classConfiguration, PreDestroy.class);
	}

	private <T> Optional<Invoker<T, Void>> findLifeCycleCallback(ClassConfiguration<T> classConfiguration,
	                                                             Class<? extends Annotation> lifeCycleCallbackAnnotation) {
		return classConfiguration.methodConfigurations()
		                         .stream()
		                         .filter(
				                         methodConfiguration -> methodConfiguration.hasAnnotation(lifeCycleCallbackAnnotation))
		                         .map(methodConfiguration -> {
			                         @SuppressWarnings("unchecked") Invoker<T, Void> invoker =
					                         (Invoker<T, Void>) invokerFactoryManager.getInvoker(classConfiguration.info().name(),
							                         methodConfiguration.info().methodIdentifier());
			                         return invoker;
		                         })
		                         .collect(findOnly(() -> new DeploymentException(
				                         "Multiple methods annotation with @%s in class %s".formatted(
						                         lifeCycleCallbackAnnotation.getSimpleName(), classConfiguration.info().name()))));
	}

}
