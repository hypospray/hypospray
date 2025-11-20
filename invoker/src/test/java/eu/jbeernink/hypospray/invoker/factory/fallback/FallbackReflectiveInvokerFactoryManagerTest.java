package eu.jbeernink.hypospray.invoker.factory.fallback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.enterprise.invoke.Invoker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactory;

@DisplayName("FallbackReflectiveInvokerFactoryManager")
class FallbackReflectiveInvokerFactoryManagerTest {

	private final FallbackReflectiveInvokerFactoryManager manager = new FallbackReflectiveInvokerFactoryManager();

	@Test
	@DisplayName(
			"getInvokerFactory(String className) with a known class name, returns an invoker factory that can generate invokers for a method on that class.")
	void getInvokerFactory_withKnownClassName_returnsInvokerFactoryWhichCanCallMethodsForClass() throws Exception {
		@SuppressWarnings("unchecked") InvokerFactory<String> invokerFactory =
				(InvokerFactory<String>) manager.getInvokerFactory("java.lang.String");

		Invoker<String, Object> invoker = invokerFactory.create("equals[(Ljava/lang/Object;)Z]");
		Object invoke = invoker.invoke("foo", new Object[]{"bar"});
		assertEquals(Boolean.FALSE, invoke);
	}

	@Test
	@DisplayName(
			"getInvokerFactory(String className) with a known class name, returns an invoker factory that can generate invokers for a constructor on that class.")
	void getInvokerFactory_withKnownClassName_returnsInvokerFactoryWhichCanCallConstructorsForClass() throws Exception {
		@SuppressWarnings("unchecked") InvokerFactory<String> invokerFactory =
				(InvokerFactory<String>) manager.getInvokerFactory("java.lang.String");

		Invoker<?, Object> invoker = invokerFactory.create("new[([C)Ljava/lang/String;]");
		Object invoke = invoker.invoke(null, new Object[]{new char[]{'d', 'i'}});
		assertEquals("di", invoke);
	}

	@Test
	@DisplayName("getInvokerFactory(String className) with an unknown class name, throws IllegalArgumentException.")
	void getInvokerFactory_withUnknownClassName_throwsIllegalArgumentException() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> manager.getInvokerFactory("fake.java.lang.String"));
	}
}