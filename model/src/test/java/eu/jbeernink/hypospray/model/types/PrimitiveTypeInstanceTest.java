package eu.jbeernink.hypospray.model.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import java.util.List;

import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("PrimitiveTypeInstance")
class PrimitiveTypeInstanceTest {

	@Nested
	@DisplayName("BOOLEAN")
	class Boolean {
		private PrimitiveTypeInstance primitiveType = PrimitiveTypeInstance.BOOLEAN;

		@Test
		@DisplayName("descriptorString() returns Z.")
		void descriptorString_returnsZ() {
			String descriptorString = PrimitiveTypeInstance.BOOLEAN.descriptorString();

			assertEquals("Z", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Boolean.")
		void getBoxedClassName_returnsBoolean() {
			String boxedClassName = primitiveType.getBoxedClassName();

			assertEquals("java.lang.Boolean", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with BOOLEAN, returns true.")
		void isAssignableFrom_withBoolean_returnsTrue() {
			boolean isAssignableFrom = primitiveType.isAssignableFrom(PrimitiveTypeInstance.BOOLEAN);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "BOOLEAN", mode = EXCLUDE)
		void isAssignableFrom_withNonBooleanValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = primitiveType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with class type instance of java.lang.Boolean, returns true.")
		void isAssignableFrom_withBooleanClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = primitiveType.isAssignableFrom(TypeFactory.getInstance().of(java.lang.Boolean.class));

			assertTrue(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("BYTE")
	class Byte {

		private final PrimitiveTypeInstance byteType = PrimitiveTypeInstance.BYTE;

		@Test
		@DisplayName("descriptorString() returns B.")
		void descriptorString_returnsB() {
			String descriptorString = byteType.descriptorString();

			assertEquals("B", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Byte.")
		void getBoxedClassName_returnsByte() {
			String boxedClassName = byteType.getBoxedClassName();

			assertEquals("java.lang.Byte", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with BYTE, returns true.")
		void isAssignableFrom_withByte_returnsTrue() {
			boolean isAssignableFrom = byteType.isAssignableFrom(PrimitiveTypeInstance.BYTE);

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with class type instance of java.lang.Byte, returns true.")
		void isAssignableFrom_withByteClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = byteType.isAssignableFrom(TypeFactory.getInstance().of(java.lang.Byte.class));

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "BYTE", mode = EXCLUDE)
		void isAssignableFrom_withNonByteValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = byteType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("SHORT")
	class Short {
		private final PrimitiveTypeInstance shortType = PrimitiveTypeInstance.SHORT;

		@Test
		@DisplayName("descriptorString() returns S.")
		void descriptorString_returnsS() {
			String descriptorString = PrimitiveTypeInstance.SHORT.descriptorString();

			assertEquals("S", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Short.")
		void getBoxedClassName_returnsShort() {
			String boxedClassName = shortType.getBoxedClassName();

			assertEquals("java.lang.Short", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with SHORT, returns true.")
		void isAssignableFrom_withShort_returnsTrue() {
			boolean isAssignableFrom = shortType.isAssignableFrom(PrimitiveTypeInstance.SHORT);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "SHORT", mode = EXCLUDE)
		void isAssignableFrom_withDifferentPrimitiveType_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = shortType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom() with class type instance of java.lang.Short, returns true.")
		void isAssignableFrom_withShortClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = shortType.isAssignableFrom(TypeFactory.getInstance().of(java.lang.Short.class));

			assertTrue(isAssignableFrom);
		}

	}


	@Nested
	@DisplayName("INT")
	class Int {
		private final PrimitiveTypeInstance intType = PrimitiveTypeInstance.INT;

		@Test
		@DisplayName("descriptorString() returns I.")
		void descriptorString_returnsI() {
			String descriptorString = intType.descriptorString();

			assertEquals("I", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Integer.")
		void getBoxedClassName_returnsInteger() {
			String boxedClassName = intType.getBoxedClassName();

			assertEquals("java.lang.Integer", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with INT, returns true.")
		void isAssignableFrom_withInt_returnsTrue() {
			boolean isAssignableFrom = intType.isAssignableFrom(PrimitiveTypeInstance.INT);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "INT", mode = EXCLUDE)
		void isAssignableFrom_withNonIntValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = intType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with class type instance of java.lang.Integer, returns true.")
		void isAssignableFrom_withIntegerClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = intType.isAssignableFrom(TypeFactory.getInstance().of(Integer.class));

			assertTrue(isAssignableFrom);
		}
	}


	@Nested
	@DisplayName("LONG")
	class Long {

		private final PrimitiveTypeInstance longType = PrimitiveTypeInstance.LONG;

		@Test
		@DisplayName("descriptorString() returns J.")
		void descriptorString_returnsJ() {
			String descriptorString = longType.descriptorString();

			assertEquals("J", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Long.")
		void getBoxedClassName_returnsLong() {
			String boxedClassName = longType.getBoxedClassName();

			assertEquals("java.lang.Long", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with LONG, returns true")
		void isAssignableFrom_withLong_returnsTrue() {
			boolean isAssignableFrom = longType.isAssignableFrom(PrimitiveTypeInstance.LONG);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "LONG", mode = EXCLUDE)
		void isAssignableFrom_withNonLongValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = longType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance) with class type instance of java.lang.Long, returns true.")
		void isAssignableFrom_withLongClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = longType.isAssignableFrom(TypeFactory.getInstance().of(Long.class));

			assertTrue(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("FLOAT")
	class Float {

		private final PrimitiveTypeInstance floatType = PrimitiveTypeInstance.FLOAT;

		@Test
		@DisplayName("descriptorString() returns F.")
		void descriptorString_onFloat_returnsF() {
			String descriptorString = floatType.descriptorString();

			assertEquals("F", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Float.")
		void getBoxedClassName_returnsFloat() {
			String boxedClassName = floatType.getBoxedClassName();

			assertEquals("java.lang.Float", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with FLOAT, returns true.")
		void isAssignableFrom_withFloat_returnsTrue() {
			boolean isAssignableFrom = floatType.isAssignableFrom(PrimitiveTypeInstance.FLOAT);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "FLOAT", mode = EXCLUDE)
		void isAssignableFrom_withNonFloatValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = floatType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance) with class type instance for java.lang.Float, returns true.")
		void isAssignableFrom_withFloatClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = floatType.isAssignableFrom(TypeFactory.getInstance().of(Float.class));

			assertTrue(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("DOUBLE")
	class Double {

		private final PrimitiveTypeInstance doubleType = PrimitiveTypeInstance.DOUBLE;

		@Test
		@DisplayName("descriptorString() returns D.")
		void descriptorString_onDouble_returnsD() {
			String descriptorString = PrimitiveTypeInstance.DOUBLE.descriptorString();

			assertEquals("D", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Double.")
		void getBoxedClassName_returnsDouble() {
			String boxedClassName = doubleType.getBoxedClassName();

			assertEquals("java.lang.Double", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with DOUBLE, returns true.")
		void isAssignableFrom_withDouble_returnsTrue() {
			boolean isAssignableFrom = doubleType.isAssignableFrom(PrimitiveTypeInstance.DOUBLE);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false")
		@EnumSource(value = PrimitiveKind.class, names = "DOUBLE", mode = EXCLUDE)
		void isAssignableFrom_withNonDoubleValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = doubleType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("CHAR")
	class Char {

		private final PrimitiveTypeInstance charType = PrimitiveTypeInstance.CHAR;

		@Test
		@DisplayName("descriptorString() returns C.")
		void descriptorString_returnsC() {
			String descriptorString = PrimitiveTypeInstance.CHAR.descriptorString();

			assertEquals("C", descriptorString);
		}

		@Test
		@DisplayName("getBoxedClassName() returns java.lang.Character.")
		void getBoxedClassName_returnsCharacter() {
			String boxedClassName = charType.getBoxedClassName();

			assertEquals("java.lang.Character", boxedClassName);
		}

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with CHAR, returns true.")
		void isAssignableFrom_withChar_returnsTrue() {
			boolean isAssignableFrom = charType.isAssignableFrom(PrimitiveTypeInstance.CHAR);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "CHAR", mode = EXCLUDE)
		void isAssignableFrom_withNonCharValue_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = charType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance) with class type instance of java.lang.Character, returns true.")
		void isAssignableFrom_withCharClassTypeInstance_returnsTrue() {
			boolean isAssignableFrom = charType.isAssignableFrom(TypeFactory.getInstance().of(Character.class));

			assertTrue(isAssignableFrom);
		}
	}

	// Tests covering the same behaviour for all constants in PrimitiveTypeInstance.

	@ParameterizedTest(name = "{0}.isAssignableFrom(TypeInstance) with array type instance, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withArrayTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
		PrimitiveTypeInstance primitiveType = new PrimitiveTypeInstance(primitiveKind, List.of());
		boolean isAssignableFrom = primitiveType.isAssignableFrom(new ArrayTypeInstance(primitiveType, List.of()));

		assertFalse(isAssignableFrom);
	}

	@ParameterizedTest(name = "{0}.isAssignableFrom(TypeInstance) with parameterized type instance, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withParameterizedTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
		boolean isAssignableFrom = new PrimitiveTypeInstance(primitiveKind, List.of()).isAssignableFrom(
				TypeFactory.getInstance().parameterized(List.class, String.class));

		assertFalse(isAssignableFrom);
	}

	@ParameterizedTest(name = "{0}.isAssignableFrom(TypeInstance) with incompatible class type instance, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withIncompatibleTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
		boolean isAssignableFrom =
				new PrimitiveTypeInstance(primitiveKind, List.of()).isAssignableFrom(TypeFactory.getInstance().ofObject());

		assertFalse(isAssignableFrom);
	}

	@ParameterizedTest(name = "{0}.isAssignableFrom(TypeInstance) with type variable instance, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withTypeVariableInstance_returnsFalse(PrimitiveKind primitiveKind) {
		boolean isAssignableFrom = new PrimitiveTypeInstance(primitiveKind, List.of()).isAssignableFrom(
				new DeclaredTypeVariableInstance(new ReflectiveClassInformation<>(List.class), "E", List.of(), List.of()));

		assertFalse(isAssignableFrom);
	}

	@ParameterizedTest(name = "{0}.isAssignableFrom(TypeInstance) with void type instance, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withVoidTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
		boolean isAssignableFrom =
				PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind).isAssignableFrom(TypeFactory.getInstance().ofVoid());

		assertFalse(isAssignableFrom);
	}

	@ParameterizedTest(name = "{0}.isAssignableFrom(TypeInstance) with wildcard type instance, returns false.")
	@EnumSource(PrimitiveKind.class)
	void isAssignableFrom_withWildcardTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
		boolean isAssignableFrom = PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind)
		                                                .isAssignableFrom(TypeFactory.getInstance().wildcardUnbounded());

		assertFalse(isAssignableFrom);
	}
}