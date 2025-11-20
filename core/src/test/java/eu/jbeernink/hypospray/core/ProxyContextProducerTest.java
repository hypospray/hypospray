package eu.jbeernink.hypospray.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProxyContextProducer")
class ProxyContextProducerTest {


	@Test
	@DisplayName("produceInvokerFactory() with no active proxy context, throws IllegalStateException.")
	void producerInvokerFactory_withoutActiveProxyContext_throwsIllegalStateException() {
		var exception =
				assertThrows(IllegalStateException.class, ProxyContextProducer::produceInvokerFactory);

		assertEquals("No proxy context is currently active.", exception.getMessage());
	}

	@Test
	@DisplayName("produceInvokerFactory() with normal scoped proxy context, returns invoker factory.")
	void produceInvokerFactory_withNormalScopedProxyContext_throwsIllegalStateException() {
		var invokerFactory = new FakeInvokerFactory();

		var actualInvokerFactory = ProxyContextProducer.withProxyContext(invokerFactory, ProxyContextProducer::produceInvokerFactory);

		assertEquals(invokerFactory, actualInvokerFactory);
	}

	@Test
	@DisplayName("produceInvokerFactory() with non-normal scoped proxy context, returns invoker factory."
	)
	void produceInvokerFactory_withNonNormalScopedProxyContext_throwsIllegalStateException() {
		var invokerFactory = new FakeInvokerFactory();

		var actualInvokerFactory = ProxyContextProducer.withNonNormalScopedProxyContext(invokerFactory, null, "", ProxyContextProducer::produceInvokerFactory);

		assertEquals(invokerFactory, actualInvokerFactory);
	}


	private record FakeInvokerFactory() implements Function<String, Invoker<Void, Object>> {

		@Override
		public Invoker<Void, Object> apply(String s) {
			return (_, _) -> "";
		}
	}
}