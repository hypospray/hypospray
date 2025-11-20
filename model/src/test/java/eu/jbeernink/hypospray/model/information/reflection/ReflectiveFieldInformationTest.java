package eu.jbeernink.hypospray.model.information.reflection;

import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.annotation.StringValue;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.types.ParameterizedTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;

class ReflectiveFieldInformationTest {
	@SuppressWarnings("unused")
	private String nonFinalField;
	private final int finalField = 1;

	@Named("foo")
	private Object annotatedField;

	@Nested
	@DisplayName("with non-final field")
	class WithNonFinalField {

		private ReflectiveFieldInformation fieldInformation;

		@BeforeEach
		void setup() throws Exception {
			Field field = ReflectiveFieldInformationTest.class.getDeclaredField("nonFinalField");
			fieldInformation = new ReflectiveFieldInformation(field);
		}

		@Test
		@DisplayName("declaringClass() returns class information about the declaring class.")
		void declaringClass_returnsDeclaringClassInformation() {
			ClassInformation<?> classInformation = fieldInformation.declaringClass();

			var expectedClassInformation = new ReflectiveClassInformation<>(ReflectiveFieldInformationTest.class);
			assertEquals(expectedClassInformation, classInformation);
		}

		@Test
		@DisplayName("name() returns the field name.")
		void name_returnsFieldName() {
			String fieldName = fieldInformation.name();

			assertEquals("nonFinalField", fieldName);
		}

		@Test
		@DisplayName("type() returns the type of the field.")
		void type_returnsFieldType() {
			TypeInstance typeInstance = fieldInformation.type();

			var expectedTypeInstance = TypeFactory.getInstance().fromJavaType(String.class);
			assertEquals(expectedTypeInstance, typeInstance);
		}

		@Test
		@DisplayName("modifiers() returns the fields modifiers.")
		void modifiers_returnsFieldModifiers() {
			int modifiers = fieldInformation.modifiers();

			assertEquals(PRIVATE, modifiers);
		}

		@Test
		@DisplayName("annotationInformation() returns an empty list.")
		void annotationInformation_returnsEmptyList() {
			List<AnnotationInformation> annotations = fieldInformation.annotationInformation();

			assertEquals(List.of(), annotations);
		}

		@Test
		@DisplayName("annotations() returns an empty list.")
		void annotations_returnsEmptyList() {
			List<AnnotationInfo> annotations = fieldInformation.annotations();

			assertEquals(List.of(), annotations);
		}
	}

	@Nested
	@DisplayName("with final field")
	class WithFinalField {
		private ReflectiveFieldInformation fieldInformation;

		@BeforeEach
		void setup() throws Exception {
			Field field = ReflectiveFieldInformationTest.class.getDeclaredField("finalField");
			fieldInformation = new ReflectiveFieldInformation(field);
		}

		@Test
		@DisplayName("modifiers() returns the private and final modifiers.")
		void modifiers_returnsFieldModifiers() {
			int modifiers = fieldInformation.modifiers();

			assertEquals(PRIVATE | FINAL, modifiers);
		}
	}

	@Nested
	@DisplayName("with annotated field")
	class WithAnnotatedField {
		private ReflectiveFieldInformation fieldInformation;

		@BeforeEach
		void setup() throws Exception {
			Field field = ReflectiveFieldInformationTest.class.getDeclaredField("annotatedField");
			fieldInformation = new ReflectiveFieldInformation(field);
		}

		@Test
		@DisplayName("annotations() returns the annotations on the field.")
		void annotations_returnsFieldAnnotations() {
			List<AnnotationInfo> annotations = fieldInformation.annotations();

			var expectedAnnotations = List.of(
					new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Named.class),
							Map.of("value", new StringValue("foo"))));
			assertEquals(expectedAnnotations, annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns the annotations on the field.")
		void annotationInformation_returnsFieldAnnotations() {
			List<AnnotationInformation> annotations = fieldInformation.annotationInformation();

			var expectedAnnotations = List.of(
					new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Named.class),
							Map.of("value", new StringValue("foo"))));
			assertEquals(expectedAnnotations, annotations);
		}
	}

	@Nested
	@DisplayName("with a parameterized field")
	class WithParameterizedField {

		private List<String> field;

		private ReflectiveFieldInformation fieldInformation;

		@BeforeEach
		void setup() throws Exception {
			Field field = WithParameterizedField.class.getDeclaredField("field");
			fieldInformation = new ReflectiveFieldInformation(field);
		}

		@Test
		@DisplayName("type() returns the correct parameterized type.")
		void type_returnsParameterizedType() {
			TypeInstance type = fieldInformation.type();

			var expectedTypeInstance = new ParameterizedTypeInstance(TypeFactory.getInstance().of(List.class).asClass(),
					List.of(TypeFactory.getInstance().of(String.class)), List.of());
			assertEquals(expectedTypeInstance, type);
		}
	}
}