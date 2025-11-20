package eu.jbeernink.hypospray.model.information.synthetic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.inject.literal.NamedLiteral;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectivePackageInformation;

@DisplayName("SyntheticPackageInformation")
class SyntheticPackageInformationTest {

	@Nested
	@DisplayName("equals(Object)")
	class Equals {

		@Test
		@DisplayName("with an object that does not implement PackageInformation, returns false.")
		void equals_withNonPackageInformationObject_returnsFalse() {
			var packageInformation = new SyntheticPackageInformation("eu.jbeernink.hypospray.test", List.of());

			boolean isEqual = packageInformation.equals(new Object());

			assertFalse(isEqual);
		}

		@Test
		@DisplayName("with a package with a different name, returns false.")
		void equals_withDifferentPackageName_returnsFalse() {
			var packageInformation = new SyntheticPackageInformation("eu.jbeernink.hypospray.test", List.of());

			boolean isEqual =
					packageInformation.equals(new SyntheticPackageInformation("eu.jbeernink.hypospray.test.model", List.of()));

			assertFalse(isEqual);
		}

		@Test
		@DisplayName("with a package with the same name, returns true.")
		void equals_withSamePackageName_returnsTrue() {
			var packageInformation = new SyntheticPackageInformation("eu.jbeernink.hypospray.test", List.of());

			boolean isEqual =
					packageInformation.equals(new SyntheticPackageInformation("eu.jbeernink.hypospray.test", List.of()));

			assertTrue(isEqual);
		}

		@Test
		@DisplayName("with a package with the same name, but different annotations, returns true.")
		void equals_withSamePackageNameAndDifferentAnnotations_returnsTrue() {
			var packageInformation = new SyntheticPackageInformation("eu.jbeernink.hypospray.test", List.of(new ReflectiveAnnotationInformation<>(
					NamedLiteral.of("test"))));

			boolean isEqual = packageInformation.equals(new SyntheticPackageInformation("eu.jbeernink.hypospray.test", List.of()));

			assertTrue(isEqual);
		}

		@Test
		@DisplayName("with reflective package information for the same package, returns true.")
		void equals_withReflectivePackageInformationForSamePackage_returnsTrue() {
			var packageInformation = new SyntheticPackageInformation("eu.jbeernink.hypospray.model.information.synthetic", List.of());

			boolean isEqual = packageInformation.equals(
					new ReflectivePackageInformation(SyntheticPackageInformationTest.class.getPackage()));

			assertTrue(isEqual);
		}
	}

	@Test
	@DisplayName("hashCode() returns the expected hash code.")
	void hashCode_returnsExpectedHashCode() {
		var packageName = "eu.jbeernink.hypospray.test";
		var packageInformation = new SyntheticPackageInformation(packageName, List.of());

		int hashCode = packageInformation.hashCode();

		int expectedHashCode = Objects.hash(packageName);
		assertEquals(expectedHashCode, hashCode);
	}
}