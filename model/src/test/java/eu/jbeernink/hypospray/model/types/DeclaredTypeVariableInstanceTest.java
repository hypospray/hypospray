package eu.jbeernink.hypospray.model.types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("DeclaredTypeVariableInstance")
class DeclaredTypeVariableInstanceTest {

	private final ClassInformation<DeclaredTypeVariableInstanceTest> owner =
			new ReflectiveClassInformation<>(DeclaredTypeVariableInstanceTest.class);
	private final TypeFactory typeFactory = TypeFactory.getInstance();

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with array type, returns false.")
	void isAssignableFrom_withArrayType_returnsFalse() {
		var typeVariable = new DeclaredTypeVariableInstance(owner, "E", List.of(), List.of());

		boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.ofArray(typeFactory.ofObject(), 1));

		assertFalse(isAssignableFrom);
	}

	@Nested
	@DisplayName("without type bounds")
	class WithoutTypeBounds {

		private final DeclaredTypeVariableInstance typeVariable =
				new DeclaredTypeVariableInstance(owner, "E", List.of(), List.of());

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with class type, returns true.")
		void isAssignableFrom_withClassType_returnsTrue() {
			boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.ofObject());

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with parameterized type, returns true")
		void isAssignableFrom_withParameterizedType_returnsTrue() {
			boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

			assertTrue(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with type bounds")
	class WithTypeBounds {

		private final DeclaredTypeVariableInstance typeVariable =
				new DeclaredTypeVariableInstance(owner, "E", List.of(typeFactory.of(List.class)), List.of());

		@Test
		@DisplayName(
				"isAssignableFrom(TypeInstance) with class type that matches the bounds, returns true.")
		void isAssignableFrom_withClassTypeInstanceThatMatchesBounds_returnsTrue() {
			boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.of(ArrayList.class));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with a class type that does not subclass bounds, returns false.")
		void isAssignableFrom_withClassTypeThatDoesMatchBounds_returnsFalse() {
			boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.ofObject());

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with parameterized type that matches bounds, returns true.")
		void isAssignableFrom_withParameterizedTypeThatMatchesBounds_returnsTrue() {
			boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.parameterized(ArrayList.class, String.class));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with parameterized type that does not match bounds, returns false.")
		void isAssignableFrom_withParameterizedTypeThatDoesNotMatchBounds_returnsFalse() {
			boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.parameterized(Collection.class, String.class));

			assertFalse(isAssignableFrom);
		}
	}

	@DisplayName("isAssignableFrom(TypeInstance)")
	@ParameterizedTest(name = "with PrimitiveTypeInstance.{0}, returns false")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withPrimitiveType_returnsFalse(PrimitiveKind primitiveKind) {
		var typeVariable = new DeclaredTypeVariableInstance(owner, "E", List.of(), List.of());

		boolean isAssignableFrom = typeVariable.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with type variable type, returns false.")
	void isAssignableFrom_withTypeVariableType_returnsFalse() {
		var typeVariable = new DeclaredTypeVariableInstance(owner, "E", List.of(), List.of());

		boolean isAssignableFrom = typeVariable.isAssignableFrom(typeVariable);

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with void type, returns false.")
	void isAssignableFrom_withVoidType_returnsFalse() {
		var typeVariable = new DeclaredTypeVariableInstance(owner, "E", List.of(), List.of());

		boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.ofVoid());

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with wildcard type, returns false.")
	void isAssignableFrom_withWildcardType_returnsFalse() {
		var typeVariable = new DeclaredTypeVariableInstance(owner, "E", List.of(), List.of());

		boolean isAssignableFrom = typeVariable.isAssignableFrom(typeFactory.wildcardUnbounded());

		assertFalse(isAssignableFrom);
	}
}