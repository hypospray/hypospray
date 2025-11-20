package eu.jbeernink.hypospray.core.context.singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SingletonScopeContext")
class SingletonScopeContextTest {

	@Test
	@DisplayName("isActive() returns true.")
	void isActive_returnsTrue() {
		SingletonScopeContext context = new SingletonScopeContext();

		boolean isActive = context.isActive();

		assertTrue(isActive);
	}

	@Test
	@DisplayName("getScope() returns Singleton.class.")
	void getScope_returnsSingleton() {
		SingletonScopeContext context = new SingletonScopeContext();

		Class<? extends Annotation> singletonClass = context.getScope();

		assertEquals(Singleton.class, singletonClass);
	}

	@Nested
	@DisplayName("without existing bean instances")
	class WithoutExistingBeans {

		private final SingletonScopeContext context = new SingletonScopeContext();

		@Test
		@DisplayName("get(Contextual<T>) returns null.")
		void get_returnsNull() {
			FakeBean instance = context.get(new FakeContextual());

			assertNull(instance);
		}

		@Test
		@DisplayName("get(Contextual<T>, CreationalContext<T>) creates and returns a new instance of the contextual.")
		void get_returnsNewInstanceOfContextual() {
			FakeBean instance = context.get(new FakeContextual(), new FakeCreationalContext());

			assertEquals(new FakeBean(0), instance);
		}
	}

	@Nested
	@DisplayName("with an existing instance")
	class WithExistingInstance {

		private final SingletonScopeContext context = new SingletonScopeContext();
		private final FakeContextual contextual = new FakeContextual();

		private FakeBean existingInstance;

		@BeforeEach
		void setup() {
			existingInstance = context.get(contextual, new FakeCreationalContext());
		}

		@Test
		@DisplayName("get(Contextual<T>) returns the existing instance.")
		void get_returnsExistingInstance() {
			FakeBean instance = context.get(contextual, new FakeCreationalContext());

			assertEquals(existingInstance, instance);
		}

		@Test
		@DisplayName("get(Contextual<T>, CreationalContext<T>) returns the existing instance.")
		void get_withContextualAndCreationalContext_returnsExistingInstance() {
			FakeBean instance = context.get(contextual, new FakeCreationalContext());

			assertEquals(existingInstance, instance);
		}
	}

	private record FakeBean(int count) {}

	private static class FakeContextual implements Contextual<FakeBean> {
		private final AtomicInteger count = new AtomicInteger();

		@Override
		public FakeBean create(CreationalContext<FakeBean> creationalContext) {
			return new FakeBean(count.getAndIncrement());
		}

		@Override
		public void destroy(FakeBean instance, CreationalContext<FakeBean> creationalContext) {
			throw new UnsupportedOperationException();
		}
	}

	private static final class FakeCreationalContext implements CreationalContext<FakeBean> {
		@Override
		public void push(FakeBean incompleteInstance) {
		}

		@Override
		public void release() {
		}
	}
}