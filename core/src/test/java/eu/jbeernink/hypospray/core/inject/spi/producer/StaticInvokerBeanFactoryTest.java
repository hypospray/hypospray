package eu.jbeernink.hypospray.core.inject.spi.producer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.exception.BeanCreationException;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.invoker.factory.NoOpInvoker;

@DisplayName("StaticInvokerBeanFactory")
class StaticInvokerBeanFactoryTest {

	private InjectableBeanContainer beanContainer;
	private HyposprayCreationalContext<String> creationalContext;

	@BeforeEach
	void setUp() {
		CreationalContextManager creationalContextManager = new CreationalContextManager();
		beanContainer = new InjectableBeanContainer(creationalContextManager, new ContainerRegistry());
	}

	@Nested
	@DisplayName("without injection points")
	class WithoutInjectionPoints {

		private final FakeInvoker<String> factoryInvoker = new FakeInvoker<>();

		private StaticInvokerBeanFactory<String> factory =
				new StaticInvokerBeanFactory<>(factoryInvoker, List.of(), new NoOpInvoker<>());

		@Test
		@DisplayName("create(BeanContainer, HyposprayCreationalContext<T>) calls the factory invoker.")
		void create_callsFactoryInvoker() {
			var _ = factory.create(beanContainer, beanContainer.createCreationalContext(newBean(String.class)));

			assertEquals(List.of(new Invocation()), factoryInvoker.invocations);
		}

		@Test
		@DisplayName(
				"create(BeanContainer, HyposprayCreationalContext<T>) returns the value returned by the factory invoker.")
		void create_returnsFactoryInvokerValue() {
			var expectedValue = "Hello world";
			factoryInvoker.setReturnValue(expectedValue);

			String result = factory.create(beanContainer, beanContainer.createCreationalContext(newBean(String.class)));

			assertEquals(expectedValue, result);
		}
	}

	@Test
	@DisplayName(
			"create(BeanContainer, HypsprayCreationalContext) throws a BeanCreationException when an exception is thrown from the factory invoker.")
	void create_whenInvokerThrowsException_throwsBeanCreationException() {
		var factoryInvoker = new FakeInvoker<String>();
		var factory = new StaticInvokerBeanFactory<>(factoryInvoker, List.of(), new NoOpInvoker<>());
		var errorMessage = "Something went wrong.";
		factoryInvoker.setExceptionSupplier(() -> new IllegalArgumentException(errorMessage));

		var exception = assertThrows(BeanCreationException.class,
				() -> factory.create(beanContainer, beanContainer.createCreationalContext(newBean(String.class))));

		assertAll(() -> assertEquals(errorMessage, exception.getCause().getMessage(),
						"Error message in cause must match expected error message."),
				() -> assertInstanceOf(IllegalArgumentException.class, exception.getCause(),
						"Cause must match expected exception type."),
				() -> assertEquals("Unable to create instance of bean.", exception.getMessage(),
						"Error message must match expected message."));
	}

	private record Invocation(Object... arguments) {

		@Override
		public boolean equals(Object obj) {
			return switch (obj) {
				case Invocation(Object[] args) -> Arrays.equals(args, arguments);
				default -> false;
			};
		}
	}

	private static class FakeInvoker<T> implements Invoker<Void, T> {

		private final List<Invocation> invocations = new LinkedList<>();
		private T returnValue;
		private Supplier<? extends Exception> exceptionSupplier = null;

		public FakeInvoker<T> setReturnValue(T returnValue) {
			this.returnValue = returnValue;
			return this;
		}

		public void setExceptionSupplier(Supplier<? extends Exception> exceptionSupplier) {
			this.exceptionSupplier = exceptionSupplier;
		}

		public List<Invocation> getInvocations() {
			return List.copyOf(invocations);
		}

		@Override
		public T invoke(Void unused, Object[] arguments) throws Exception {
			invocations.add(new Invocation(arguments));

			if (exceptionSupplier != null) {
				throw exceptionSupplier.get();
			}
			return returnValue;
		}
	}

	private static <T> Bean<T> newBean(Class<T> clazz) {
		return new Bean<T>() {
			@Override
			public Class<?> getBeanClass() {
				return clazz;
			}

			@Override
			public Set<InjectionPoint> getInjectionPoints() {
				return Set.of();
			}

			@Override
			public T create(CreationalContext<T> creationalContext) {
				return null;
			}

			@Override
			public void destroy(T instance, CreationalContext<T> creationalContext) {
			}

			@Override
			public Set<Type> getTypes() {
				return Set.of(clazz);
			}

			@Override
			public Set<Annotation> getQualifiers() {
				return Set.of();
			}

			@Override
			public Class<? extends Annotation> getScope() {
				return Dependent.class;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public Set<Class<? extends Annotation>> getStereotypes() {
				return Set.of();
			}

			@Override
			public boolean isAlternative() {
				return false;
			}
		};
	}
}