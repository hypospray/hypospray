package eu.jbeernink.hypospray.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.PackageInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.ClassType;
import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.ArrayTypeInstance;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.DeclaredTypeVariableInstance;
import eu.jbeernink.hypospray.model.types.ParameterizedTypeInstance;
import eu.jbeernink.hypospray.model.types.PrimitiveTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;
import eu.jbeernink.hypospray.model.types.VoidTypeInstance;
import eu.jbeernink.hypospray.model.types.WildcardTypeInstance;
import eu.jbeernink.hypospray.model.types.lazy.LazyTypeVariableInstance;

@DisplayName("TypeFactory")
class TypeFactoryTest {

	@Retention(RUNTIME)
	@Target(TYPE_USE)
	@interface TypeAnnotation {
		@SuppressWarnings("ClassExplicitlyAnnotation")
		enum Literal implements TypeAnnotation, Annotation {
			INSTANCE;

			@Override
			public Class<? extends Annotation> annotationType() {
				return TypeAnnotation.class;
			}
		}
	}

	private TypeFactory typeFactory;

	@BeforeEach
	void setup() {
		typeFactory = TypeFactory.getInstance();
	}

	@Nested
	@DisplayName(".of(Class<?>)")
	class Of {


		@Test
		@DisplayName("with void returns VoidInstanceType.")
		void withVoid_returnsVoidInstanceType() {
			TypeInstance typeInstance = typeFactory.of(Void.TYPE);

			assertEquals(VoidTypeInstance.VOID, typeInstance);
		}


		@ParameterizedTest
		@EnumSource(PrimitiveKind.class)
		@DisplayName("with primitive type returns correct PrimitiveTypeInstance.")
		void withPrimitiveType_returnsPrimitiveTypeInstance(PrimitiveKind primitiveKind) {
			TypeInstance typeInstance = typeFactory.of(getJavaType(primitiveKind));

			assertEquals(new PrimitiveTypeInstance(primitiveKind, List.of()), typeInstance);
		}

		private Class<?> getJavaType(PrimitiveKind primitiveKind) {
			return switch (primitiveKind) {
				case BOOLEAN -> Boolean.TYPE;
				case BYTE -> Byte.TYPE;
				case SHORT -> Short.TYPE;
				case INT -> Integer.TYPE;
				case LONG -> Long.TYPE;
				case FLOAT -> Float.TYPE;
				case DOUBLE -> Double.TYPE;
				case CHAR -> Character.TYPE;
			};
		}

		@Test
		@DisplayName("with array type, returns ArrayTypeInstance.")
		void withArrayType_returnsArrayTypeInstance() {
			Class<String[]> arrayClass = String[].class;

			TypeInstance typeInstance = typeFactory.of(arrayClass);

			assertEquals(
					new ArrayTypeInstance(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class)), List.of()),
					typeInstance);
		}

		@Test
		@DisplayName("with multi-dimensional array, returns nested ArrayTypeInstance.")
		void withMultiDimensionalArray_returnsNestedArrayTypeInstance() {
			Class<int[][][]> arrayClass = int[][][].class;

			TypeInstance typeInstance = typeFactory.of(arrayClass);

			var expectedTypeInstance = new ArrayTypeInstance(
					new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.INT, List.of()), List.of()), List.of());
			assertEquals(expectedTypeInstance, typeInstance);
		}
	}

	@Test
	@DisplayName("ofObject() returns an ClassTypeInstance for Object.class.")
	void ofObject_returnsExpectedClassTypeInstance() {
		TypeInstance typeInstance = typeFactory.ofObject();

		assertEquals(new ClassTypeInstance(new ReflectiveClassInformation<>(Object.class)), typeInstance);
	}

	@Test
	@DisplayName("ofVoid() returns VoidTypeInstance.VOID.")
	void ofVoid_returnsVoidTypeInstance() {
		TypeInstance typeInstance = typeFactory.ofVoid();

		assertEquals(VoidTypeInstance.VOID, typeInstance);
	}

	@ParameterizedTest
	@EnumSource(PrimitiveKind.class)
	@DisplayName("ofPrimitive(PrimitiveKind) returns the correct PrimitiveTypeInstance.")
	void ofPrimitive_returnsCorrectPrimitiveTypeInstance(PrimitiveKind primitiveKind) {
		TypeInstance typeInstance = typeFactory.ofPrimitive(primitiveKind);

		var expectedType = PrimitiveTypeInstance.fromPrimitiveKind(primitiveKind);
		assertEquals(expectedType, typeInstance);
	}

	@Nested
	@DisplayName("typeVariable(TypeVariableOwner, TypeVariable)")
	class TypeVariable_withTypeVariableOwnerAndTypeVariable {

		@Retention(RUNTIME)
		@Target({TYPE, TYPE_USE})
		@interface MyAnnotation {}

		@SuppressWarnings("all")
		class Foo<X, Y extends @MyAnnotation CharSequence, @NonNull Z> {}

		@Test
		@DisplayName("returns a type variable with the correct owner.")
		void returnsTypeVariableWithCorrectOwner() {
			var classInformation = new ReflectiveClassInformation<>(Foo.class);

			TypeVariableInstance typeVariable = typeFactory.typeVariable(classInformation, Foo.class.getTypeParameters()[0]);

			assertEquals(classInformation, typeVariable.owner());
		}

		@Test
		@DisplayName("returns a type variable with the correct name.")
		void returnsTypeVariableWithCorrectName() {
			var classInformation = new ReflectiveClassInformation<>(Foo.class);

			TypeVariableInstance typeVariable = typeFactory.typeVariable(classInformation, Foo.class.getTypeParameters()[0]);

			assertEquals("X", typeVariable.name());
		}

		@Test
		@DisplayName("with a type variable with no explicit bounds, returns a type variable with an upper bound of object.")
		void withTypeVariableWithoutBounds_returnsUpperBoundOfObject() {
			var classInformation = new ReflectiveClassInformation<>(Foo.class);

			TypeVariableInstance typeVariable = typeFactory.typeVariable(classInformation, Foo.class.getTypeParameters()[0]);

			var expectedBounds = List.of(typeFactory.ofObject());
			assertEquals(expectedBounds, typeVariable.upperBounds());
		}

		@Test
		@DisplayName("with a type variable with an explicit bound, returns a type variable with that bound.")
		void withTypeVariableWithBounds_returnsCorrectUpperBound() {
			var classInformation = new ReflectiveClassInformation<>(Foo.class);

			TypeVariableInstance typeVariable = typeFactory.typeVariable(classInformation, Foo.class.getTypeParameters()[1]);

			var expectedBounds = List.of(typeFactory.of(CharSequence.class)
			                                        .withTypeAnnotations(List.of(new ReflectiveAnnotationInformation<>(
					                                        Foo.class.getTypeParameters()[1].getAnnotatedBounds()[0].getAnnotations()[0]))));
			assertEquals(expectedBounds, typeVariable.upperBounds());
		}

		@Test
		@DisplayName("with a type variable without type annotations, returns an type variable without annotations")
		void withTypeVariableWithoutAnnotations_returnsTypeVariableWithoutAnnotations() {
			var classInformation = new ReflectiveClassInformation<>(Foo.class);

			TypeVariableInstance typeVariable = typeFactory.typeVariable(classInformation, Foo.class.getTypeParameters()[0]);

			assertEquals(List.of(), typeVariable.typeAnnotations());
		}

		@Test
		@DisplayName("with a type variable with an annotation, returns the type variable with annotations.")
		void withTypeVariableWithAnnotation_returnsTypeVariableWithAnnotations() {
			var classInformation = new ReflectiveClassInformation<>(Foo.class);

			TypeVariableInstance typeVariable = typeFactory.typeVariable(classInformation, Foo.class.getTypeParameters()[2]);

			var expectedAnnotations =
					List.of(new ReflectiveAnnotationInformation<>(Foo.class.getTypeParameters()[2].getAnnotations()[0]));
			assertEquals(expectedAnnotations, typeVariable.typeAnnotations());
		}
	}

	@Nested
	@DisplayName("wildcardWithUpperBound(Type)")
	class WildcardWithUpperBound {

		@Test
		@DisplayName("with TypeInstance returns WildcardTypeInstance with upper bound")
		void withValidTypeInstance_returnsWildcardTypeInstanceWithUpperBound() {
			var upperBound = new ClassTypeInstance(newClassInformation(BigDecimal.class));

			WildcardTypeInstance typeInstance = typeFactory.wildcardWithUpperBound(upperBound);

			assertEquals(new WildcardTypeInstance(List.of(upperBound), List.of(), List.of()), typeInstance);
		}

		@Test
		@DisplayName("with unsupported Type as upper bound, throws IllegalArgumentException.")
		void withUnsupportedTypeAsUpperBound_throwsIllegalArgumentException() {
			var fakeType = new FakeType();

			var exception = assertThrows(IllegalArgumentException.class, () -> typeFactory.wildcardWithUpperBound(fakeType));

			assertEquals("Unsupported Type instance, only Type instances created by the TypeFactory are supported: " +
			             "eu.jbeernink.hypospray.model.TypeFactoryTest$FakeType", exception.getMessage());
		}
	}

	@Nested
	@DisplayName("wildcardWithLowerBound(Type)")
	class WildcardWithLowerBound {

		@Test
		@DisplayName("with TypeInstance returns WildcardTypeInstance with lower bound")
		void withValidTypeInstance_returnsWildcardTypeInstanceWithUpperBound() {
			var lowerBound = new ClassTypeInstance(newClassInformation(BigDecimal.class));

			WildcardTypeInstance typeInstance = typeFactory.wildcardWithLowerBound(lowerBound);

			assertEquals(new WildcardTypeInstance(List.of(), List.of(lowerBound), List.of()), typeInstance);
		}

		@Test
		@DisplayName("with unsupported Type as lower bound, throws IllegalArgumentException.")
		void withUnsupportedTypeAsLowerBound_throwsIllegalArgumentException() {
			var fakeType = new FakeType();

			var exception = assertThrows(IllegalArgumentException.class, () -> typeFactory.wildcardWithLowerBound(fakeType));

			assertEquals("Unsupported Type instance, only Type instances created by the TypeFactory are supported: " +
			             "eu.jbeernink.hypospray.model.TypeFactoryTest$FakeType", exception.getMessage());
		}
	}

	@Test
	@DisplayName("wildcardUnbounded() returns unbounded WildcardTypeInstance.")
	void wildcardUnbounded_returnsUnboundedWildcardTypeInstance() {
		WildcardTypeInstance wildcardTypeInstance = typeFactory.wildcardUnbounded();

		assertEquals(new WildcardTypeInstance(List.of(), List.of(), List.of()), wildcardTypeInstance);
	}

	@Nested
	@DisplayName("ofClass(String)")
	class OfClassString {

		@Test
		@DisplayName("with existing class name, returns ClassTypeInstance for type.")
		void withExistingClassName_returnsClassTypeInstanceForType() {
			TypeInstance typeInstance = typeFactory.ofClass("java.lang.String");

			assertEquals(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class)), typeInstance);
		}

		@Test
		@DisplayName("with fake class name, returns null.")
		void withFakeClassName_returnsNull() {
			TypeInstance typeInstance = typeFactory.ofClass("sdfjgakldjsflkjasdklfjaklsdjfkalsdjf");

			assertNull(typeInstance);
		}
	}

	@Nested
	@DisplayName("parameterized(Class<?>, Class<?>...)")
	class ParameterizedClassVarargsClass {

		@Test
		@DisplayName("with generic class and correct number of type parameters, returns ParameterizedTypeInstance.")
		void withGenericClassAndCorrectNumberOfTypeParameters_returnsParameterizedTypeInstance() {
			ParameterizedTypeInstance typeInstance = typeFactory.parameterized(Map.class, String.class, Integer.class);

			assertEquals(typeInstance, new ParameterizedTypeInstance(new ClassTypeInstance(newClassInformation(Map.class)),
					List.of(new ClassTypeInstance(newClassInformation(String.class)),
							new ClassTypeInstance(newClassInformation(Integer.class))), List.of()));
		}

		@Test
		@DisplayName("with a non-generic class, throws IllegalArgumentException.")
		void withNonGenericClass_throwsIllegalArgumentException() {
			var exception =
					assertThrows(IllegalArgumentException.class, () -> typeFactory.parameterized(String.class, Object.class));

			assertEquals("Class does not have any generic type parameters: java.lang.String", exception.getMessage());
		}

		@Test
		@DisplayName("with incorrect number of type parameters, throws IllegalArgumentException.")
		void withIncorrectNumberOfTypeParameters_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(List.class, Integer.class, String.class));

			assertEquals("Incorrect number of generic type parameters, java.util.List has 1, but 2 were specified.",
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("parameterized(Class<?>, Type...)")
	class ParameterizedClassVarargsType {

		@Test
		@DisplayName("with generic class and correct number of type parameters, returns ParameterizedTypeInstance.")
		void withGenericClassAndCorrectNumberOfTypeParameters_returnsParameterizedTypeInstance() {
			ParameterizedTypeInstance typeInstance =
					typeFactory.parameterized(Map.class, new ClassTypeInstance(newClassInformation(String.class)),
							new ClassTypeInstance(newClassInformation(Integer.class)));

			assertEquals(typeInstance, new ParameterizedTypeInstance(new ClassTypeInstance(newClassInformation(Map.class)),
					List.of(new ClassTypeInstance(newClassInformation(String.class)),
							new ClassTypeInstance(newClassInformation(Integer.class))), List.of()));
		}

		@Test
		@DisplayName("with a non-generic class, throws IllegalArgumentException.")
		void withNonGenericClass_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(String.class, new ClassTypeInstance(newClassInformation(Object.class))));

			assertEquals("Class does not have any generic type parameters: java.lang.String", exception.getMessage());
		}

		@Test
		@DisplayName("with incorrect number of type parameters, throws IllegalArgumentException.")
		void withIncorrectNumberOfTypeParameters_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(List.class, new ClassTypeInstance(newClassInformation(Integer.class)),
							new ClassTypeInstance(newClassInformation(String.class))));

			assertEquals("Incorrect number of generic type parameters, java.util.List has 1, but 2 were specified.",
					exception.getMessage());
		}

		@Test
		@DisplayName("with unsupported type instance, throws IllegalArgumentException.")
		void withUnsupportedTypeInstance_throwsIllegalArgumentException() {
			var exception =
					assertThrows(IllegalArgumentException.class, () -> typeFactory.parameterized(List.class, new FakeType()));

			assertEquals(
					"Unsupported Type instance, only Type instances created by the TypeFactory are supported: eu.jbeernink.hypospray.model.TypeFactoryTest$FakeType",
					exception.getMessage());
		}
	}

	@Nested
	@DisplayName("parameterized(ClassType, Type...)")
	class ParameterizedClassTypeVarargsType {

		@Test
		@DisplayName("with generic class and correct number of type parameters, returns ParameterizedTypeInstance.")
		void withGenericClassAndCorrectNumberOfTypeParameters_returnsParameterizedTypeInstance() {
			ParameterizedTypeInstance typeInstance =
					typeFactory.parameterized(new ClassTypeInstance(newClassInformation(Map.class)),
							new ClassTypeInstance(newClassInformation(String.class)),
							new ClassTypeInstance(newClassInformation(Integer.class)));

			assertEquals(typeInstance, new ParameterizedTypeInstance(new ClassTypeInstance(newClassInformation(Map.class)),
					List.of(new ClassTypeInstance(newClassInformation(String.class)),
							new ClassTypeInstance(newClassInformation(Integer.class))), List.of()));
		}

		@Test
		@DisplayName("with a non-generic class, throws IllegalArgumentException.")
		void withNonGenericClass_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(new ClassTypeInstance(newClassInformation(String.class)),
							new ClassTypeInstance(newClassInformation(Object.class))));

			assertEquals("Class does not have any generic type parameters: java.lang.String", exception.getMessage());
		}

		@Test
		@DisplayName("with incorrect number of type parameters, throws IllegalArgumentException.")
		void withIncorrectNumberOfTypeParameters_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(new ClassTypeInstance(newClassInformation(List.class)),
							new ClassTypeInstance(newClassInformation(Integer.class)),
							new ClassTypeInstance(newClassInformation(String.class))));

			assertEquals("Incorrect number of generic type parameters, java.util.List has 1, but 2 were specified.",
					exception.getMessage());
		}

		@Test
		@DisplayName("with unsupported type instance, throws IllegalArgumentException.")
		void withUnsupportedTypeInstance_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(new ClassTypeInstance(newClassInformation(List.class)), new FakeType()));

			assertEquals(
					"Unsupported Type instance, only Type instances created by the TypeFactory are supported: eu.jbeernink.hypospray.model.TypeFactoryTest$FakeType",
					exception.getMessage());
		}

		@Test
		@DisplayName("with unsupported ClassType instance, throws IllegalArgumentException")
		void withUnsupportedClassType_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.parameterized(new FakeType(), new ClassTypeInstance(newClassInformation(String.class))));

			assertEquals("Unsupported Type instance, only Type instances created by the TypeFactory are supported: " +
			             "eu.jbeernink.hypospray.model.TypeFactoryTest$FakeType", exception.getMessage());
		}
	}

	@Nested
	@DisplayName("ofClass(ClassInfo)")
	class OfClassClassInfo {

		@Test
		@DisplayName("with ClassInformation, returns ClassTypeInstance.")
		void withClassInformation_returnsClassTypeInstance() {
			ClassInformation<String> classInformation = new ReflectiveClassInformation<>(String.class);

			ClassTypeInstance classTypeInstance = typeFactory.ofClass(classInformation);

			assertEquals(new ClassTypeInstance(classInformation), classTypeInstance);
		}

		@Test
		@DisplayName("with unsupported ClassInfo type, throws IllegalArgumentException.")
		void withUnknownClassInfoType_throwsIllegalArgumentException() {
			ClassInfo classInfo = new FakeClassInfo();

			var exception = assertThrows(IllegalArgumentException.class, () -> typeFactory.ofClass(classInfo));

			assertEquals("Unsupported ClassInfo type, only ClassInfo types created by Hypospray are supported: " +
			             "eu.jbeernink.hypospray.model.TypeFactoryTest$OfClassClassInfo$FakeClassInfo",
					exception.getMessage());
		}

		private static class FakeClassInfo implements ClassInfo {
			@Override
			public String name() {
				return null;
			}

			@Override
			public String simpleName() {
				return null;
			}

			@Override
			public PackageInfo packageInfo() {
				return null;
			}

			@Override
			public List<jakarta.enterprise.lang.model.types.TypeVariable> typeParameters() {
				return null;
			}

			@Override
			public jakarta.enterprise.lang.model.types.Type superClass() {
				return null;
			}

			@Override
			public ClassInfo superClassDeclaration() {
				return null;
			}

			@Override
			public List<jakarta.enterprise.lang.model.types.Type> superInterfaces() {
				return null;
			}

			@Override
			public List<ClassInfo> superInterfacesDeclarations() {
				return null;
			}

			@Override
			public boolean isPlainClass() {
				return false;
			}

			@Override
			public boolean isInterface() {
				return false;
			}

			@Override
			public boolean isEnum() {
				return false;
			}

			@Override
			public boolean isAnnotation() {
				return false;
			}

			@Override
			public boolean isRecord() {
				return false;
			}

			@Override
			public boolean isAbstract() {
				return false;
			}

			@Override
			public boolean isFinal() {
				return false;
			}

			@Override
			public int modifiers() {
				return 0;
			}

			@Override
			public Collection<MethodInfo> constructors() {
				return null;
			}

			@Override
			public Collection<MethodInfo> methods() {
				return null;
			}

			@Override
			public Collection<FieldInfo> fields() {
				return null;
			}

			@Override
			public Collection<RecordComponentInfo> recordComponents() {
				return null;
			}

			@Override
			public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
				return false;
			}

			@Override
			public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
				return false;
			}

			@Override
			public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
				return null;
			}

			@Override
			public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
				return null;
			}

			@Override
			public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
				return null;
			}

			@Override
			public Collection<AnnotationInfo> annotations() {
				return null;
			}
		}
	}

	@Nested
	@DisplayName("ofArray(Type, int)")
	class OfArray {

		@Test
		@DisplayName("with one-dimensional array, returns ArrayTypeInstance.")
		void withOneDimensionaArray_returnsArrayTypeInstance() {
			ArrayTypeInstance typeInstance = typeFactory.ofArray(PrimitiveTypeInstance.DOUBLE, 1);

			assertEquals(new ArrayTypeInstance(PrimitiveTypeInstance.DOUBLE, List.of()), typeInstance);
		}

		@Test
		@DisplayName("with multi-dimensional array, returns nested ArrayTypeInstance.")
		void withMultiDimensionalArray_returnsNestedArrayTypeInstance() {
			ArrayTypeInstance typeInstance = typeFactory.ofArray(PrimitiveTypeInstance.CHAR, 2);

			assertEquals(new ArrayTypeInstance(new ArrayTypeInstance(PrimitiveTypeInstance.CHAR, List.of()), List.of()),
					typeInstance);
		}

		@Test
		@DisplayName("with array type as element type, throws IllegalArgumentException.")
		void withArrayTypeAsElementType_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.ofArray(new ArrayTypeInstance(PrimitiveTypeInstance.CHAR, List.of()), 1));

			assertEquals(
					"ElementType of an array cannot be an array itself, please use the dimensions parameter to indicate a nested array instead.",
					exception.getMessage());
		}

		@Test
		@DisplayName("with unsupported element type instance, throws IllegalArgumentException.")
		void withUnsupportedTypeInstance_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class, () -> typeFactory.ofArray(new FakeType(), 10));

			assertEquals(
					"Unsupported Type instance, only Type instances created by the TypeFactory are supported: eu.jbeernink.hypospray.model.TypeFactoryTest$OfArray$FakeType",
					exception.getMessage());
		}

		@Test
		@DisplayName("with negative array dimensions, throws IllegalArgumentException.")
		void withNegativeArrayDimensions_throwsIllegalArgumentException() {
			var exception =
					assertThrows(IllegalArgumentException.class, () -> typeFactory.ofArray(PrimitiveTypeInstance.INT, -10));

			assertEquals("The dimensions of an array must be 1 or greater: -10.", exception.getMessage());
		}

		@Test
		@DisplayName("with zero array dimension, throws IllegalArgumentException.")
		void withZeroArrayDimension_throwsIllegalArgumentException() {
			var exception =
					assertThrows(IllegalArgumentException.class, () -> typeFactory.ofArray(PrimitiveTypeInstance.BOOLEAN, 0));

			assertEquals("The dimensions of an array must be 1 or greater: 0.", exception.getMessage());
		}

		@Test
		@DisplayName("with wildcard type, throws IllegalArgumentException.")
		void withWildcardType_throwsIllegalArgumentException() {
			var exception = assertThrows(IllegalArgumentException.class,
					() -> typeFactory.ofArray(new WildcardTypeInstance(List.of(), List.of(), List.of()), 10));

			assertEquals("Wildcards are not supported as the element types of an array.", exception.getMessage());
		}

		private static final class FakeType implements PrimitiveType {

			@Override
			public String name() {
				return "fake";
			}

			@Override
			public PrimitiveKind primitiveKind() {
				return PrimitiveKind.INT;
			}

			@Override
			public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
				return false;
			}

			@Override
			public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
				return false;
			}

			@Override
			public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
				return null;
			}

			@Override
			public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
				return List.of();
			}

			@Override
			public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
				return List.of();
			}

			@Override
			public Collection<AnnotationInfo> annotations() {
				return List.of();
			}
		}
	}

	@Nested
	@DisplayName("fromJavaType(Type)")
	class FromJavaType {
		@SuppressWarnings("unused")
		private static class WithRecursiveTypeVariable<@Nullable E extends Comparable<E>> {

		}

		private abstract static class WithParameterizedType implements List<String> {

		}

		@Test
		@DisplayName("with a regular class, returns ClassTypeInstance for type.")
		void withRegularClass_returnsClassTypeInstance() {
			TypeInstance typeInstance = typeFactory.fromJavaType(String.class);

			assertEquals(new ClassTypeInstance(newClassInformation(String.class)), typeInstance);
		}

		@Test
		@DisplayName("with type variable, returns TypeVariableInstance for type.")
		void withTypeVariable_returnsTypeVariableInstance() {
			TypeVariable<Class<WithRecursiveTypeVariable>> typeVariable =
					WithRecursiveTypeVariable.class.getTypeParameters()[0];

			TypeInstance typeInstance = typeFactory.fromJavaType(typeVariable);

			assertEquals(
					new DeclaredTypeVariableInstance(new ReflectiveClassInformation<>(WithRecursiveTypeVariable.class), "E",
							List.of(), List.of()), typeInstance);
		}

		@Test
		@DisplayName("with type variable, returns TypeVariableInstance with correct bounds.")
		void withTypeVariable_returnsTypeVariableInstanceWithCorrectBounds() {
			TypeVariable<Class<WithRecursiveTypeVariable>> typeVariable =
					WithRecursiveTypeVariable.class.getTypeParameters()[0];

			TypeInstance typeInstance = typeFactory.fromJavaType(typeVariable);

			var expectedBounds = List.of(
					new ParameterizedTypeInstance(new ReflectiveClassInformation<>(Comparable.class).asType().asClass(), List.of(
							new DeclaredTypeVariableInstance(new ReflectiveClassInformation<>(WithRecursiveTypeVariable.class), "E",
									List.of(), List.of())), List.of()));
			assertEquals(expectedBounds, typeInstance.asTypeVariable().upperBounds());
		}

		@Test
		@DisplayName("with type variable, returns TypeVariableInstance with correct annotations.")
		void withTypeVariable_returnsTypeVariableInstanceWithCorrectAnnotations() {
			TypeVariable<Class<WithRecursiveTypeVariable>> typeVariable =
					WithRecursiveTypeVariable.class.getTypeParameters()[0];

			TypeInstance typeInstance = typeFactory.fromJavaType(typeVariable);

			var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(
					WithRecursiveTypeVariable.class.getTypeParameters()[0].getAnnotations()[0]));
			assertEquals(expectedAnnotations, typeInstance.asTypeVariable().typeAnnotations());
		}

		@Test
		@DisplayName("with parameterized type, returns ParameterizedTypeInstance for type.")
		void withParameterizedType_returnsParameterizedTypeInstance() {
			Type parameterizedType = WithParameterizedType.class.getGenericInterfaces()[0];

			TypeInstance typeInstance = typeFactory.fromJavaType(parameterizedType);

			assertEquals(new ParameterizedTypeInstance(typeFactory.fromJavaType(List.class).asClass(),
					List.of(typeFactory.fromJavaType(String.class)), List.of()), typeInstance);
		}

		@Test
		@DisplayName("with parameterized type with type variable, returns ParameterizedTypeInstance with type variable.")
		void withParameterizedTypeWithTypeVariable_returnsParameterizedTypeInstanceWithTypeVariable() {
			Type parameterizedType = WithParameterizedType.class.getGenericInterfaces()[0];

			TypeInstance typeInstance = typeFactory.fromJavaType(parameterizedType);

			var expectedTypeInstance =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(String.class)),
							List.of());
			assertEquals(expectedTypeInstance, typeInstance);
		}

		@Test
		@DisplayName("with class with recursive type variable, returns type.")
		void withClassWithRecursiveTypeVariable_returnsType() {
			TypeInstance typeInstance = typeFactory.fromJavaType(WithRecursiveTypeVariable.class);

			assertEquals(new ClassTypeInstance(newClassInformation(WithRecursiveTypeVariable.class)), typeInstance);
		}

		@Test
		@DisplayName("with unbounded wildcard, returns wildcard with default upper bound.")
		void withUnboundedWildcard_returnsWildcardWithDefaultUpperBound() {
			abstract class Foo implements Comparable<List<?>> {}
			Type wildcard =
					((ParameterizedType) ((ParameterizedType) Foo.class.getGenericInterfaces()[0]).getActualTypeArguments()[0]).getActualTypeArguments()[0];

			TypeInstance typeInstance = typeFactory.fromJavaType(wildcard);

			assertEquals(new WildcardTypeInstance(List.of(typeFactory.ofObject()), List.of(), List.of()), typeInstance);
		}
	}

	@Nested
	@DisplayName("fromAnnotatedType(AnnotatedType)")
	class FromAnnotatedType {

		@Test
		@DisplayName("with generic interfaces with type annotations on generic parameters, returns correct type.")
		void withWildcardWithAnnotatedUpperBound_returnsWildcardWithAnnotatedUpperBound() {
			abstract class Foo implements Comparable<List<? extends @TypeAnnotation Object>> {}
			AnnotatedType genericInterface = Foo.class.getAnnotatedInterfaces()[0];

			TypeInstance typeInstance = typeFactory.fromAnnotatedType(genericInterface);


			var expectedTypeInstance = new ParameterizedTypeInstance(typeFactory.of(Comparable.class).asClass(), List.of(
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(new WildcardTypeInstance(List.of(
							typeFactory.ofObject()
							           .withTypeAnnotations(
									           List.of(new ReflectiveAnnotationInformation<>(TypeAnnotation.Literal.INSTANCE)))),
							List.of(), List.of())), List.of())), List.of());
			assertEquals(expectedTypeInstance, typeInstance);
		}

		@Test
		@DisplayName("with recursive parameterized type, returns parameterized type.")
		void withRecursiveTypeVariable_returnsTypeVariable() {
			class Foo<@TypeAnnotation E extends List<E>> {}
			TypeVariable<Class<Foo>>[] typeParameters = Foo.class.getTypeParameters();
			AnnotatedParameterizedType annotatedBound =
					(AnnotatedParameterizedType) typeParameters[0].getAnnotatedBounds()[0];

			TypeInstance typeInstance = typeFactory.fromAnnotatedType(annotatedBound);

			LateReference<TypeVariableInstance> typeVariableReference = new LateReference<>();
			ParameterizedTypeInstance expectedType =
					typeFactory.parameterized(List.class, new LazyTypeVariableInstance(typeVariableReference));
			typeVariableReference.setValue(
					typeFactory.typeVariable(typeFactory.of(Foo.class).asClass().declaration(), "E", List.of(expectedType),
							List.of(new ReflectiveAnnotationInformation<>(TypeAnnotation.Literal.INSTANCE))));
			assertEquals(expectedType, typeInstance);
		}
	}

	private static <T> ClassInformation<T> newClassInformation(Class<T> clazz) {
		return new ReflectiveClassInformation<>(clazz);
	}

	private static class FakeType implements ClassType {
		@Override
		public ClassInfo declaration() {
			return null;
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
			return false;
		}

		@Override
		public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
			return false;
		}

		@Override
		public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
			return null;
		}

		@Override
		public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
			return List.of();
		}

		@Override
		public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
			return List.of();
		}

		@Override
		public Collection<AnnotationInfo> annotations() {
			return List.of();
		}
	}
}