package eu.jbeernink.hypospray.core.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InjectionPointProducer")
class InjectionPointProducerTest {

	@Test
	@DisplayName("produceInjectionPoint() without setting an injection point returns null.")
	void produceInjectionPoint_withoutSettingAnInjectionPoint_returnsNull() {
		var exception =
				assertThrows(IllegalStateException.class, InjectionPointProducer::produceInjectionPoint);

		assertEquals("No current injection point found.", exception.getMessage());
	}

	@Test
	@DisplayName("produceInjectionPoint() after a call to withInjectionPoint throws IllegalStateException.")
	void produceInjectionPoint_afterCallToWithInjectionPoint_throwsIllegalStateException() {
		var expectedInjectionPoint = new FakeInjectionPoint();
		InjectionPointProducer.withInjectionPoint(expectedInjectionPoint, () -> null);

		var exception =
				assertThrows(IllegalStateException.class, InjectionPointProducer::produceInjectionPoint);

		assertEquals("No current injection point found.", exception.getMessage());
	}

	@Test
	@DisplayName("produceInjectionPoint() with an injection point set, returns that injection point.")
	void produceInjectionPoint_withSettingInjectionPoint_returnsInjectionPoint() {
		var expectedInjectionPoint = new FakeInjectionPoint();

		InjectionPoint injectionPoint = InjectionPointProducer.withInjectionPoint(expectedInjectionPoint,
				InjectionPointProducer::produceInjectionPoint);

		assertSame(expectedInjectionPoint, injectionPoint);
	}

	private static final class FakeInjectionPoint implements InjectionPoint {

		@Override
		public Type getType() {
			return Object.class;
		}

		@Override
		public Set<Annotation> getQualifiers() {
			return null;
		}

		@Override
		public Bean<?> getBean() {
			return null;
		}

		@Override
		public Member getMember() {
			return null;
		}

		@Override
		public Annotated getAnnotated() {
			return null;
		}

		@Override
		public boolean isDelegate() {
			return false;
		}

		@Override
		public boolean isTransient() {
			return false;
		}
	}

	private static class FakeCreationalContext<T> implements CreationalContext<T> {

		@Override
		public void push(T incompleteInstance) {

		}

		@Override
		public void release() {

		}
	}
}