package eu.jbeernink.hypospray.core.invoke;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InstanceInvoker")
class InstanceInvokerTest {

	private InstanceInvoker<TestClass, String> instanceInvoker;
	private TestClass testClass;

	@BeforeEach
	void setup() {
		testClass = new TestClass();
		instanceInvoker =
				new InstanceInvoker<>(testClass, (instance, args) -> instance.method((String) args[0], (String) args[1]));
	}

	@Test
	@DisplayName("invoke(T, Object[]) calls the method on the given instance.")
	void invoke_callsMethodOnGivenInstance() throws Exception {
		var _ = instanceInvoker.invoke(null, new Object[]{"a", "b"});

		assertEquals(List.of(new Invocation("a", "b")), testClass.invocations);
	}

	@Test
	@DisplayName("invoke(T, Object[]) returns the result of the method call.")
	void invoke_returnsResultOfMethodCall() throws Exception {
		String result = instanceInvoker.invoke(null, new Object[]{"foo", "bar"});

		assertEquals("foo-bar", result);
	}

	@Test
	@DisplayName("invoke(T, Object[]) when the invoked method throws an exception, rethrows that exception.")
	void invoke_withMethodThatThrows_rethrowsExceptionFromMethod() {
		var _ =
				assertThrows(NullPointerException.class, () -> instanceInvoker.invoke(null, new Object[]{null, null}));
	}

	private record Invocation(String a, String b) {}

	private static class TestClass {
		private final List<Invocation> invocations = new ArrayList<>();

		public String method(String a, String b) {
			requireNonNull(a);
			invocations.add(new Invocation(a, b));

			return String.format("%s-%s", a, b);
		}
	}
}