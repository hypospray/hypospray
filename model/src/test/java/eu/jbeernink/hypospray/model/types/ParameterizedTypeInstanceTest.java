package eu.jbeernink.hypospray.model.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.literal.NamedLiteral;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("ParameterizedTypeInstance")
class ParameterizedTypeInstanceTest {

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	@Test
	@DisplayName("descriptorString() returns raw type descriptor string.")
	void descriptorString_returnsRawTypeDescriptorString() {
		var parameterizedType =
				new ParameterizedTypeInstance(new ClassTypeInstance(new ReflectiveClassInformation<>(List.class)),
						List.of(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class))), List.of());

		String descriptorString = parameterizedType.descriptorString();

		assertEquals("Ljava/util/List;", descriptorString);
	}

	@Test
	@DisplayName("withTypeAnnotations(List<AnnotationInformation>) creates a copy with the new annotations.")
	void withTypeAnnotations_returnsCopyWithTheNewAnnotations() {
			var parameterizedType =
				new ParameterizedTypeInstance(new ClassTypeInstance(new ReflectiveClassInformation<>(List.class)),
						List.of(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class))), List.of(new ReflectiveAnnotationInformation<>(
						NamedLiteral.of("old"))));
			List<AnnotationInformation> annotations = List.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("new")));

			ParameterizedTypeInstance copy = parameterizedType.withTypeAnnotations(annotations);

			var expectedCopy = new ParameterizedTypeInstance(new ClassTypeInstance(new ReflectiveClassInformation<>(List.class)),
					List.of(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class))), annotations);
			assertEquals(expectedCopy, copy);
	}

	@Nested
	@DisplayName("isAssignableFrom(TypeInstance)")
	class IsAssignableFrom {

		@Test
		@DisplayName("with void type, returns false.")
		void withVoidTyp_returnsFalse() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(String.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(VoidTypeInstance.VOID);

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("with primitive type, returns false.")
		void withPrimitiveType_returnsFalse() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(String.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(PrimitiveTypeInstance.INT);

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("with parameterized with the same raw type and type parameters, returns true.")
		void withParameterizedTypeWithSameRawTypeAndTypeParameters_returnsTrue() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(Number.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(Number.class)), List.of()));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("with subclass which sets same type parameters, returns true.")
		void withSubclassWhichSetsSameTypeParameters_returnsTrue() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(String.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(
					new ParameterizedTypeInstance(typeFactory.of(ArrayList.class).asClass(),
							List.of(typeFactory.of(String.class)), List.of()));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("with array type, returns false.")
		void withArrayType_returnsFalse() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(String.class)), List.of());

			boolean isAssignableFrom =
					parameterizedType.isAssignableFrom(typeFactory.ofArray(typeFactory.of(String.class), 1));

			assertFalse(isAssignableFrom);
		}

		@Test
		@DisplayName("with class type that equals the raw type, returns true.")
		void withClassInstanceThatEqualsRawType_returnsTrue() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(String.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(typeFactory.of(List.class));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("with parameterized type with a sub-class and same type arguments, returns true.")
		void withParameterizedTypeWithSubClassSameTypeArguments_returnsTrue() {
			var parameterizedType = new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(),
					List.of(typeFactory.fromJavaType(String.class)), List.of());

			boolean isAssignableFrom =
					parameterizedType.isAssignableFrom(typeFactory.parameterized(ArrayList.class, String.class));

			assertTrue(isAssignableFrom);
		}

//		@Disabled("TODO #66: Fix assignability of type variable to type before re-enabling.")
		@Test
		@DisplayName("with a raw class type that is a subclass of the raw type, returns true.")
		void withClassInstanceThatIsASubclassOfRawType_returnsTrue() {
			var parameterizedType = new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(),
					List.of(typeFactory.fromJavaType(String.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(typeFactory.of(ArrayList.class));

			assertTrue(isAssignableFrom);
		}

		@Test
		@DisplayName("with class type that does not equal the raw type, returns false.")
		void withClassInstanceThatDoesNotEqualsRawType_returnsFalse() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(List.class).asClass(), List.of(typeFactory.of(Number.class)), List.of());

			boolean isAssignableFrom = parameterizedType.isAssignableFrom(typeFactory.of(Set.class));

			assertFalse(isAssignableFrom);
		}
	}

	@Nested
	@DisplayName("parentTypes()")
	class ParentTypes {
		static class NonGenericClass {}

		interface NonGenericInterface {}

		static class GenericClass<E> {}

		interface GenericInterface<E> {}

		static class GenericClassWithNonGenericParents<E> extends NonGenericClass implements NonGenericInterface {}

		static class GenericClassWithGenericParents<X, Y> extends GenericClass<X> implements GenericInterface<Y> {}

		@Test
		@DisplayName("with generic class with non-generic parents, returns list of class type instances.")
		void withNonGenericParents_returnsListOfTypeInstances() {
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(GenericClassWithNonGenericParents.class).asClass(),
							List.of(typeFactory.of(String.class)), List.of());

			List<TypeInstance> parentTypes = parameterizedType.parentTypes();

			assertEquals(List.of(typeFactory.of(NonGenericClass.class), typeFactory.of(NonGenericInterface.class)),
					parentTypes);
		}

		@Test
		@DisplayName(
				"with generic class that passes type arguments to generic parents, returns a list of parameterized type instances.")
		void withGenericClassPassingTypeArgumentsToParents_returnsListOfParameterizedTypeInstances() {
			TypeInstance firstTypeArgument = typeFactory.of(String.class);
			TypeInstance secondTypeArgument = typeFactory.of(BigDecimal.class);
			var parameterizedType =
					new ParameterizedTypeInstance(typeFactory.of(GenericClassWithGenericParents.class).asClass(),
							List.of(firstTypeArgument, secondTypeArgument), List.of());

			List<TypeInstance> parentTypes = parameterizedType.parentTypes();

			var expectedParentTypes = List.of(
					new ParameterizedTypeInstance(typeFactory.of(GenericClass.class).asClass(), List.of(firstTypeArgument), List.of()),
					new ParameterizedTypeInstance(typeFactory.of(GenericInterface.class).asClass(), List.of(secondTypeArgument), List.of()));
			assertEquals(expectedParentTypes, parentTypes);
		}
	}
}