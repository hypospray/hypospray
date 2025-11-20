package eu.jbeernink.hypospray.model.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("WildcardTypeInstance")
class WildcardTypeInstanceTest {

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with an array type, returns false")
	void isAssignableFrom_withArrayType_returnsFalse() {
		var wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.ofArray(typeFactory.ofObject(), 1));

		assertFalse(isAssignableFrom);
	}

	@Nested
	@DisplayName("without bounds")
	class WithoutBounds {

		private final WildcardTypeInstance wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with a class type, returns false.")
		void isAssignableFrom_withClassType_returnsFalse() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.of(List.class));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with a ParameterizedType, returns false.")
		void isAssignableFrom_withParameterizedType_returnsFalse() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("withTypeAnnotations(List<AnnotationInformation>) creates a copy with the new annotations set.")
		void withTypeAnnotations_returnsCopyWithAnnotations() {
			List<AnnotationInformation> annotations = List.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("test")));

			WildcardTypeInstance copy = wildcard.withTypeAnnotations(annotations);

			assertEquals(new WildcardTypeInstance(List.of(), List.of(), annotations), copy);
		}

		@Test
		@DisplayName("upperBound() returns null.")
		void upperBound_returnsNull() {
			TypeInstance upperBound = wildcard.upperBound();

			assertNull(upperBound);
		}

		@Test
		@DisplayName("lowerBound() returns null.")
		void lowerBound_returnsNull() {
			TypeInstance lowerBound = wildcard.lowerBound();

			assertNull(lowerBound);
		}
	}

	@Nested
	@DisplayName("with upper bound")
	class WithUpperBound {

		private final WildcardTypeInstance wildcard =
				new WildcardTypeInstance(List.of(TypeFactory.getInstance().parameterized(List.class, String.class)), List.of(), List.of());

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with a class type, returns false.")
		void isAssignableFrom_withClassType_returnsFalse() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.of(List.class));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with a ParameterizedType, returns false.")
		void isAssignableFrom_withParameterizedType_returnsFalse() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("upperBound() returns the upper bound.")
		void upperBound_returnsTheUpperBound() {
			TypeInstance upperBound = wildcard.upperBound();

			assertEquals(TypeFactory.getInstance().parameterized(List.class, String.class), upperBound);
		}

		@Test
		@DisplayName("lowerBound() returns null.")
		void lowerBound_returnsNull() {
			TypeInstance lowerBound = wildcard.lowerBound();

			assertNull(lowerBound);
		}
	}

	@Nested
	@DisplayName("with lower bound")
	class WithLowerBound {

		private final WildcardTypeInstance wildcard =
				new WildcardTypeInstance(List.of(), List.of(TypeFactory.getInstance().parameterized(List.class, String.class)), List.of());

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with a class type of a subclass of the lower bound, returns true.")
		void isAssignableFrom_withClassTypeThatIsASubclassOfLowerBound_returnsTrue() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.of(List.class));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName(
				"isAssignableFrom(TypeInstance) with a class type that is not a subclass of the lower bound, returns false. ")
		void isAssignableFrom_withClassTypeThatIsNotASubclassOfLowerBound_returnsFalse() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.of(String.class));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName(
				"isAssignableFrom(TypeInstance) with a ParameterizedType that is a subclass of the lower bound, returns true.")
		void isAssignableFrom_withParameterizedTypeOfLowerBound_returnsTrue() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName(
				"isAssignableFrom(TypeInstance) with a ParameterizedType that is not a sublcass of the lower bound, returns false.")
		void isAssignableFrom_withParameterizedTypeThatIsNotASubclassOfLowerBound_returnsFalse() {
			boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.parameterized(Optional.class, String.class));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("upperBound() returns null.")
		void upperBound_returnsNull() {
			TypeInstance upperBound = wildcard.upperBound();

			assertNull(upperBound);
		}

		@Test
		@DisplayName("lowerBound() returns the lower bound.")
		void lowerBound_returnsTheLowerBound() {
			TypeInstance lowerBound = wildcard.lowerBound();

			assertEquals(TypeFactory.getInstance().parameterized(List.class, String.class), lowerBound);
		}
	}

	@Nested
	@DisplayName("with type annotations")
	class WithTypeAnnotations {
		private final List<AnnotationInformation> typeAnnotations =
				List.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("test")));
		private final WildcardTypeInstance wildcard = new WildcardTypeInstance(List.of(), List.of(), typeAnnotations);

		@Test
		@DisplayName("annotations() returns the type annotations.")
		void annotations_returnsTypeAnnotations() {
			List<AnnotationInfo> annotations = wildcard.annotations();

			assertEquals(typeAnnotations, annotations);
		}
	}
	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a ParameterizedType, returns false.")
	void isAssignableFrom_withParameterizedType_returnsFalse() {
		var wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

		assertFalse(isAssignableFrom);
	}

	@DisplayName("isAssignableFrom(TypeInstance)")
	@ParameterizedTest(name = "with PrimitiveTypeInstance.{0}, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withPrimitiveType_returnsFalse(PrimitiveKind primitiveKind) {
		var wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		boolean isAssignableFrom = wildcard.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with type variable, returns false.")
	void isAssignableFrom_withTypeVariable_returnsFalse() {
		var wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		boolean isAssignableFrom = wildcard.isAssignableFrom(
				new DeclaredTypeVariableInstance(new ReflectiveClassInformation<>(List.class), "E", List.of(), List.of()));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with void type, returns false.")
	void isAssignableFrom_withVoidType_returnsFalse() {
		var wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.ofVoid());

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with wildcard type, returns false.")
	void isAssignableFrom_withWildcardType_returnsFalse() {
		var wildcard = new WildcardTypeInstance(List.of(), List.of(), List.of());

		boolean isAssignableFrom = wildcard.isAssignableFrom(typeFactory.wildcardUnbounded());

		assertFalse(isAssignableFrom);
	}
}