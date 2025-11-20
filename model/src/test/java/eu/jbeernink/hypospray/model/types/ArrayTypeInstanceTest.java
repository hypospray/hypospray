package eu.jbeernink.hypospray.model.types;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("ArrayTypeInstance")
class ArrayTypeInstanceTest {

	@Test
	@DisplayName("descriptorString() with single dimensional array, returns the type descriptor.")
	void descriptorString_withSingleDimensionalArray_returnsTypeDescriptor() {
		var arrayType = new ArrayTypeInstance(PrimitiveTypeInstance.INT, List.of());

		String descriptorString = arrayType.descriptorString();

		assertEquals("[I", descriptorString);
	}

	@Test
	@DisplayName("descriptorString() with multi-dimensional array, returns multi-dimensional type descriptor.")
	void descriptorString_withMultiDimensionalArray_returnsTypeDescriptor() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()), List.of());

		String descriptorString = arrayType.descriptorString();

		assertEquals("[[J", descriptorString);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a class type instance, returns false.")
	void isAssignableFrom_withClassTypeInstance_returnsFalse() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.INT, List.of()), List.of());

		boolean isAssignableFrom =
				arrayType.isAssignableFrom(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class)));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a primitive instance, returns false.")
	void isAssignableFrom_withPrimitiveInstance_returnsFalse() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()), List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(PrimitiveTypeInstance.INT);

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with parameterized type instance, returns false.")
	void isAssignableFrom_withParameterizedTypeInstance_returnsFalse() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()), List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(
				new ParameterizedTypeInstance(new ClassTypeInstance(new ReflectiveClassInformation<>(List.class)),
						List.of(new ReflectiveClassInformation<>(String.class).asType()), List.of()));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with wildcard instance, returns false.")
	void isAssignableFrom_withWildcardInstance_returnsFalse() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()), List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(TypeFactory.getInstance().wildcardUnbounded());

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with type variable, returns false.")
	void isAssignableFrom_withTypeVariableInstance_returnsFalse() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()), List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(
				new DeclaredTypeVariableInstance(new ReflectiveClassInformation<>(List.class), "X", List.of(), List.of()));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with void, returns false.")
	void isAssignableFrom_withVoidInstance_returnsFalse() {
		var arrayType = new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()), List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(VoidTypeInstance.VOID);

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with array type with same component type, returns true.")
	void isAssignableFrom_withArrayTypeWithSameComponentType_returnsTrue() {
		var arrayType = new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(new ArrayTypeInstance(PrimitiveTypeInstance.LONG, List.of()));

		assertTrue(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with array type with unrelated component type, returns false.")
	void isAssignableFrom_withArrayTypeWithUnrelatedComponentType_returnsFalse() {
		var arrayType =
				new ArrayTypeInstance(new ArrayTypeInstance(TypeFactory.getInstance().of(String.class), List.of()), List.of());

		boolean isAssignableFrom =
				arrayType.isAssignableFrom(new ArrayTypeInstance(TypeFactory.getInstance().of(List.class), List.of()));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a array type with subclass as component type, returns true.")
	void isAssignableFrom_withArrayTypeWithSubclassAsComponentType_returnsTrue() {
		var arrayType = new ArrayTypeInstance(TypeFactory.getInstance().of(CharSequence.class), List.of());

		boolean isAssignableFrom =
				arrayType.isAssignableFrom(new ArrayTypeInstance(TypeFactory.getInstance().of(String.class), List.of()));

		assertTrue(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with an array type with supertype as component type, returns false.")
	void isAssignableFrom_withArrayTypeWithSupertypeAsComponentType_returnsFalse() {
		var arrayType =
				new ArrayTypeInstance(new ArrayTypeInstance(TypeFactory.getInstance().of(String.class), List.of()), List.of());

		boolean isAssignableFrom = arrayType.isAssignableFrom(TypeFactory.getInstance().of(CharSequence.class));

		assertFalse(isAssignableFrom);
	}
}