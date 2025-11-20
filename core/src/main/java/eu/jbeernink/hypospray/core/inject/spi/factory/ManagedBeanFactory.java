package eu.jbeernink.hypospray.core.inject.spi.factory;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.inject.Inject;

import eu.jbeernink.hypospray.codegeneration.generator.CodeGenerator;
import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxy;
import eu.jbeernink.hypospray.core.annotation.ProxyCreationContext;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.AnnotatedConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ConstructorConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.MethodConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.ConstructorInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedContextual;
import eu.jbeernink.hypospray.core.inject.spi.producer.BeanConstructor;
import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.core.inject.spi.producer.FactoryBeanProducer;
import eu.jbeernink.hypospray.core.inject.spi.producer.StaticInvokerBeanFactory;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.annotation.ArrayValue;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilder;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.ParameterizedTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.reflection.ParameterizedTypeImpl;

@Priority(10)
public final class ManagedBeanFactory implements ContextualFactory<ManagedContextual<?>> {

	private final RandomGenerator randomGenerator;
	private final InvokerFactoryManager invokerFactoryManager;
	private static final ClassInformation<? extends Annotation> DEPENDENT_SCOPE =
			ClassInformationSource.getInstance().getClassInformation(Dependent.class);

	public ManagedBeanFactory() {
		this(RandomGeneratorFactory.getDefault().create(), InvokerFactoryManager.getInstance());
	}

	ManagedBeanFactory(RandomGenerator randomGenerator, InvokerFactoryManager invokerFactoryManager) {
		this.randomGenerator = randomGenerator;
		this.invokerFactoryManager = invokerFactoryManager;
	}

	@Override
	public boolean isCandidate(ClassConfiguration<?> classConfiguration) {
		ClassInformation<?> classInformation = classConfiguration.info();
		if (isAbstract(classInformation) || isInnerClass(classInformation) || isExtension(classInformation)) {
			return false;
		}

		return hasBeanConstructor(classConfiguration) || hasStaticProducerMethods(classConfiguration);
	}

	@Override
	public Set<ManagedContextual<?>> processCandidate(ClassConfiguration<?> classConfiguration,
	                                                  Supplier<InjectableBeanContainer> beanContainerSupplier) {
		if (!isCandidate(classConfiguration)) {
			throw new IllegalArgumentException(
					"The given ClassConfiguration is not a valid bean candidate type: " + classConfiguration);
		}

		var beans = new HashSet<DiscoveredBean<?>>();
		if (hasBeanConstructor(classConfiguration)) {
			DiscoveredBean<?> classBean = newClassBean(classConfiguration, beanContainerSupplier);
			beans.add(classBean);

			if (isProxiedBean(beanContainerSupplier, classBean)) {
				beans.add(createClientProxyBean(classBean, classConfiguration.info(), beanContainerSupplier));
			}
		}

		findStaticProducerMethods(classConfiguration).map(method -> createStaticProducerBean(method, beanContainerSupplier))
		                                             .forEach(beans::add);

		return Set.copyOf(beans);
	}

	private DiscoveredBean<?> createStaticProducerBean(MethodConfiguration method,
	                                                   Supplier<InjectableBeanContainer> beanContainerSupplier) {
		TypeInstance returnType = method.info().returnType();
		InjectableBeanContainer beanContainer = beanContainerSupplier.get();

		// TODO support parameters.

		@SuppressWarnings("unchecked")
		Invoker<Void, Object> invoker =
				(Invoker<Void, Object>) invokerFactoryManager.getInvokerFactory(method.info().declaringClass().name())
				                                             .create(method.info().methodIdentifier());

		// TODO support disposer method.

		var beanProducer = new FactoryBeanProducer<>(beanContainer, new StaticInvokerBeanFactory<>(invoker, List.of(), new NoOpInvoker<>()));

		return DiscoveredBean.newBuilder(method.info().declaringClass())
		                     .addAllTypeInstances(Set.of(returnType))
		                     .addAllQualifiers(getQualifiers(method, beanContainer))
		                     .setScope(getScope(method, beanContainer))
		                     .setInjectionTarget(beanProducer)
		                     .build();
	}

	private <T> DiscoveredBean<T> createClientProxyBean(DiscoveredBean<T> classBean, ClassInformation<?> classInformation,
	                                                    Supplier<InjectableBeanContainer> beanContainerSupplier) {
		try {
			Class<?> beanClass = classBean.getBeanClass();
			ClientProxy clientProxy = CodeGenerator.getInstance()
			                                       .newClientProxy(String.format("%s$$hypospray$proxy%d", beanClass.getName(),
					                                       randomGenerator.nextLong()))
			                                       .withProxiedClass(beanClass)
			                                       .withProxiedClass(classInformation)
			                                       .build();

			if (!ManagedBeanFactory.class.getModule().canRead(beanClass.getModule())) {
				ManagedBeanFactory.class.getModule().addReads(beanClass.getModule());
			}

			Class<?> clientProxyClass =
					MethodHandles.privateLookupIn(beanClass, MethodHandles.lookup()).defineClass(clientProxy.classData());

			ClassInformation<?> proxyClassInformation =
					ClassInformationSource.getInstance().getClassInformation(clientProxyClass);
			TypeFactory typeFactory = TypeFactory.getInstance();
			ParameterizedTypeInstance parameterType = typeFactory.parameterized(Function.class, typeFactory.of(String.class),
					typeFactory.parameterized(Invoker.class, Void.class, Object.class));

			ConstructorInformation<?> proxyConstructor = proxyClassInformation.constructorInformation()
			                                                                  .stream()
			                                                                  .filter(
					                                                                  c -> c.parameterInformation().size() == 1 &&
					                                                                       c.parameterInformation()
					                                                                        .getFirst()
					                                                                        .type()
					                                                                        .equals(
							                                                                        typeFactory.of(Function.class)))
			                                                                  .findFirst()
			                                                                  .orElseThrow(); // TODO throw better exception.

			var beanReference = new LateReference<Bean<?>>();

			@SuppressWarnings("unchecked") Invoker<Void, T> constructorInvoker =
					(Invoker<Void, T>) invokerFactoryManager.getInvoker(clientProxyClass.getName(),
							"new[(%s)%s]".formatted(Function.class.descriptorString(), clientProxyClass.descriptorString()));

			DiscoveredBean<T> proxyBean = DiscoveredBean.<T>newBuilder(clientProxyClass)
			                                            .addType(beanClass)
			                                            .addQualifier(
					                                            eu.jbeernink.hypospray.core.annotation.ClientProxy.Literal.INSTANCE)
			                                            .setInjectionTarget(
					                                            new ClassBeanProducer<>(beanContainerSupplier.get(),
							                                            new BeanConstructor<>(constructorInvoker, List.of(
									                                            new ConstructorInjectionPoint(parameterType, Set.of(
											                                            new SyntheticAnnotationInformationBuilder(
													                                            ClassInformationSource.getInstance()
													                                                                  .getClassInformation(
															                                                                  ProxyCreationContext.class)).build()),
											                                            0, proxyConstructor.parameterInformation().getFirst(),
											                                            beanReference))), Set.of(), Set.of(),
							                                            new NoOpInvoker<>(), new NoOpInvoker<>()))
			                                            .build();

			beanReference.setValue(proxyBean);

			return proxyBean;
		} catch (IllegalAccessException e) {
			throw new DeploymentException(
					"Unable to generate proxy class for due to access restrictions: " + classBean.getBeanClass(), e);
		}
	}

	private <T> DiscoveredBean<T> newClassBean(ClassConfiguration<T> classConfiguration,
	                                           Supplier<? extends BeanContainer> beanContainerSupplier) {
		BeanContainer beanContainer = beanContainerSupplier.get();

		var classBeanProducerFactory = new ClassBeanProducerFactory(beanContainerSupplier, invokerFactoryManager);

		return DiscoveredBean.<T>newBuilder(classConfiguration.info())
		                     .addAllTypes(getTypes(classConfiguration))
		                     .deprecatedAddAllQualifiers(getQualifiers(classConfiguration, beanContainer))
		                     .setScope(getScope(classConfiguration, beanContainer).orElse(Dependent.class))
		                     .setInjectionTarget(classBeanProducerFactory.createClassBeanProducer(classConfiguration))
		                     .build();
	}

	private static <T> Set<Type> getTypes(ClassConfiguration<T> classConfiguration) {
		// TODO update to return TypeInstances.
		Optional<AnnotationInformation> typedAnnotation = classConfiguration.annotations()
		                                                                    .stream()
		                                                                    .filter(
				                                                                    annotationInformation -> annotationInformation.name()
				                                                                                                                  .equals(
						                                                                                                                  Typed.class.getName()))
		                                                                    .findFirst();

		if (typedAnnotation.isPresent() &&
		    typedAnnotation.get().value() instanceof ArrayValue(List<AnnotationMemberValue> types)) {
			return types.stream().map(value -> value.asType().asJavaType()).collect(toUnmodifiableSet());

		}

		return getTypeClosure(classConfiguration.info().classInstance());
	}

	private static ClassInformation<? extends Annotation> getScope(AnnotatedConfiguration configuration,
	                                                               InjectableBeanContainer beanContainer) {
		return findScope(configuration, beanContainer).orElseGet(
				() -> (ClassInformation<Dependent>) ClassInformationSource.getInstance().getClassInformation(Dependent.class));
	}

	private static Optional<ClassInformation<? extends Annotation>> findScope(AnnotatedConfiguration configuration,
	                                                                          InjectableBeanContainer beanContainer) {
		return configuration.annotations()
		                    .stream()
		                    .map(AnnotationInformation::declaration)
		                    .filter(beanContainer::isScope)
		                    .collect(findOnly(() -> new DeploymentException(
				                    "More than one scope defined on member: %s".formatted(configuration))));
	}

	@Deprecated // To be replaced by the non-reflective method above.
	private static Optional<Class<? extends Annotation>> getScope(ClassConfiguration<?> classConfiguration,
	                                                              BeanContainer beanContainer) {
		return classConfiguration.annotations()
		                         .stream()
		                         .map(annotation -> annotation.declaration().classInstance())
		                         .filter(beanContainer::isScope)
		                         .collect(findOnly(() -> new DefinitionException(
				                         "Annotated type declares more than one explicit scope: " +
				                         classConfiguration.info().name())));
	}

	private static Set<AnnotationInformation> getQualifiers(AnnotatedConfiguration configuration,
	                                                        InjectableBeanContainer beanContainer) {
		Set<AnnotationInformation> qualifiers = configuration.annotations()
		                                                     .stream()
		                                                     .filter(annotationInformation -> beanContainer.isQualifier(
				                                                     annotationInformation.declaration()))
		                                                     .collect(toUnmodifiableSet());

		if (qualifiers.isEmpty()) {
			// TODO use synthetic annotation builder instead.
			return Set.of(new ReflectiveAnnotationInformation<>(Default.Literal.INSTANCE),
					new ReflectiveAnnotationInformation<>(Any.Literal.INSTANCE));
		}

		return qualifiers;
	}

	private static Set<Annotation> getQualifiers(ClassConfiguration<?> configuration, BeanContainer beanContainer) {
		Set<Annotation> qualifiers = configuration.annotations()
		                                          .stream()
		                                          .filter(annotationInformation -> beanContainer.isQualifier(
				                                          annotationInformation.declaration().classInstance()))
		                                          .map(AnnotationInformation::annotationInstance)
		                                          .collect(toUnmodifiableSet());

		if (qualifiers.isEmpty()) {
			return Set.of(Default.Literal.INSTANCE, Any.Literal.INSTANCE);
		}

		return qualifiers;
	}

	private static boolean isAbstract(ClassInformation<?> info) {
		return info.isAbstract();
	}

	private static boolean isInnerClass(ClassInformation<?> info) {
		return info.classInstance().getEnclosingClass() != null;
	}

	private static boolean isExtension(ClassInfo classInfo) {
		// TODO migrate to using ClassInformation.
		if (classInfo.name().equals(Extension.class.getName())) {
			return true;
		}

		if (classInfo.superClassDeclaration() != null && isExtension(classInfo.superClassDeclaration())) {
			return true;
		}

		return classInfo.superInterfacesDeclarations().stream().anyMatch(ManagedBeanFactory::isExtension);
	}

	private static boolean hasBeanConstructor(ClassConfiguration<?> classConfiguration) {
		return getBeanConstructor(classConfiguration).isPresent();
	}

	private static <T> Optional<ConstructorConfiguration<T>> getBeanConstructor(
			ClassConfiguration<T> classConfiguration) {
		return getInjectableConstructor(classConfiguration).or(() -> getDefaultConstructor(classConfiguration));
	}

	/// {@return} if a [ClassConfiguration] defines any static producer methods.
	///
	/// @param classConfiguration the class configuration to check.
	private static boolean hasStaticProducerMethods(ClassConfiguration<?> classConfiguration) {
		return findStaticProducerMethods(classConfiguration).findAny().isPresent();
	}

	/// Finds all static producer methods defined by a given [ClassConfiguration].
	///
	/// @param classConfiguration the class configuration to get the static producer methods from.
	/// @return a stream containing all static producer methods.
	private static Stream<MethodConfiguration> findStaticProducerMethods(ClassConfiguration<?> classConfiguration) {
		return findProducerMethods(classConfiguration).filter(method -> method.info().isStatic());
	}

	/// Finds all producer methods defined by a given [ClassConfiguration].
	///
	/// The result will include both static and non-static producer methods.
	///
	/// @param classConfiguration the configuration of the class to find the producer methods on.
	/// @return a stream containing all producer methods.
	private static Stream<MethodConfiguration> findProducerMethods(ClassConfiguration<?> classConfiguration) {
		return classConfiguration.methodConfigurations().stream().filter(method -> method.hasAnnotation(Produces.class));
	}

	private static <T> Optional<ConstructorConfiguration<T>> getInjectableConstructor(
			ClassConfiguration<T> classConfiguration) {
		return classConfiguration.constructorConfigurations()
		                         .stream()
		                         .filter(constructor -> constructor.hasAnnotation(Inject.class))
		                         .collect(findOnly(() -> new DeploymentException(
				                         String.format("%s has multiple constructors marked with @Inject.",
						                         classConfiguration.info().name()))));
	}

	private static <T> Optional<ConstructorConfiguration<T>> getDefaultConstructor(
			ClassConfiguration<T> classConfiguration) {
		return classConfiguration.constructorConfigurations()
		                         .stream()
		                         .filter(annotatedConstructor -> annotatedConstructor.parameters().isEmpty())
		                         .findAny();
	}

	private static boolean isProxiedBean(Supplier<InjectableBeanContainer> beanContainerSupplier,
	                                     DiscoveredBean<?> classBean) {
		return beanContainerSupplier.get().isNormalScope(classBean.getScope());
	}

	private static Set<Type> getTypeClosure(Type type) {
		var typeClosure = new HashSet<Type>();
		var typeVariableMapping = new HashMap<TypeVariable<?>, Type>();

		var typesToExplore = new ArrayList<Type>();
		typesToExplore.add(type);

		while (!typesToExplore.isEmpty()) {
			Type newType = typesToExplore.removeFirst();
			switch (newType) {
				case Class<?> clazz -> {
					for (TypeVariable<?> typeVariable : clazz.getTypeParameters()) {
						typeVariableMapping.putIfAbsent(typeVariable, typeVariable);
					}
					typeClosure.add(clazz);

					typesToExplore.addAll(getParentTypes(clazz));
				}
				case ParameterizedType parameterizedType -> {
					if (parameterizedType.getRawType() instanceof Class<?> clazz) {
						for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
							typeVariableMapping.putIfAbsent(clazz.getTypeParameters()[i],
									parameterizedType.getActualTypeArguments()[i]);
						}


						List<Type> resolvedTypes = Arrays.stream(parameterizedType.getActualTypeArguments()).map(typeArgument -> {
							if (typeArgument instanceof TypeVariable<?> typeVariable) {
								return typeVariableMapping.get(typeVariable);
							}

							return typeArgument;
						}).toList();

						typeClosure.add(new ParameterizedTypeImpl(clazz, null, resolvedTypes));

						typesToExplore.addAll(getParentTypes(clazz));
					}
				}
				default -> throw new IllegalArgumentException("Unsupported type: " + type);
			}
		}

		return typeClosure;
	}

	private static Set<Type> getParentTypes(Class<?> clazz) {
		Set<Type> parentTypes = new HashSet<>();
		if (clazz.getGenericSuperclass() != null) {
			parentTypes.add(clazz.getGenericSuperclass());
		}

		parentTypes.addAll(Arrays.asList(clazz.getGenericInterfaces()));
		return parentTypes;
	}
}
