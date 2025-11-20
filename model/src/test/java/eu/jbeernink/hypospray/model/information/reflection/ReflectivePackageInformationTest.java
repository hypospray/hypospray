package eu.jbeernink.hypospray.model.information.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.lang.model.AnnotationInfo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.testing.annotations.EmptyClassInAnnotatedPackage;
import eu.jbeernink.hypospray.model.testing.noannotations.EmptyClass;

@DisplayName("ReflectivePackageInformation")
class ReflectivePackageInformationTest {

	// TODO re-enable disabled tests once implementation is complete.
	@Nested
	@DisplayName("with package without annotations")
	class WithPackageWithoutAnnotations {

		private final ReflectivePackageInformation packageInformation =
				new ReflectivePackageInformation(EmptyClass.class.getPackage());

		@Test
		@DisplayName("name() returns the name of the package.")
		void name_returnsPackageName() {
			String name = packageInformation.name();

			assertEquals("eu.jbeernink.hypospray.model.testing.noannotations", name);
		}

		@Test
		@DisplayName("annotations() returns an empty list.")
		void annotations_returnsEmptyList() {
			Collection<AnnotationInfo> annotations = packageInformation.annotations();

			assertEquals(List.of(), annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns an empty list.")
		void annotationInformation_returnsEmptyList() {
			List<AnnotationInformation> annotations = packageInformation.annotationInformation();

			assertEquals(List.of(), annotations);
		}
	}

	@Nested
	@DisplayName("with annotated package")
	class WithAnnotatedPackage {

		private final ReflectivePackageInformation packageInformation =
				new ReflectivePackageInformation(EmptyClassInAnnotatedPackage.class.getPackage());

		@Test
		@DisplayName("annotations() returns list of annotations.")
		void annotations_returnsEmptyList() {
			Collection<AnnotationInfo> annotations = packageInformation.annotations();

			List<AnnotationInformation> expectedAnnotations =
					List.of(new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Vetoed.class), Map.of()));
			assertEquals(expectedAnnotations, annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns an empty list.")
		void annotationInformation_returnsEmptyList() {
			List<AnnotationInformation> annotations = packageInformation.annotationInformation();

			List<AnnotationInformation> expectedAnnotations =
					List.of(new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Vetoed.class), Map.of()));
			assertEquals(expectedAnnotations, annotations);
		}
	}
}