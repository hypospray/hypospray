package eu.jbeernink.hypospray.core.invoke;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReflectiveMethodInvoker")
class ReflectiveMethodInvokerTest {

	private TestClass testClass;

	@BeforeEach
	void setup() {
		testClass = new TestClass();
	}

	@Nested
	@DisplayName("with public method method")
	class WithPublicMethod {

		private ReflectiveMethodInvoker<TestClass, String> invoker;

		@BeforeEach
		void setup() throws Exception {
			Method publicMethod = TestClass.class.getMethod("publicMethod", String.class, String.class, String.class);
			invoker = new ReflectiveMethodInvoker<>(publicMethod);
		}

		@Test
		@DisplayName("invoke(T, Object[]) calls the method on the given instance.")
		void invoke_callsMethodOnGivenInstance() throws Exception {
			var _ = invoker.invoke(testClass, new Object[]{"foo", "+", "bar"});

			assertEquals(List.of(new PublicMethodInvocation("foo", "+", "bar")), testClass.invocations);
		}

		@Test
		@DisplayName("invoke(T, Object[]) returns the result from the invoked method.")
		void invoke_returnsTheResult() throws Exception {
			String result = invoker.invoke(testClass, new Object[]{"bar", "-", "foo"});

			assertEquals("bar-foo", result);
		}

		@Test
		@DisplayName("invoke(T, Object[]) rethrows any exceptions thrown by the invoked method.")
		void invoke_rethrowsExceptionsThrownByInvokedMethod() {
			assertThrows(NullPointerException.class, () -> invoker.invoke(testClass, new Object[]{null, null, null}));
		}
	}

	@Nested
	@DisplayName("with private method method")
	class WithPrivateMethod {

		private ReflectiveMethodInvoker<TestClass, String> invoker;

		@BeforeEach
		void setup() throws Exception {
			Method privateMethod = TestClass.class.getDeclaredMethod("privateMethod", String.class, String.class);
			invoker = new ReflectiveMethodInvoker<>(privateMethod);
		}

		@Test
		@DisplayName("invoke(T, Object[]) calls the method on the given instance.")
		void invoke_callsMethodOnGivenInstance() throws Exception {
			var _ = invoker.invoke(testClass, new Object[]{"foo", "bar"});

			assertEquals(List.of(new PrivateMethodInvocation("foo", "bar")), testClass.invocations);
		}

		@Test
		@DisplayName("invoke(T, Object[]) returns the result from the invoked method.")
		void invoke_returnsTheResult() throws Exception {
			String result = invoker.invoke(testClass, new Object[]{"bar", "foo"});

			assertEquals("bar+foo", result);
		}

		@Test
		@DisplayName("invoke(T, Object[]) rethrows any exceptions thrown by the invoked method.")
		void invoke_rethrowsExceptionsThrownByInvokedMethod() {
			assertThrows(NullPointerException.class, () -> invoker.invoke(testClass, new Object[]{null, null}));
		}
	}


	private sealed interface Invocation {}

	private record PrivateMethodInvocation(String a, String b) implements Invocation {}

	private record PublicMethodInvocation(String a, String b, String c) implements Invocation {}

	public static class TestClass {

		final List<Invocation> invocations = new ArrayList<>();

		public String publicMethod(String a, String b, String c) {
			requireNonNull(a);
			invocations.add(new PublicMethodInvocation(a, b, c));

			return String.format("%s%s%s", a, b, c);
		}

		private String privateMethod(String a, String b) {
			requireNonNull(a);
			invocations.add(new PrivateMethodInvocation(a, b));

			return String.format("%s+%s", a, b);
		}
	}
}