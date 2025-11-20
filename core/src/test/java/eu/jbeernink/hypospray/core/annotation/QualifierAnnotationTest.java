package eu.jbeernink.hypospray.core.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Named;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.annotation.IntegerValue;
import eu.jbeernink.hypospray.model.annotation.StringValue;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@Named("QualifierAnnotation")
public class QualifierAnnotationTest {

	@Test
	@DisplayName(
			"new(ClassInformation, Map<String, AnnotationMemberValue>) with a non-annotation class, throws IllegalArgumentException.")
	void new_withNonAnnotationClass_throwsIllegalArgumentException() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> new QualifierAnnotation(new ReflectiveClassInformation<>(Object.class), Map.of()));

		assertEquals("java.lang.Object is not an annotation.", exception.getMessage());
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	@DisplayName(
			"new(ClassInformation, Map<String, AnnotationMemberValue>) with a null class, throws NullPointerException.")
	void new_withNullClass_throwsNullPointerException() {
		var _ = assertThrows(NullPointerException.class, () -> new QualifierAnnotation(null, Map.of()));
	}

	@Test
	@SuppressWarnings("DataFlowIssue")
	@DisplayName(
			"new(ClassInformation, Map<String, AnnotationMemberValue>) with a null map, throws NullPointerException.")
	void new_withNullMap_throwsNullPointerException() {
		var _ = assertThrows(NullPointerException.class,
				() -> new QualifierAnnotation(new ReflectiveClassInformation<>(Named.class), null));
	}

	@Test
	@DisplayName(
			"new(ClassInformation, Map<String, AnnotationMemberValue>) makes a defensive copy of the map of annotation members.")
	void new_makesDefensiveCopyOfAnnotationMemberMap() {
		var annotationMembers = new HashMap<String, AnnotationMemberValue>();
		annotationMembers.put("test", new IntegerValue(10));

		var qualifierAnnotation =
				new QualifierAnnotation(new ReflectiveClassInformation<>(Named.class), annotationMembers);

		assertAll(() -> assertEquals(annotationMembers, qualifierAnnotation.bindingMembers()),
				() -> assertNotSame(annotationMembers, qualifierAnnotation.bindingMembers()));
	}

	@Retention(RUNTIME)
	public @interface MyAnnotation {
		String value();
		@Nonbinding int number();
		int number2() default 1;
	}

	@Test
	@DisplayName("of(AnnotationInformation) creates a qualifier annotation with the correct class information.")
	void of_createsInstanceWithCorrectClassInformation() {
		@MyAnnotation(value = "name", number = 12) class MyClass {}
		var annotation = new ReflectiveAnnotationInformation<>(MyClass.class.getAnnotation(MyAnnotation.class));

		var qualifierAnnotation = QualifierAnnotation.of(annotation);

		assertEquals(new ReflectiveClassInformation<>(MyAnnotation.class), qualifierAnnotation.annotation());
	}

	@Test
	@DisplayName("of(AnnotationInformation) creates qualifier annotation with only the binding fields set.")
	void of_createsInstanceWithBindingFields() {
		@MyAnnotation(value = "name", number = 1, number2 = 5) class MyClass {}
		var annotation = new ReflectiveAnnotationInformation<>(MyClass.class.getAnnotation(MyAnnotation.class));

		var qualifierAnnotation = QualifierAnnotation.of(annotation);

		var expectedBindingMembers = Map.of("value", new StringValue("name"), "number2", new IntegerValue(5));
		assertEquals(expectedBindingMembers, qualifierAnnotation.bindingMembers());
	}

	@Test
	@DisplayName("of(AnnotationInformation) creates a qualifier annotation with unset fields set to their default value.")
	void of_createsInstanceWithUnsetFieldsSetToDefaultValue() {
		@MyAnnotation(value = "name", number = 1) class MyClass {}
		var annotation = new ReflectiveAnnotationInformation<>(MyClass.class.getAnnotation(MyAnnotation.class));

		var qualifierAnnotation = QualifierAnnotation.of(annotation);

		var expectedValue = new IntegerValue(1);
		assertEquals(expectedValue, qualifierAnnotation.bindingMembers().get("number2"));
	}
}