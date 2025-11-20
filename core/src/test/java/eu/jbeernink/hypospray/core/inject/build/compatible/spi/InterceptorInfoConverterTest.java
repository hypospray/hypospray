package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import static jakarta.enterprise.inject.spi.InterceptionType.AROUND_INVOKE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter.InterceptorInfoConverter;
import eu.jbeernink.hypospray.core.inject.spi.ConstructorInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedInterceptor;
import eu.jbeernink.hypospray.core.inject.spi.producer.BeanConstructor;
import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.core.invoke.NoopInvoker;
import eu.jbeernink.hypospray.core.invoke.ReflectiveConstructorInvoker;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveParameterInformation;

@DisplayName("InterceptorInfoConverter")
class InterceptorInfoConverterTest {

	private @interface MyInterceptorBinding {}

	@SuppressWarnings("ClassExplicitlyAnnotation")
	private static class MyInterceptorBindingLiteral extends AnnotationLiteral<MyInterceptorBinding> implements
			MyInterceptorBinding {}

	private static final MyInterceptorBindingLiteral MY_INTERCEPTOR_BINDING_LITERAL = new MyInterceptorBindingLiteral();

	private static class TestInterceptor {

		private final BeanContainer beanContainer;

		@Inject
		public TestInterceptor(BeanContainer beanContainer) {
			this.beanContainer = beanContainer;
		}
	}

	private InterceptorInfoConverter converter;

	private BeanContainer beanContainer;

	@BeforeEach
	void setup() {
		converter = new InterceptorInfoConverter();
		beanContainer = new InjectableBeanContainer(new CreationalContextManager(), new ContainerRegistry());
	}

	@Test
	@DisplayName("apply(Interceptor<?>) converts an interceptor to InterceptorInformation.")
	void apply_convertsInterceptorToInterceptorInfo() throws Exception {
		int priority = 10;
		var interceptionTypes = Set.of(AROUND_INVOKE);
		var interceptor = createInterceptor(interceptionTypes, priority);
		var interceptorClassInfo = new ReflectiveClassInformation<>(TestInterceptor.class);
		TypeFactory typeFactory = TypeFactory.getInstance();

		InterceptorInformation interceptorInfo = converter.apply(interceptor);

		assertEquals(new InterceptorInformation(interceptorClassInfo,
				Set.of(new ReflectiveAnnotationInformation<>(MY_INTERCEPTOR_BINDING_LITERAL)), interceptionTypes,
				Set.of(typeFactory.fromJavaType(TestInterceptor.class), typeFactory.fromJavaType(Object.class)), Set.of(),
				Set.of(), priority), interceptorInfo);
	}

	private ManagedInterceptor<TestInterceptor> createInterceptor(Set<InterceptionType> interceptionTypes, int priority)
			throws NoSuchMethodException {
		Constructor<TestInterceptor> constructor = TestInterceptor.class.getConstructor(BeanContainer.class);

		return new ManagedInterceptor<>(new ReflectiveClassInformation<>(TestInterceptor.class),
				Set.of(TestInterceptor.class, Object.class), Set.of(MY_INTERCEPTOR_BINDING_LITERAL), interceptionTypes,
				new ClassBeanProducer<>(beanContainer, new BeanConstructor<>(new ReflectiveConstructorInvoker<>(constructor),
						List.of(new ConstructorInjectionPoint(TypeFactory.getInstance().of(BeanContainer.class), Set.of(), 0,
								new ReflectiveParameterInformation(constructor.getParameters()[0]), null))), Set.of(), Set.of(),
						new NoOpInvoker<>(), new NoOpInvoker<>()), priority, new NoopInvoker<>(), new NoopInvoker<>(),
				new NoopInvoker<>(), new NoopInvoker<>(), new NoopInvoker<>());
	}
}