package eu.jbeernink.hypospray.model.information.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.enterprise.inject.spi.DeploymentException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("DefaultClassInformationSource")
class DefaultClassInformationSourceTest {

	private final DefaultClassInformationSource classInformationSource = new DefaultClassInformationSource();

	@Test
	@DisplayName("getClassInformation(String) with a known class name, returns the class information.")
	void getClassInformation_withString_withKnownClassName_returnsClassInformation() {
		ClassInformation<?> classInformation = classInformationSource.getClassInformation("java.lang.String");

		var expectedClassInformation = new ReflectiveClassInformation<>(String.class);
		assertEquals(expectedClassInformation, classInformation);
	}

	@Test
	@DisplayName("getClassInformation(String) with an unknown class name, throws DeploymentException.")
	void getClassInformation_withString_withUnknownClassName_throwsDeploymentException() {
		var exception = assertThrows(DeploymentException.class,
				() -> classInformationSource.getClassInformation("this.is.not.a.real.class.name"));

		Assertions.assertEquals("No class with name this.is.not.a.real.class.name can be found.", exception.getMessage());
	}

	@Test
	@DisplayName("getClassInformation(Class<T>) returns the class information.")
	void getClassInformation_returnsClassInformation() {
		ClassInformation<?> classInformation = classInformationSource.getClassInformation(Integer.class);

		var expectedClassInformation = new ReflectiveClassInformation<>(Integer.class);
		assertEquals(expectedClassInformation, classInformation);
	}
}