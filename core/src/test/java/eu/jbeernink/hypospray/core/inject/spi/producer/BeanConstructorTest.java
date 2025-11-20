package eu.jbeernink.hypospray.core.inject.spi.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.invoke.ReflectiveConstructorInvoker;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

@DisplayName("Constructor")
class BeanConstructorTest {

	@Nested
	@DisplayName("with no injection points")
	class WithNoInjectionPoints {

		private BeanContainer beanContainer;
		private HyposprayCreationalContext<String> creationalContext;

		private BeanConstructor<String> beanConstructor;

		@BeforeEach
		void setup() throws Exception {
			CreationalContextManager creationalContextManager = new CreationalContextManager();
			beanContainer = new InjectableBeanContainer(creationalContextManager, new ContainerRegistry());
			creationalContext = creationalContextManager.newCreationalContext(newBean(String.class));

			Constructor<String> constructor = String.class.getConstructor();
			beanConstructor = new BeanConstructor<>(new ReflectiveConstructorInvoker<>(constructor), List.of());
		}

		@Test
		@DisplayName("invoke(BeanContainer, HyposprayCreationalContext<T>) creates a new instance.")
		void invoke_createsInstance() {
			String instance = beanConstructor.invoke(beanContainer, creationalContext);

			assertEquals("", instance);
		}
	}

	@Nested
	@DisplayName("with a ReflectiveConstructorInvoker")
	class WithReflectiveConstructorInvoker {
		private BeanContainer beanContainer;
		private HyposprayCreationalContext<String> creationalContext;

		private BeanConstructor<String> beanConstructor;
		private Constructor<String> constructor;

		@BeforeEach
		void setup() throws Exception {
			CreationalContextManager creationalContextManager = new CreationalContextManager();
			beanContainer = new InjectableBeanContainer(creationalContextManager, new ContainerRegistry());
			creationalContext = creationalContextManager.newCreationalContext(newBean(String.class));

			constructor = String.class.getConstructor();
			beanConstructor = new BeanConstructor<>(new ReflectiveConstructorInvoker<>(constructor), List.of());
		}

		@Test
		@DisplayName("constructor() returns the constructor held by the invoker.")
		void constructor_returnsConstructorOwnedByInvoker() {
			var actualConstructor = beanConstructor.constructor();

			assertEquals(constructor, actualConstructor);
		}

	}

	@Nested
	@DisplayName("with invoker which can supply constructor")
	class WithInvokerWhichCanSupplyConstructor {

		private BeanConstructor<String> beanConstructor;
		private Constructor<String> constructor;

		@BeforeEach
		void setup() throws Exception {
			CreationalContextManager creationalContextManager = new CreationalContextManager();
			BeanContainer	beanContainer = new InjectableBeanContainer(creationalContextManager, new ContainerRegistry());
			CreationalContext<String> creationalContext = creationalContextManager.newCreationalContext(newBean(String.class));

			constructor = String.class.getConstructor();
			beanConstructor = new BeanConstructor<>(new ConstructorSupplyingInvoker<>(constructor), List.of());
		}

		@Test
		@DisplayName("constructor() returns the constructor supplied by the invoker.")
		void constructor_returnsConstructorOwnedByInvoker() {
			var actualConstructor = beanConstructor.constructor();

			assertEquals(constructor, actualConstructor);
		}
	}

	@Nested
	@DisplayName("with invoker which cannot supply the constructor")
	class WithInvokerWhichCannotSupplyTheConstructor {
		private BeanConstructor<String> beanConstructor;
		private Constructor<String> constructor;

		@BeforeEach
		void setup() throws Exception {
			constructor = String.class.getConstructor();
			beanConstructor = new BeanConstructor<>((_, arguments) -> Arrays.toString(arguments), List.of());
		}

		@Test
		@DisplayName("constructor() throws IllegalStateException.")
		void constructor_throwsIllegalStateException() {
			var exception = assertThrows(IllegalStateException.class, () -> beanConstructor.constructor());

			assertEquals("Unable to obtain instance of constructor from invoker.", exception.getMessage());
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

	private record ConstructorSupplyingInvoker<T>(Constructor<T> constructor) implements Invoker<Void, T>,
			Supplier<Constructor<T>> {

		@Override
		public T invoke(Void instance, Object[] arguments) throws Exception {
			return constructor.newInstance(arguments);
		}

		@Override
		public Constructor<T> get() {
			return constructor;
		}
	}
}