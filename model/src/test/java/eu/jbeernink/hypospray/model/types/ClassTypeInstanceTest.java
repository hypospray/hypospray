package eu.jbeernink.hypospray.model.types;

import static eu.jbeernink.hypospray.model.types.PrimitiveTypeInstance.INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilder;

@DisplayName("ClassTypeInstance")
class ClassTypeInstanceTest {

	@Test
	@DisplayName("decriptorString() returns class descriptor.")
	void decriptorString_returnsClassDescriptor() {
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(String.class));

		var descriptorString = classTypeInstance.descriptorString();

		assertEquals("Ljava/lang/String;", descriptorString);
	}

	@Test
	@DisplayName("descriptorString() on a nested class type, returns nested class descriptor.")
	void descriptorString_onNestedClassType_returnsNestedClassDescriptor() {
		class Foo {}
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(Foo.class));

		var descriptorString = classTypeInstance.descriptorString();

		assertEquals("Leu/jbeernink/hypospray/model/types/ClassTypeInstanceTest$1Foo;", descriptorString);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a class type instance of the same type, returns true.")
	void isAssignableFrom_withSameClassType_returnsTrue() {
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(String.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class)));

		assertTrue(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a class type with a supertype, returns false.")
	void isAssignableFrom_withSuperClassType_returnsFalse() {
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(String.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(new ClassTypeInstance(new ReflectiveClassInformation<>(Object.class)));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a class type instance with a subclass, returns true.")
	void isAssignableFrom_withSubClassTypeInstance_returnsTrue() {
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(CharSequence.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class)));

		assertTrue(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a parameterized type with assignable raw type, returns true.")
	void isAssignableFrom_withParameterizedTypeInstanceWithAssignableRawType_returnsTrue() {
		var typeFactory = TypeFactory.getInstance();
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(Collection.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

		assertTrue(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with a parameterized type with an unassignable raw type, returns false.")
	void isAssignableFrom_withParameterizedTypeWithUnassignableRawType_returnsFalse() {
		var typeFactory = TypeFactory.getInstance();
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(Map.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(typeFactory.parameterized(List.class, String.class));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with an array type, returns false.")
	void isAssignableFrom_withArrayTypeInstance_returnsFalse() {
		var typeFactory = TypeFactory.getInstance();
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(List.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(typeFactory.ofArray(typeFactory.ofObject(), 1));

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with void type, returns false.")
	void isAssignableFrom_withVoidTypeInstance_returnsFalse() {
		var typeFactory = TypeFactory.getInstance();
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(String.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(typeFactory.ofVoid());

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("isAssignableFrom(TypeInstance) with wildcard type, returns false.")
	void isAssignableFrom_withWildcardTypeInstance_returnsFalse() {
		var typeFactory = TypeFactory.getInstance();
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(String.class));

		boolean isAssignableFrom = classTypeInstance.isAssignableFrom(typeFactory.wildcardUnbounded());

		assertFalse(isAssignableFrom);
	}

	@Test
	@DisplayName("withTypeAnnotations(List<AnnotationInformation>) creates a copy with the given type annotations.")
	void withTypeAnnotations_returnsCopyWithGivenTypeAnnotations() {
		var classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(String.class));
		var expectedAnnotations = List.of(new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(
				NonNull.class)).build());

		var copy = classTypeInstance.withTypeAnnotations(expectedAnnotations);

		assertEquals(expectedAnnotations, copy.annotations());
	}

	@Nested
	@DisplayName("with Boolean type")
	class WithBooleanType {

		private final ClassTypeInstance booleanClassTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(Boolean.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with BOOLEAN, returns true.")
		void isAssignableFrom_withBooleanTypeInstance_returnsTrue() {
			boolean isAssignableFrom = booleanClassTypeInstance.isAssignableFrom(PrimitiveTypeInstance.BOOLEAN);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "BOOLEAN", mode = EXCLUDE)
		void isAssignableFrom_withNonBooleanTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = booleanClassTypeInstance.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with Byte type")
	class WithByteType {

		private final ClassTypeInstance byteType = new ClassTypeInstance(new ReflectiveClassInformation<>(Byte.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with BYTE, returns true.")
		void isAssignableFrom_withByte_returnsTrue() {
			boolean isAssignableFrom = byteType.isAssignableFrom(PrimitiveTypeInstance.BYTE);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "BYTE", mode = EXCLUDE)
		void isAssignableFrom_withNonByteTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = byteType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with Char type")
	class WithCharType {

		private final ClassTypeInstance charType = new ClassTypeInstance(new ReflectiveClassInformation<>(Character.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with CHAR, returns true.")
		void isAssignableFrom_withChar_returnsTrue() {
			boolean isAssignableFrom = charType.isAssignableFrom(PrimitiveTypeInstance.CHAR);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "CHAR", mode = EXCLUDE)
		void isAssignableFrom_withNonCharTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = charType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with Double type")
	class WithDoubleType {

		private final ClassTypeInstance doubleType = new ClassTypeInstance(new ReflectiveClassInformation<>(Double.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with DOUBLE, returns true.")
		void isAssignableFrom_withDouble_returnsTrue() {
			boolean isAssignableFrom = doubleType.isAssignableFrom(PrimitiveTypeInstance.DOUBLE);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "DOUBLE", mode = EXCLUDE)
		void isAssignableFrom_withNonDoubleTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = doubleType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with Float type")
	class WithFloatType {

		private final ClassTypeInstance floatType = new ClassTypeInstance(new ReflectiveClassInformation<>(Float.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with FLOAT, returns true.")
		void isAssignableFrom_withFloat_returnsTrue() {
			boolean isAssignableFrom = floatType.isAssignableFrom(PrimitiveTypeInstance.FLOAT);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "FLOAT", mode = EXCLUDE)
		void isAssignableFrom_withNonFloatTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = floatType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with Long type")
	class WithLongType {

		private final ClassTypeInstance longType = new ClassTypeInstance(new ReflectiveClassInformation<>(Long.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with LONG, returns true.")
		void isAssignableFrom_withLong_returnsTrue() {
			boolean isAssignableFrom = longType.isAssignableFrom(PrimitiveTypeInstance.LONG);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "LONG", mode = EXCLUDE)
		void isAssignableFrom_withNonLongTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = longType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("with Integer type")
	class WithIntegerType {

		private final ClassTypeInstance classTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(Integer.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with int type, returns true.")
		void isAssignableFrom_withIntegerType_returnsTrue() {
			boolean isAssignableFrom = classTypeInstance.isAssignableFrom(INT);

			assertTrue(isAssignableFrom);
		}

	}

	@Nested
	@DisplayName("with Short type")
	class WithShortType {

		private final ClassTypeInstance shortType = new ClassTypeInstance(new ReflectiveClassInformation<>(Short.class));

		@Test
		@DisplayName("isAssignableFrom(TypeInstance) with SHORT, returns true.")
		void isAssignableFrom_withShort_returnsTrue() {
			boolean isAssignableFrom = shortType.isAssignableFrom(PrimitiveTypeInstance.SHORT);

			assertTrue(isAssignableFrom);
		}

		@DisplayName("isAssignableFrom(TypeInstance)")
		@ParameterizedTest(name = "with {0}, returns false.")
		@EnumSource(value = PrimitiveKind.class, names = "SHORT", mode = EXCLUDE)
		void isAssignableFrom_withNonShortTypeInstance_returnsFalse(PrimitiveKind primitiveKind) {
			boolean isAssignableFrom = shortType.isAssignableFrom(PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind));

			assertFalse(isAssignableFrom);
		}
	}

}