package eu.jbeernink.hypospray.core.invoke;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NoopInvoker")
class NoopInvokerTest {

	@Test
	@DisplayName("invoke(T, Object[]) returns null")
	void invoke_returnsNull() throws Exception {
		var noopInvoker = new NoopInvoker<>();

		Object result = noopInvoker.invoke("null", new Object[]{});

		assertNull(result);
	}
}