package eu.jbeernink.hypospray.model.information.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Named;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.annotation.StringValue;
import eu.jbeernink.hypospray.model.information.ClassInformation;

@DisplayName("ReflectiveAnnotationInformation")
@NullMarked
class ReflectiveAnnotationInformationTest {

	private final ReflectiveAnnotationInformation<Named> annotationInformation = new ReflectiveAnnotationInformation<>(
			NamedLiteral.of("test"));

	@Test
	@DisplayName("declaration() returns class information for the annotation.")
	void declaration_returnsAnnotationClassInformation() {
		ClassInformation<? extends Annotation> declaration = annotationInformation.declaration();

		var expectedDeclaration = new ReflectiveClassInformation<>(Named.class);
		assertEquals(expectedDeclaration, declaration);
	}

	@Test
	@DisplayName("memberValues() returns the member values of the annotation.")
	void memberValues_returnsAnnotationMemberValues() {
		Map<String, AnnotationMemberValue> members = annotationInformation.memberValues();

		var expectedMembers = Map.of("value", new StringValue("test"));
		assertEquals(expectedMembers, members);
	}

	@Test
	@DisplayName("members() returns the member values of the annotation.")
	void members_returnsAnnotationMemberValues() {
		Map<String, AnnotationMember> members = annotationInformation.members();

		var expectedMembers = Map.of("value", new StringValue("test"));
		assertEquals(expectedMembers, members);
	}

	@Test
	@DisplayName("member(String) with a known member name, returns the value for that member.")
	void member_withKnownMemberName_returnsMemberValue() {
		AnnotationMemberValue value = annotationInformation.member("value");

		assertEquals(new StringValue("test"), value);
	}

	@Test
	@DisplayName("member(String) with unknown member name, returns null.")
	void member_withUnknownMemberName_returnsNull() {
		AnnotationMemberValue value = annotationInformation.member("unknown");

		assertNull(value);
	}

	@Test
	@DisplayName("hasMember(String) with a known member name, returns true.")
	void hasMember_withKnownMemberName_returnsTrue() {
		boolean hasMember = annotationInformation.hasMember("value");

		assertTrue(hasMember);
	}

	@Test
	@DisplayName("hasMember(String) with an unknown member name, returns false.")
	void hasMember_withUnknownMemberName_returnsFalse() {
		boolean hasMember = annotationInformation.hasMember("unknown");

		assertFalse(hasMember);
	}

	@Test
	@DisplayName("isBindingMember(String) with a member that is not annotated with @Nonbinding, returns true.")
	void isBindingMember_withMemberWithoutNonBinding_returnsTrue() {
		boolean isBinding = annotationInformation.isBindingMember("value");

		assertTrue(isBinding);
	}

	@interface NonBindingAnnotation {
		@Nonbinding
		String foo();

		final class Literal extends AnnotationLiteral<NonBindingAnnotation> implements NonBindingAnnotation {

			@Override
			public String foo() {
				return "";
			}
		}
	}

	@Test
	@DisplayName("isBindingMember(String) with a member that is annotated with @Nonbinding, returns false.")
	void isBindingMember_withMemberWithNonbindin_returnsFalse() {
		var annotationInformation = new ReflectiveAnnotationInformation<>(new NonBindingAnnotation.Literal());

		boolean isBinding = annotationInformation.isBindingMember("foo");

		assertFalse(isBinding);
	}

	@Test
	@DisplayName("isBindingMember(String) with a member that does not exist, throws IllegalArgumentException.")
	void isBindingMember_withNonexistingMember_throwsIllegalArgumentException() {
		var exception =
				assertThrows(IllegalArgumentException.class, () -> annotationInformation.isBindingMember("bar"));

		assertEquals("Annotation jakarta.inject.Named has no member named 'bar'", exception.getMessage());
	}
}