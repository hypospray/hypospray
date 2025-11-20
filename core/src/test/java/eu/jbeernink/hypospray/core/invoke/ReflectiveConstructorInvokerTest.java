package eu.jbeernink.hypospray.core.invoke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReflectiveConstructorInvoker")
public class ReflectiveConstructorInvokerTest {

	@Nested
	@DisplayName("with constructor with no arguments")
	class WithConstructorWithNoArguments {

		private ReflectiveConstructorInvoker<TestRecord> invoker;

		@BeforeEach
		void setup() throws Exception {
			invoker = new ReflectiveConstructorInvoker<>(TestRecord.class.getDeclaredConstructor());
		}

		@Test
		@DisplayName("invoke(Void, Object[]) with null arguments, returns instance of expected class.")
		void invoke_withNullArgumentArray_returnsInstanceOfExpectedClass() throws Exception {
			TestRecord testRecord = invoker.invoke(null, null);

			assertEquals(new TestRecord(), testRecord);
		}

		@Test
		@DisplayName("invoke(Void, Object[]) with empty array of arguments, returns instance of expected class.")
		void invoke_withEmptyArgumentArray_returnsInstanceOfExpectedClass() throws Exception {
			TestRecord testRecord = invoker.invoke(null, new Object[]{});

			assertEquals(new TestRecord(), testRecord);
		}

		@Test
		@DisplayName("invoke(Void, Object[] with non-empty array of arguments, throws IllegalArgumentException.")
		void invoke_withNonEmptyArgumentArray_throwsSomething() {
			var exception = assertThrows(IllegalArgumentException.class, () -> invoker.invoke(null, new Object[]{"test"}));

			assertEquals("wrong number of arguments: 1 expected: 0", exception.getMessage());
		}

		record TestRecord() {}
	}

	@Nested
	@DisplayName("with constructor with arguments")
	class WithConstructorWithArguments {
		private ReflectiveConstructorInvoker<TestRecord> invoker;

		@BeforeEach
		void setup() throws Exception {
			invoker = new ReflectiveConstructorInvoker<>(TestRecord.class.getDeclaredConstructor(String.class, int.class));
		}

		@Test
		@DisplayName("invoke(Void, Object[]) with null arguments, throws IllegalArgumentException.")
		void invoke_withNullArgumentArray_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class, () -> invoker.invoke(null, null));

			assertEquals("wrong number of arguments: 0 expected: 2", exception.getMessage());
		}

		@Test
		@DisplayName("invoke(Void, Object[]) with empty array of arguments, throws IllegalArgumentException.")
		void invoke_withEmptyArgumentArray_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class, () -> invoker.invoke(null, new Object[]{}));

			assertEquals("wrong number of arguments: 0 expected: 2", exception.getMessage());
		}

		@Test
		@DisplayName("invoke(Void, Object[] with non-empty array of arguments, returns expected instance.")
		void invoke_withNonEmptyArgumentArray_returnsExpectedInstance() throws Exception {
			TestRecord testRecord = invoker.invoke(null, new Object[]{"foo", 42});

			assertEquals(new TestRecord("foo", 42), testRecord);
		}

		@Test
		@DisplayName("invoke(Void, Object[] with incorrect number of arguments, throws IllegalArgumentException.")
		void invoke_withNonEmptyArgumentArray_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class, () -> invoker.invoke(null, new Object[]{"foo"}));

			assertEquals("wrong number of arguments: 1 expected: 2", exception.getMessage());
		}

		@Test
		@DisplayName("invoke(Void, Object[] with incorrect type of arguments, throws IllegalArgumentException.")
		void invoke_withIncorrectArgumentType_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class, () -> invoker.invoke(null, new Object[]{1, 2}));

			assertEquals("argument type mismatch", exception.getMessage());
		}

		record TestRecord(String s, int i) {}
	}

	@Nested
	@DisplayName("with a private constructor")
	class WithPrivateConstructor {

		private ReflectiveConstructorInvoker<ClassWithPrivateConstructor> constructorInvoker;

		@BeforeEach
		void setup() throws Exception {
			Constructor<ClassWithPrivateConstructor> constructor =
					ClassWithPrivateConstructor.class.getDeclaredConstructor();
			constructorInvoker = new ReflectiveConstructorInvoker<>(constructor);
		}

		@Test
		@DisplayName("invoke(Void, Object[]) calls the private constructor correctly.")
		void invoke_callsPrivateConstructor() throws Exception {
			var instance = constructorInvoker.invoke(null, null);

			assertInstanceOf(ClassWithPrivateConstructor.class, instance);
		}

		private static final class ClassWithPrivateConstructor {
			private ClassWithPrivateConstructor() {
			}
		}
	}
}
