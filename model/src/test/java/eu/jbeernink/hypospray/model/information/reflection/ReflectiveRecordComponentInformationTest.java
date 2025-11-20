package eu.jbeernink.hypospray.model.information.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.types.TypeInstance;

@DisplayName("ReflectiveRecordComponentInformation")
class ReflectiveRecordComponentInformationTest {

	private record TestRecord(String a) {}

	private ReflectiveRecordComponentInformation<TestRecord> information;

	@BeforeEach
	void setUp() {
		information = new ReflectiveRecordComponentInformation<>(new ReflectiveClassInformation<>(TestRecord.class),
				TestRecord.class.getRecordComponents()[0]);
	}

	@Test
	@DisplayName("name() returns the name of the record component.")
	void name_returnsRecordComponentName() {
		String name = information.name();

		assertEquals("a", name);
	}

	@Test
	@DisplayName("type() returns a type instance for the record component.")
	void type_returnsRecordComponentType() {
		TypeInstance type = information.type();

		assertEquals(TypeFactory.getInstance().of(String.class), type);
	}

}