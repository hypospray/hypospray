package eu.jbeernink.hypospray.core.inject.spi.factory;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.inject.Scope;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.annotation.ClientProxy;
import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ConstructorConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.MethodConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.ConstructorInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedContextual;
import eu.jbeernink.hypospray.core.inject.spi.factory.cases.ExtensionClass;
import eu.jbeernink.hypospray.core.inject.spi.factory.cases.OuterClass.InnerClass;
import eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestAbstractClass;
import eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestClass;
import eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestInterface;
import eu.jbeernink.hypospray.core.inject.spi.producer.BeanConstructor;
import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.core.inject.spi.producer.FactoryBeanProducer;
import eu.jbeernink.hypospray.core.inject.spi.producer.StaticInvokerBeanFactory;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.core.test.invoker.FakeInvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveConstructorInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderFactory;
import eu.jbeernink.hypospray.model.reference.LateReference;

@DisplayName("ManagedBeanFactory")
class ManagedBeanFactoryTest {

	private static RandomGenerator randomGenerator;

	private final TypeFactory typeFactory = TypeFactory.getInstance();
	private final SyntheticAnnotationInformationBuilderFactory annotationInformationBuilderFactory =
			new SyntheticAnnotationInformationBuilderFactory();

	private ManagedBeanFactory managedBeanFactory;
	private InjectableBeanContainer beanContainer;
	private Supplier<InjectableBeanContainer> beanContainerSupplier;
	private FakeInvokerFactoryManager invokerFactoryManager;

	@BeforeAll
	static void beforeSetup() {
		// Use a common randomGenerator for all test instances so that the test always uses a unique value for the proxy class.
		randomGenerator = new IncrementingRandomGenerator();
	}

	@BeforeEach
	void setup() {
		var containerRegistry = new ContainerRegistry();
		var creationalContextManager = new CreationalContextManager();
		beanContainer = new InjectableBeanContainer(creationalContextManager, containerRegistry);
		beanContainerSupplier = () -> beanContainer;
		invokerFactoryManager = new FakeInvokerFactoryManager();
		managedBeanFactory = new ManagedBeanFactory(randomGenerator, invokerFactoryManager);
	}

	@Nested
	@DisplayName("with interface")
	class WithInterface {

		private ClassConfiguration<TestInterface> interfaceClassConfiguration;

		@BeforeEach
		void setup() {
			interfaceClassConfiguration =
					ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestInterface.class));
		}

		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns false")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(interfaceClassConfiguration);

			assertFalse(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) throws IllegalArgumentException.")
		void processCandidate_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> managedBeanFactory.processCandidate(interfaceClassConfiguration, beanContainerSupplier));

			assertEquals("The given ClassConfiguration is not a valid bean candidate type: " + interfaceClassConfiguration,
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with abstract class")
	class WithAbstractClass {

		private ClassConfiguration<TestAbstractClass> abstractClassConfiguration;

		@BeforeEach
		void setup() {
			abstractClassConfiguration =
					ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestAbstractClass.class));
		}

		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns false")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(abstractClassConfiguration);

			assertFalse(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) throws IllegalArgumentException.")
		void processCandidate_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> managedBeanFactory.processCandidate(abstractClassConfiguration, beanContainerSupplier));

			assertEquals("The given ClassConfiguration is not a valid bean candidate type: " + abstractClassConfiguration,
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with inner class")
	class WithInnerClass {

		private ClassConfiguration<InnerClass> innerClassConfiguration;

		@BeforeEach
		void setup() {
			innerClassConfiguration = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(InnerClass.class));
		}


		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns false")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(innerClassConfiguration);

			assertFalse(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) throws IllegalArgumentException.")
		void processCandidate_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> managedBeanFactory.processCandidate(innerClassConfiguration, beanContainerSupplier));

			assertEquals("The given ClassConfiguration is not a valid bean candidate type: " + innerClassConfiguration,
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with extension class")
	class WithExtensionClass {

		private ClassConfiguration<ExtensionClass> extensionClassConfiguration;

		@BeforeEach
		void setup() {
			extensionClassConfiguration =
					ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(ExtensionClass.class));
		}


		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns false")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(extensionClassConfiguration);

			assertFalse(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) throws IllegalArgumentException.")
		void processCandidate_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> managedBeanFactory.processCandidate(extensionClassConfiguration, beanContainerSupplier));

			assertEquals("The given ClassConfiguration is not a valid bean candidate type: " + extensionClassConfiguration,
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with bean class without default constructor")
	class WithBeanClassWithoutDefaultConstructor {

		private ClassConfiguration<TestClass> beanType;

		@BeforeEach
		void setup() {
			beanType =
					new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(), List.of(), List.of(),
							List.of());
		}


		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns false")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(beanType);

			assertFalse(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) throws IllegalArgumentException.")
		void processCandidate_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> managedBeanFactory.processCandidate(beanType, beanContainerSupplier));

			assertEquals("The given ClassConfiguration is not a valid bean candidate type: " + beanType,
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with bean class without injectable constructor")
	class WithBeanClassWithoutInjectableConstructor {

		private ClassConfiguration<TestClass> beanType;

		@BeforeEach
		void setup() {
			var classInformation = new ReflectiveClassInformation<>(TestClass.class);
			List<ConstructorConfiguration<TestClass>> constructors = classInformation.constructorInformation()
			                                                                         .stream()
			                                                                         .filter(constructor ->
					                                                                         constructor.parameterInformation()
					                                                                                    .size() == 1 &&
					                                                                         constructor.parameterInformation()
					                                                                                    .getFirst()
					                                                                                    .type()
					                                                                                    .asJavaType()
					                                                                                    .equals(String.class))
			                                                                         .map(
					                                                                         ConstructorConfiguration::fromConstructorInformation)
			                                                                         .toList();

			beanType = new ClassConfiguration<>(classInformation, List.of(), constructors, List.of(), List.of());
		}


		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns false")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(beanType);

			assertFalse(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) throws IllegalArgumentException.")
		void processCandidate_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> managedBeanFactory.processCandidate(beanType, beanContainerSupplier));

			assertEquals("The given ClassConfiguration is not a valid bean candidate type: " + beanType,
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with valid bean class with default constructor")
	class WithValidBeanClassWithDefaultConstructor {

		private ClassConfiguration<TestClass> beanType;

		@BeforeEach
		void setup() {
			var classInformation = new ReflectiveClassInformation<>(TestClass.class);
			List<ConstructorConfiguration<TestClass>> constructors = classInformation.constructorInformation()
			                                                                         .stream()
			                                                                         .filter(
					                                                                         constructor -> constructor.parameterInformation()
					                                                                                                   .isEmpty())
			                                                                         .map(
					                                                                         ConstructorConfiguration::fromConstructorInformation)
			                                                                         .toList();
			beanType = new ClassConfiguration<>(classInformation, List.of(), constructors, List.of(), List.of());
		}


		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns true")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(beanType);

			assertTrue(isCandidate);
		}

		@Test
		@DisplayName(
				"processCandidate(ClassConfiguration<?>) returns ManagedBean instance with InjectionTarget for the default constructor.")
		void processCandidate_throwsIllegalArgumentException() throws Exception {
			Set<ManagedContextual<?>> result = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

			assertEquals(Set.of(new DiscoveredBean<>(new ReflectiveClassInformation<>(TestClass.class),
					Set.of(typeFactory.of(TestClass.class), typeFactory.of(TestInterface.class), typeFactory.of(Object.class)),
					Set.of(new ReflectiveAnnotationInformation<>(Any.Literal.INSTANCE),
							new ReflectiveAnnotationInformation<>(Default.Literal.INSTANCE)),
					new ReflectiveClassInformation<>(Dependent.class), Set.of(), new ClassBeanProducer<>(beanContainer,
					new BeanConstructor<>(invokerFactoryManager.getInvoker(TestClass.class.getConstructor()), List.of()),
					Set.of(), Set.of(), new NoOpInvoker<>(), new NoOpInvoker<>()), null, false)), result);
		}
	}

	@Nested
	@DisplayName("with valid bean class with injectable constructor")
	class WithValidBeanClassWithInjectableConstructor {

		private ClassConfiguration<TestClass> beanType;

		@BeforeEach
		void setup() {
			ClassInformation<TestClass> classInformation = new ReflectiveClassInformation<>(TestClass.class);

			ConstructorConfiguration<TestClass> constructor = classInformation.constructorInformation()
			                                                                  .stream()
			                                                                  .filter(
					                                                                  c -> c.parameterInformation().size() == 1 &&
					                                                                       c.parameterInformation()
					                                                                        .getFirst()
					                                                                        .type()
					                                                                        .asJavaType()
					                                                                        .equals(String.class))
			                                                                  .map(
					                                                                  ConstructorConfiguration::fromConstructorInformation)
			                                                                  .findFirst()
			                                                                  .orElseThrow();
			constructor.addAnnotation(
					new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Inject.class), Map.of()));

			beanType = new ClassConfiguration<>(classInformation, List.of(), List.of(constructor), List.of(), List.of());
		}


		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns true")
		void isCandidate_returnsFalse() {
			boolean isCandidate = managedBeanFactory.isCandidate(beanType);

			assertTrue(isCandidate);
		}

		@Test
		@DisplayName(
				"processCandidate(ClassConfiguration<?>) returns ManagedBean with InjectionTarget calling the injectable constructor.")
		void processCandidate_throwsIllegalArgumentException() throws Exception {
			Set<ManagedContextual<?>> result = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

			assertEquals(Set.of(new DiscoveredBean<>(new ReflectiveClassInformation<>(TestClass.class),
					Set.of(typeFactory.of(TestClass.class), typeFactory.of(TestInterface.class), typeFactory.of(Object.class)),
					Set.of(new ReflectiveAnnotationInformation<>(Any.Literal.INSTANCE),
							new ReflectiveAnnotationInformation<>(Default.Literal.INSTANCE)),
					new ReflectiveClassInformation<>(Dependent.class), Set.of(), new ClassBeanProducer<>(beanContainer,
					new BeanConstructor<>(invokerFactoryManager.getInvoker(TestClass.class.getConstructor(String.class)), List.of(
							new ConstructorInjectionPoint(typeFactory.of(String.class),
									Set.of(new ReflectiveAnnotationInformation<>(Default.Literal.INSTANCE)), 0,
									new ReflectiveConstructorInformation<>(
											TestClass.class.getConstructor(String.class)).parameterInformation().getFirst(),
									new LateReference<>()))), Set.of(), Set.of(), new NoOpInvoker<>(), new NoOpInvoker<>()), null,
					false)), result);
		}
	}

	@Nested
	@DisplayName("with a class that is a non-bean class with a static producer method")
	class WithStaticProducerClass {

		private ClassConfiguration<TestClass> producerClass;

		@BeforeEach
		void setup() {
			ClassInformation<TestClass> classInformation = new ReflectiveClassInformation<>(TestClass.class);

			MethodConfiguration producerMethod = classInformation.methodInformation()
			                                                     .stream()
			                                                     .filter(method -> method.isStatic() &&
			                                                                       method.name().equals("produceString"))
			                                                     .findFirst()
			                                                     .map(MethodConfiguration::fromMethodInfo)
			                                                     .orElseThrow();
			producerMethod.addAnnotation(Produces.class).addAnnotation(NamedLiteral.of("test"));

			producerClass =
					new ClassConfiguration<>(classInformation, List.of(), List.of(), List.of(producerMethod), List.of());
		}

		@Test
		@DisplayName("isCandidate(ClassConfiguration<?>) returns true")
		void isCandidate_returnsTrue() {
			boolean isCandidate = managedBeanFactory.isCandidate(producerClass);

			assertTrue(isCandidate);
		}

		@Test
		@DisplayName("processCandidate(ClassConfiguration<?>) returns a bean for the static producer method.")
		void processCandidate_returnsBeanForStaticProducerMethod() throws Exception {
			Set<ManagedContextual<?>> managedContextuals =
					managedBeanFactory.processCandidate(producerClass, beanContainerSupplier);

			@SuppressWarnings("unchecked")
			var expectedInvoker =
					(Invoker<Void, String>) invokerFactoryManager.getInvoker(TestClass.class.getMethod("produceString"));
			var expectedBean = DiscoveredBean.<String>newBuilder(TestClass.class)
			                                 .addType(typeFactory.of(String.class))
			                                 .addQualifier(new ReflectiveAnnotationInformation<>(NamedLiteral.of("test")))
			                                 .setInjectionTarget(new FactoryBeanProducer<>(beanContainer,
					                                 new StaticInvokerBeanFactory<>(expectedInvoker, List.of(),
							                                 new NoOpInvoker<>())))
			                                 .build();
			assertEquals(Set.of(expectedBean), managedContextuals);
		}
	}

	@Test
	@DisplayName(
			"processCandidate(ClassConfiguration<?>) with bean class without qualifier annotations, returns a ManagedBean with the @Default and @Any qualifiers.")
	void processCandidate_withBeanClassWithoutQualifierAnnotations_returnsExpectedQualifiers() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));

		Set<ManagedContextual<?>> managedBeans = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> discoveredBean = (DiscoveredBean<?>) getOnly(managedBeans);
		assertEquals(Set.of(Any.Literal.INSTANCE, Default.Literal.INSTANCE), discoveredBean.getQualifiers());
	}

	@Test
	@DisplayName(
			"processCandidate(ClassConfiguration<?>) with bean class with qualifier annotations, returns expected qualifiers.")
	void processCandidate_withBeanClassWithQualifierAnnotations_returnsExpectedQualifiers() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));
		beanType.addAnnotation(Any.Literal.INSTANCE);
		beanType.addAnnotation(NamedLiteral.of("test"));

		Set<ManagedContextual<?>> managedBeans = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> discoveredBean = (DiscoveredBean<?>) getOnly(managedBeans);
		assertEquals(Set.of(Any.Literal.INSTANCE, NamedLiteral.of("test")), discoveredBean.getQualifiers());
	}

	@Test
	@DisplayName(
			"processCandidate(ClassConfiguration<?>) with bean class without scope annotations, returns @Dependent scope.")
	void processCandidate_withBeanClassWithoutScopeAnnotations_returnsDependentScope() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));

		Set<ManagedContextual<?>> managedBeans = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> discoveredBean = (DiscoveredBean<?>) getOnly(managedBeans);
		assertEquals(Dependent.class, discoveredBean.getScope());
	}

	@Test
	@DisplayName("processCandidate(ClassConfiguration<?>) with bean class with scope annotation, returns expected scope.")
	void processCandidate_withBeanClassWithScopeAnnotation_returnsExpectedScope() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));
		beanType.addAnnotation(ScopeAnnotation.Literal.INSTANCE);

		Set<ManagedContextual<?>> managedBeans = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> discoveredBean = (DiscoveredBean<?>) getOnly(managedBeans);
		assertEquals(ScopeAnnotation.class, discoveredBean.getScope());
	}

	@Disabled
	@Test
	@DisplayName(
			"processCandidate(ClassConfiguration<?>) with bean class with normal scope annotation, returns a proxy bean with the expected name.")
	void processCandidate_withBeanClassWithScopeAnnotation_returnsProxyBean() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));
		beanType.addAnnotation(ApplicationScoped.Literal.INSTANCE);

		Set<ManagedContextual<?>> managedBeans = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> proxyBean = managedBeans.stream()
		                                          .filter(contextual -> contextual instanceof DiscoveredBean<?> bean &&
		                                                                !bean.getQualifiers()
		                                                                     .contains(ClientProxy.Literal.INSTANCE))
		                                          .map(DiscoveredBean.class::cast)
		                                          .findFirst()
		                                          .orElseThrow();
		assertEquals("eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestClass$$hypospray$proxy",
				proxyBean.getBeanClass().getName());
	}

	@Test
	@DisplayName("with bean class with multiple scope annotation, throws DefinitionException.")
	void processCandidate_withBeanClassWithMultipleScopeAnnotations_throwsDefinitionException() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));
		beanType.addAnnotation(ScopeAnnotation.Literal.INSTANCE);
		beanType.addAnnotation(RequestScoped.Literal.INSTANCE);

		var exception = assertThrows(DefinitionException.class,
				() -> managedBeanFactory.processCandidate(beanType, beanContainerSupplier));

		assertEquals(
				"Annotated type declares more than one explicit scope: eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestClass",
				exception.getMessage());
	}

	@Test
	@DisplayName("with valid bean class, returns the expected set of types.")
	void processCandidate_withValidBeanClass_returnsExpectedSetOfTypes() {
		Set<Type> beanTypes = Set.of(TestClass.class, TestInterface.class, Object.class);
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));

		Set<ManagedContextual<?>> managedContextuals = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> discoveredBean = (DiscoveredBean<?>) getOnly(managedContextuals);
		assertEquals(beanTypes, discoveredBean.getTypes());
	}

	@Test
	@DisplayName("with bean class annotated with @Typed, returns only the limited set of types")
	void processCandidate_withBeanClassWithTypedAnnotation_returnsLimitedSetOfTypes() {
		var beanType = ClassConfiguration.fromClassInfo(new ReflectiveClassInformation<>(TestClass.class));
		beanType.addAnnotation(Typed.Literal.of(new Class[]{TestInterface.class}));

		Set<ManagedContextual<?>> managedContextuals = managedBeanFactory.processCandidate(beanType, beanContainerSupplier);

		DiscoveredBean<?> discoveredBean = (DiscoveredBean<?>) getOnly(managedContextuals);
		assertEquals(Set.of(TestInterface.class), discoveredBean.getTypes());
	}

	private static ManagedContextual<?> getOnly(Set<ManagedContextual<?>> managedContextuals) {
		return managedContextuals.stream().collect(findOnly(RuntimeException::new)).orElseThrow();
	}

	@Scope
	@interface ScopeAnnotation {
		@SuppressWarnings("ClassExplicitlyAnnotation")
		class Literal extends AnnotationLiteral<ScopeAnnotation> implements ScopeAnnotation {
			public static final Literal INSTANCE = new Literal();
		}
	}

	private static final class IncrementingRandomGenerator implements RandomGenerator {
		private long next = 0;

		@Override
		public long nextLong() {
			try {
				return next;
			} finally {
				next++;
			}
		}
	}
}