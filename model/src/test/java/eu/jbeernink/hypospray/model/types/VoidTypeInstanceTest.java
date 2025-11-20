package eu.jbeernink.hypospray.model.types;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VoidTypeInstance")
class VoidTypeInstanceTest {

	@Test
	@DisplayName("descriptorString() returns V.")
	void descriptorString_returnsV() {
		String descriptorString = VoidTypeInstance.VOID.descriptorString();

		assertEquals("V", descriptorString);
	}

	@Test
	@DisplayName("isAssignableFrom() returns false.")
	void isAssignableFrom_returnsFalse() {
		boolean isAssignableFrom = VoidTypeInstance.VOID.isAssignableFrom(VoidTypeInstance.VOID);

		assertFalse(isAssignableFrom);
	}
}