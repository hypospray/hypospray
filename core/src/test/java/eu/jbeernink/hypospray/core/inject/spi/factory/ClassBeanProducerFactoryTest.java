package eu.jbeernink.hypospray.core.inject.spi.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.literal.InjectLiteral;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ConstructorConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.FieldConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.MethodConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.ConstructorInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.FieldInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.InitializerMethod;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.MethodInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestClass;
import eu.jbeernink.hypospray.core.inject.spi.producer.BeanConstructor;
import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.core.test.invoker.FakeInvoker;
import eu.jbeernink.hypospray.core.test.invoker.FakeInvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactory;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveConstructorInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveFieldInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveMethodInformation;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilder;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderFactory;
import eu.jbeernink.hypospray.model.reference.LateReference;

@DisplayName("ClassBeanProducerFactory")
class ClassBeanProducerFactoryTest {

	private BeanContainer beanContainer;
	private ClassBeanProducerFactory factory;
	private FakeInvokerFactoryManager invokerFactoryManager;
	private SyntheticAnnotationInformationBuilderFactory annotationInformationBuilderFactory;
	private TypeFactory typeFactory;

	@BeforeEach
	void setup() {
		beanContainer = new InjectableBeanContainer(new CreationalContextManager(), new ContainerRegistry());
		invokerFactoryManager = new FakeInvokerFactoryManager();
		factory = new ClassBeanProducerFactory(() -> beanContainer, invokerFactoryManager);
		annotationInformationBuilderFactory = new SyntheticAnnotationInformationBuilderFactory();
		typeFactory = TypeFactory.getInstance();
	}

	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with default constructor, returns instance for default constructor.")
	void createClassBeanProducer_withTypeWithDefaultConstructor_returnsInstanceForDefaultConstructor() throws Exception {
		var constructor = TestClass.class.getConstructor();
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(newConstructorConfiguration(constructor)), List.of(), List.of());

		ClassBeanProducer<TestClass> classBeanProducer = factory.createClassBeanProducer(classConfiguration);

		assertEquals(new ClassBeanProducer<>(beanContainer,
				new BeanConstructor<>(invokerFactoryManager.getInvoker(constructor), List.of()), Set.of(), Set.of(),
				new NoOpInvoker<>(), new NoOpInvoker<>()), classBeanProducer);
	}

	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with constructor annotated with @Inject, returns instance to use the annotated constructor.")
	void createClassBeanProducer_withInjectableConstructor_returnsInstanceForInjectableConstructorConstructor()
			throws Exception {
		var constructor = TestClass.class.getConstructor();
		var injectableConstructor = TestClass.class.getConstructor(String.class);
		var injectableConstructorConfiguration = newConstructorConfiguration(injectableConstructor);
		injectableConstructorConfiguration.addAnnotation(InjectLiteral.INSTANCE);
		injectableConstructorConfiguration.parameterConfigurations().getFirst().addAnnotation(NamedLiteral.of("test"));
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(newConstructorConfiguration(constructor), injectableConstructorConfiguration), List.of(), List.of());

		ClassBeanProducer<TestClass> classBeanProducer = factory.createClassBeanProducer(classConfiguration);

		assertEquals(new ClassBeanProducer<>(beanContainer,
						new BeanConstructor<>(invokerFactoryManager.getInvoker(injectableConstructor), List.of(
								new ConstructorInjectionPoint(typeFactory.of(String.class),
										Set.of(annotationInformationBuilderFactory.create(Named.class).value("test").build()), 0,
										new ReflectiveConstructorInformation<>(injectableConstructor).parameterInformation().getFirst(),
										new LateReference<>()))), Set.of(), Set.of(), new NoOpInvoker<>(), new NoOpInvoker<>()),
				classBeanProducer);
	}

	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with multiple constructors annotated with @Inject, throws DefinitionException.")
	void createClassBeanProducer_withMultipleInjectableConstructors_returnsInstanceForInjectableConstructorConstructor()
			throws Exception {
		var injectableConstructor1 = TestClass.class.getConstructor();
		var injectableConstructor2 = TestClass.class.getConstructor(String.class);
		ConstructorConfiguration<TestClass> injectableConstructorConfiguration1 =
				newConstructorConfiguration(injectableConstructor1);
		injectableConstructorConfiguration1.addAnnotation(InjectLiteral.INSTANCE);
		ConstructorConfiguration<TestClass> injectableConstructorConfiguration2 =
				newConstructorConfiguration(injectableConstructor2);
		injectableConstructorConfiguration2.addAnnotation(InjectLiteral.INSTANCE);
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(injectableConstructorConfiguration1, injectableConstructorConfiguration2), List.of(), List.of());

		var exception = assertThrows(DefinitionException.class, () -> factory.createClassBeanProducer(classConfiguration));

		assertEquals("Class may only have a single constructor annotated with @Inject: " +
		             "eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestClass", exception.getMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with injectable fields, returns ClassBeanProducer with expected injection points.")
	void createClassBeanProducer_withInjectableFields_returnsClassBeanProducerWithExpectedInjectionPoints()
			throws Exception {
		var constructor = TestClass.class.getConstructor();
		Field unqualifiedField = TestClass.class.getDeclaredField("field1");
		FieldConfiguration unqualifiedFieldConfiguration = newFieldConfiguration(unqualifiedField);
		unqualifiedFieldConfiguration.addAnnotation(InjectLiteral.INSTANCE);
		Field qualifiedField = TestClass.class.getDeclaredField("field2");
		FieldConfiguration qualifiedFieldConfiguration = newFieldConfiguration(qualifiedField);
		var qualifiedFieldQualifier = NamedLiteral.of("qualified");
		qualifiedFieldConfiguration.addAnnotation(qualifiedFieldQualifier);
		qualifiedFieldConfiguration.addAnnotation(InjectLiteral.INSTANCE);
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(newConstructorConfiguration(constructor)), List.of(),
				List.of(qualifiedFieldConfiguration, unqualifiedFieldConfiguration));

		ClassBeanProducer<TestClass> classBeanProducer = factory.createClassBeanProducer(classConfiguration);

		InvokerFactory<TestClass> invokerFactory =
				(InvokerFactory<TestClass>) invokerFactoryManager.getInvokerFactory(TestClass.class.getName());
		var unqualifiedFieldInformation = new ReflectiveFieldInformation(unqualifiedField);
		var unqualifiedFieldSetter = (Invoker<TestClass, Void>) (Object) invokerFactory.apply(
				unqualifiedFieldInformation.syntheticSetterMethodIdentifier());
		var qualifiedFieldInformation = new ReflectiveFieldInformation(qualifiedField);
		var qualifiedFieldSetter = (Invoker<TestClass, Void>) (Object) invokerFactory.apply(
				qualifiedFieldInformation.syntheticSetterMethodIdentifier());
		assertEquals(new ClassBeanProducer<>(beanContainer,
						new BeanConstructor<>(invokerFactoryManager.getInvoker(constructor), List.of()), Set.of(
						new FieldInjectionPoint(typeFactory.of(String.class),
								Set.of(annotationInformationBuilderFactory.create(Default.class).build()), unqualifiedFieldInformation,
								new LateReference<>(), unqualifiedFieldSetter), new FieldInjectionPoint(typeFactory.ofObject(),
								Set.of(new ReflectiveAnnotationInformation<>(qualifiedFieldQualifier)), qualifiedFieldInformation,
								new LateReference<>(), qualifiedFieldSetter)), Set.of(), new NoOpInvoker<>(), new NoOpInvoker<>()),
				classBeanProducer);
	}

	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with initialization methods, returns ClassBeanProducer with expected injection points.")
	void createClassBeanProducer_withInitializationMethods_returnsClassBeanProducerWithExpectedInjectionPoints()
			throws Exception {
		var constructor = TestClass.class.getConstructor();
		Method field1Setter = TestClass.class.getDeclaredMethod("setField1", String.class);
		MethodConfiguration field1SetterConfiguration = newMethodConfiguration(field1Setter);
		field1SetterConfiguration.parameterConfigurations().getFirst().addAnnotation(NamedLiteral.of("qualified"));
		field1SetterConfiguration.addAnnotation(InjectLiteral.INSTANCE);
		Method field2Setter = TestClass.class.getDeclaredMethod("setField2", Object.class);
		MethodConfiguration field2SetterConfiguration = newMethodConfiguration(field2Setter);
		field2SetterConfiguration.addAnnotation(InjectLiteral.INSTANCE);
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(newConstructorConfiguration(constructor)),
				List.of(field1SetterConfiguration, field2SetterConfiguration), List.of());

		ClassBeanProducer<TestClass> classBeanProducer = factory.createClassBeanProducer(classConfiguration);

		var field1SetterInformation = new ReflectiveMethodInformation(field1Setter);
		var testClassName = "eu.jbeernink.hypospray.core.inject.spi.factory.cases.TestClass";
		var field1InitializationMethod = new InitializerMethod(field1SetterInformation,
				new FakeInvoker<>(testClassName, "setField1[(Ljava/lang/String;)V]"), List.of(
				new MethodInjectionPoint(typeFactory.of(String.class),
						Set.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("qualified"))),
						field1SetterInformation.parameterInformation().getFirst(), 0, new LateReference<>())));
		var field2SetterInformation = new ReflectiveMethodInformation(field2Setter);
		var field2InitializationMethod = new InitializerMethod(field2SetterInformation,
				new FakeInvoker<>(testClassName, "setField2[(Ljava/lang/Object;)V]"), List.of(
				new MethodInjectionPoint(typeFactory.ofObject(),
						Set.of(new ReflectiveAnnotationInformation<>(Default.Literal.INSTANCE)),
						field2SetterInformation.parameterInformation().getFirst(), 0, new LateReference<>())));
		assertEquals(new ClassBeanProducer<>(beanContainer,
						new BeanConstructor<>(invokerFactoryManager.getInvoker(constructor), List.of()), Set.of(),
						Set.of(field1InitializationMethod, field2InitializationMethod), new NoOpInvoker<>(), new NoOpInvoker<>()),
				classBeanProducer);
	}

	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with a class with a post-construct method, creates a bean with a post-construct invoker.")
	void createClassBeanProducer_withClassWithPostConstruct_createsBeanWithPostConstructInvoker() throws Exception {
		var constructor = TestClass.class.getConstructor();
		var postConstructCallback = TestClass.class.getDeclaredMethod("postConstruct");
		var postConstructMethodConfiguration =
				new MethodConfiguration(new ReflectiveMethodInformation(postConstructCallback), List.of(
						new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(PostConstruct.class)).build()),
						List.of());
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(newConstructorConfiguration(constructor)), List.of(postConstructMethodConfiguration), List.of());

		ClassBeanProducer<TestClass> classBeanProducer = factory.createClassBeanProducer(classConfiguration);

		var expectedClassBeanProducer = new ClassBeanProducer<>(beanContainer,
				new BeanConstructor<>(invokerFactoryManager.getInvoker(constructor), List.of()), Set.of(), Set.of(),
				new FakeInvoker<>(TestClass.class.getName(), "postConstruct[()V]"), new NoOpInvoker<>());
		assertEquals(expectedClassBeanProducer, classBeanProducer);
	}

	@Test
	@DisplayName(
			"createClassBeanProducer(ClassConfiguration<T>) with a class with a pre-destroy method, creates a bean with a pre-destroy invoker.")
	void createClassBeanProducer_withClassWithPreDestroy_createsBeanWithPreDestroyInvoker() throws Exception {
		var constructor = TestClass.class.getConstructor();
		var postConstructCallback = TestClass.class.getDeclaredMethod("preDestroy");
		var postConstructMethodConfiguration =
				new MethodConfiguration(new ReflectiveMethodInformation(postConstructCallback), List.of(
						new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(PreDestroy.class)).build()),
						List.of());
		var classConfiguration = new ClassConfiguration<>(new ReflectiveClassInformation<>(TestClass.class), List.of(),
				List.of(newConstructorConfiguration(constructor)), List.of(postConstructMethodConfiguration), List.of());

		ClassBeanProducer<TestClass> classBeanProducer = factory.createClassBeanProducer(classConfiguration);

		var expectedClassBeanProducer = new ClassBeanProducer<>(beanContainer,
				new BeanConstructor<>(invokerFactoryManager.getInvoker(constructor), List.of()), Set.of(), Set.of(),
				new NoOpInvoker<>(), new FakeInvoker<>(TestClass.class.getName(), "preDestroy[()V]"));
		assertEquals(expectedClassBeanProducer, classBeanProducer);
	}

	private static MethodConfiguration newMethodConfiguration(Method method) {
		return MethodConfiguration.fromMethodInfo(new ReflectiveMethodInformation(method));
	}

	private static <T> ConstructorConfiguration<T> newConstructorConfiguration(Constructor<T> constructor) {
		return ConstructorConfiguration.fromConstructorInformation(new ReflectiveConstructorInformation<>(constructor));
	}

	private static FieldConfiguration newFieldConfiguration(Field field) {
		return FieldConfiguration.fromFieldInformation(new ReflectiveFieldInformation(field));
	}
}