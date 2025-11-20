package eu.jbeernink.hypospray.model;

import org.jboss.cdi.lang.model.tck.LangModelVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@Tag("tck")
@DisplayName("The CDI lang model TCK")
public class LangModelTckTest {

	@Test
	@DisplayName("passes.")
	void tck_passes() {
		var classInformation = new ReflectiveClassInformation<>(LangModelVerifier.class);

		LangModelVerifier.verify(classInformation);
	}
}
