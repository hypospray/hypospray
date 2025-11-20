package eu.jbeernink.hypospray.model.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LateReference<E>")
class LateReferenceTest {

	private LateReference<Object> reference;

	@BeforeEach
	void setup() {
		reference = new LateReference<>();
	}

	@Nested
	@DisplayName("without a value set")
	class WithoutValue {

		@Test
		@DisplayName("setValue(E) sets the value")
		void setValue_setsValue() {
			var value = "";
			reference.setValue(value);

			assertEquals(value, reference.get());
		}

		@Test
		@DisplayName("get() throws IllegalStateException.")
		void get_throwsIllegalStateException() {
			var exception = assertThrows(IllegalStateException.class, () -> reference.get());

			assertEquals("Attempt to retrieve a reference before a reference was set.", exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with a value set")
	class WithValue {

		final String value = "abc";

		@BeforeEach
		void setup() {
			reference.setValue(value);
		}

		@Test
		@DisplayName("get() returns the value.")
		void get_returnsValue() {
			Object returnedValue = reference.get();

			assertEquals(value, returnedValue);
		}

		@Test
		@DisplayName("setValue(E) throws IllegalStateException.")
		void setValue_throwsIllegalStateException() {
			var exception = assertThrows(IllegalStateException.class, () -> reference.setValue("foo"));

			assertEquals("Reference has already been set to a value.", exception.getMessage());
		}
	}
}