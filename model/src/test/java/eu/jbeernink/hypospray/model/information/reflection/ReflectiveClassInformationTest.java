package eu.jbeernink.hypospray.model.information.reflection;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.STATIC;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;
import eu.jbeernink.hypospray.model.information.RecordComponentInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticPackageInformation;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilder;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.DeclaredTypeVariableInstance;
import eu.jbeernink.hypospray.model.types.ParameterizedTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;

@DisplayName("ReflectiveClassInformation")
class ReflectiveClassInformationTest {

	@Retention(RUNTIME)
	@interface RepeatedAnnotationHolder {
		RepeatedAnnotation[] value();
	}

	@Repeatable(RepeatedAnnotationHolder.class)
	@Retention(RUNTIME)
	@interface RepeatedAnnotation {
		String value();

		@SuppressWarnings({"ClassExplicitlyAnnotation"})
		record Instance(String value) implements Annotation, RepeatedAnnotation {
			@Override
			public Class<? extends Annotation> annotationType() {
				return RepeatedAnnotation.class;
			}

			@Override
			public boolean equals(Object obj) {
				return switch (obj) {
					case RepeatedAnnotation other -> other.value().equals(value);
					case Object _ -> false;
				};
			}
		}
	}

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	@Nested
	@DisplayName("with static concrete class")
	class WithConcreteClass {
		@Vetoed
		@RepeatedAnnotation("a")
		@RepeatedAnnotation("b")
		static class ConcreteClass {

			private String s;
			@SuppressWarnings("unused")
			private String b;

			@SuppressWarnings("unused")
			ConcreteClass() {
			}

			@SuppressWarnings("unused")
			ConcreteClass(String s) {
				this.s = s;
			}

			public String getS() {
				return s;
			}

			public String getB() {
				return b;
			}
		}

		private final ReflectiveClassInformation<ConcreteClass> classInformation =
				new ReflectiveClassInformation<>(ConcreteClass.class);

		@Test
		@DisplayName("name() returns the name of the class.")
		void name_returnsClassName() {
			String name = classInformation.name();

			assertEquals(
					"eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformationTest$WithConcreteClass$ConcreteClass",
					name);
		}

		@Test
		@DisplayName("simpleName() returns the simple name of the class.")
		void simpleName_returnsClassSimpleName() {
			String simpleName = classInformation.simpleName();

			assertEquals("ConcreteClass", simpleName);
		}

		@Test
		@DisplayName("superClass() returns TypeInstance for the Object class.")
		void superClass_returnsObject() {
			TypeInstance typeInstance = classInformation.superClass();

			var expectedTypeInstance = TypeFactory.getInstance().ofObject();
			assertEquals(expectedTypeInstance, typeInstance);
		}

		@Test
		@DisplayName("superClassDeclaration() returns ClassInformation for the Object class.")
		void superClassDeclaration_returnsObjectClassInformation() {
			ClassInformation<? super ConcreteClass> superClassInformation = classInformation.superClassDeclaration();

			var expectedClassInformation = new ReflectiveClassInformation<>(Object.class);
			assertEquals(expectedClassInformation, superClassInformation);
		}

		@Test
		@DisplayName("isPlainClass() returns true.")
		void isPlainClass_returnsTrue() {
			boolean isPlainClass = classInformation.isPlainClass();

			assertTrue(isPlainClass);
		}

		@Test
		@DisplayName("isInterface() returns false.")
		void isInterface_returnsFalse() {
			boolean isInterface = classInformation.isInterface();

			assertFalse(isInterface);
		}

		@Test
		@DisplayName("isEnum() returns false.")
		void isEnum_returnsFalse() {
			boolean isEnum = classInformation.isEnum();

			assertFalse(isEnum);
		}

		@Test
		@DisplayName("isAnnotation() returns false.")
		void isAnnotation_returnsFalse() {
			boolean isAnnotation = classInformation.isAnnotation();

			assertFalse(isAnnotation);
		}

		@Test
		@DisplayName("isRecord() returns false.")
		void isRecord_returnsFalse() {
			boolean isRecord = classInformation.isRecord();

			assertFalse(isRecord);
		}

		@Test
		@DisplayName("modifiers() returns the static modifier.")
		void modifiers_returnsNoModifiers() {
			int modifiers = classInformation.modifiers();

			assertEquals(STATIC, modifiers);
		}

		@Test
		@DisplayName("superInterfaceInformation() returns an empty list.")
		void superInterfaceInformation_returnsEmptyList() {
			List<ClassInformation<?>> superInterfaceInformation = classInformation.superInterfaceInformation();

			assertEquals(List.of(), superInterfaceInformation);
		}

		@Test
		@DisplayName("superInterfacesDeclarations() returns an empty list.")
		void superInterfacesDeclarations_returnsEmptyList() {
			List<ClassInfo> superInterfacesDeclarations = classInformation.superInterfacesDeclarations();

			assertEquals(List.of(), superInterfacesDeclarations);
		}

		@Test
		@DisplayName("superInterfaceTypes() returns an empty list.")
		void superInterfaceTypes_returnsEmptyList() {
			List<TypeInstance> superInterfaceTypes = classInformation.superInterfaceTypes();

			assertEquals(List.of(), superInterfaceTypes);
		}

		@Test
		@DisplayName("superInterfaces() returns an empty list.")
		void superInterfaces_returnsEmptyList() {
			List<Type> types = classInformation.superInterfaces();

			assertEquals(List.of(), types);
		}

		@Test
		@DisplayName("packageInfo() returns package information.")
		void packageInfo_returnsPackageInfo() {
			PackageInformation packageInformation = classInformation.packageInfo();

			var expectedPackageInformation =
					new SyntheticPackageInformation("eu.jbeernink.hypospray.model.information.reflection", List.of());
			assertEquals(expectedPackageInformation, packageInformation);
		}

		@Test
		@DisplayName("annotationInformation() returns the annotation information.")
		void annotationInformation_returnsAnnotationInformation() {
			List<AnnotationInformation> annotationInformation = classInformation.annotationInformation();

			var expectedAnnotationInformation = List.of(newAnnotationBuilder(Vetoed.class).build(),
					newAnnotationBuilder(RepeatedAnnotationHolder.class).value(
							new AnnotationInfo[]{newAnnotationBuilder(RepeatedAnnotation.class).value("a").build(),
							                     newAnnotationBuilder(RepeatedAnnotation.class).value("b").build()}).build());
			assertEquals(expectedAnnotationInformation, annotationInformation);
		}

		@Test
		@DisplayName("constructorInformation() returns the list of constructors.")
		void constructorInformation_returnsConstructorInformation() throws Exception {
			List<ConstructorInformation<ConcreteClass>> constructors = classInformation.constructorInformation();

			var expectedConstructors =
					List.of(new ReflectiveConstructorInformation<>(ConcreteClass.class.getDeclaredConstructor()),
							new ReflectiveConstructorInformation<>(ConcreteClass.class.getDeclaredConstructor(String.class)));
			assertContainsExactly(expectedConstructors, constructors);
		}

		@Test
		@DisplayName("numberOfTypeParameters() returns zero.")
		void numberOfTypeParameters_returnsZero() {
			int numberOfTypeParameters = classInformation.numberOfTypeParameters();

			assertEquals(0, numberOfTypeParameters);
		}

		@Test
		@DisplayName("typeParameters() returns an empty list.")
		void typeParameters_returnsEmptyList() {
			List<TypeVariable> typeParameters = classInformation.typeParameters();

			assertEquals(List.of(), typeParameters);
		}

		@Test
		@DisplayName("typeParameterInstances() returns an empty list.")
		void typeParameterInstances_returnsEmptyList() {
			List<TypeVariableInstance> typeParameters = classInformation.typeParameterInstances();

			assertEquals(List.of(), typeParameters);
		}

		@Test
		@DisplayName("fields() returns all fields declared in the class.")
		void fields_returnsClassFields() throws Exception {
			Collection<FieldInfo> fields = classInformation.fields();

			var expectedFields = List.of(new ReflectiveFieldInformation(ConcreteClass.class.getDeclaredField("s")),
					new ReflectiveFieldInformation(ConcreteClass.class.getDeclaredField("b")));
			assertContainsExactly(expectedFields, fields);
		}

		@Test
		@DisplayName("fieldInformation() returns all fields declared in the class.")
		void fieldInformation_returnsClassFieldInformation() throws Exception {
			List<FieldInformation> fields = classInformation.fieldInformation();

			var expectedFields = List.of(new ReflectiveFieldInformation(ConcreteClass.class.getDeclaredField("s")),
					new ReflectiveFieldInformation(ConcreteClass.class.getDeclaredField("b")));
			assertContainsExactly(expectedFields, fields);
		}

		@Test
		@DisplayName("methods() returns a collection of information about the class' methods.")
		void methods_returnsClassMethodInformation() throws Exception {
			Collection<MethodInfo> methods = classInformation.methods();

			var expectedMethods = List.of(new ReflectiveMethodInformation(ConcreteClass.class.getMethod("getS")),
					new ReflectiveMethodInformation(ConcreteClass.class.getMethod("getB")));
			assertContainsExactly(expectedMethods, methods);
		}

		@Test
		@DisplayName("methodInformation() returns a list of information about the class' methods.")
		void methodInformation_returnsClassMethodInformation() throws Exception {
			List<MethodInformation> methods = classInformation.methodInformation();

			var expectedMethods = List.of(new ReflectiveMethodInformation(ConcreteClass.class.getMethod("getS")),
					new ReflectiveMethodInformation(ConcreteClass.class.getMethod("getB")));
			assertContainsExactly(expectedMethods, methods);
		}

		@Test
		@DisplayName("allMethods() includes methods declared by java.lang.Object.")
		void allMethods_includesMethodsDeclaredByObjectClass() throws Exception {
			List<MethodInformation> methods = classInformation.allMethods();

			var expectedMethods = new ArrayList<MethodInformation>();
			Stream.concat(Arrays.stream(Object.class.getDeclaredMethods()), Arrays.stream(Object.class.getMethods()))
			      .distinct()
			      .map(ReflectiveMethodInformation::new)
			      .forEach(expectedMethods::add);
			expectedMethods.add(new ReflectiveMethodInformation(ConcreteClass.class.getMethod("getS")));
			expectedMethods.add(new ReflectiveMethodInformation(ConcreteClass.class.getMethod("getB")));
			assertContainsExactly(expectedMethods, methods);
		}

		@Test
		@DisplayName("repeatableAnnotation(Class<? extends Annotation>) returns list of matching annotations.")
		void repeatableAnnotation_returnsRepeatedAnnotatiosn() {
			Collection<AnnotationInfo> annotations = classInformation.repeatableAnnotation(RepeatedAnnotation.class);

			var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(new RepeatedAnnotation.Instance("a")),
					new ReflectiveAnnotationInformation<>(new RepeatedAnnotation.Instance("b")));
			assertEquals(expectedAnnotations, annotations);
		}
	}

	@Nested
	@DisplayName("with class with superclass")
	class WithClassWithSuperClass {
		@RepeatedAnnotation("a")
		@RepeatedAnnotation("b")
		@SuppressWarnings("unused")
		abstract static class SuperClass {
			@SuppressWarnings("unused")
			private String a;

			private String getA() {
				return a;
			}

			protected abstract String getC();
		}

		@RepeatedAnnotation("c")
		@RepeatedAnnotation("d")
		@SuppressWarnings("unused")
		static class Foo extends SuperClass {
			@SuppressWarnings("unused")
			String b;

			protected String getB() {
				return b;
			}

			protected String getC() {
				return "c";
			}
		}

		private final ReflectiveClassInformation<Foo> classInformation = new ReflectiveClassInformation<>(Foo.class);

		@Test
		@DisplayName("fields() returns all fields declared in the class and superclass.")
		void fields_returnsClassAndSuperclassFields() throws Exception {
			Collection<FieldInfo> fields = classInformation.fields();

			var expectedFields = List.of(new ReflectiveFieldInformation(SuperClass.class.getDeclaredField("a")),
					new ReflectiveFieldInformation(Foo.class.getDeclaredField("b")));
			assertContainsExactly(expectedFields, fields);
		}

		@Test
		@DisplayName("fieldInformation() returns all fields declared in the class and superclass.")
		void fieldInformation_returnsClassAndSuperclassFieldInformation() throws Exception {
			List<FieldInformation> fields = classInformation.fieldInformation();

			var expectedFields = List.of(new ReflectiveFieldInformation(SuperClass.class.getDeclaredField("a")),
					new ReflectiveFieldInformation(Foo.class.getDeclaredField("b")));
			assertContainsExactly(expectedFields, fields);
		}

		@Test
		@DisplayName("superClass() returns super class type.")
		void superClass_returnsSuperClassInformation() {
			TypeInstance superClass = classInformation.superClass();

			assertEquals(TypeFactory.getInstance().of(SuperClass.class), superClass);
		}

		@Test
		@DisplayName("superClassDeclaration() returns class information for super class.")
		void superClassDeclaration_returnsSuperClassInformation() {
			ClassInformation<? super Foo> superClassInformation = classInformation.superClassDeclaration();

			assertEquals(new ReflectiveClassInformation<>(SuperClass.class), superClassInformation);
		}

		@Test
		@DisplayName("methods() returns method information including inherited methods.")
		void methods_returnsInheritedMethodInformation() throws Exception {
			Collection<MethodInfo> methods = classInformation.methods();

			var expectedMethods = List.of(new ReflectiveMethodInformation(Foo.class.getDeclaredMethod("getB")),
					new ReflectiveMethodInformation(Foo.class.getDeclaredMethod("getC")),
					new ReflectiveMethodInformation(SuperClass.class.getDeclaredMethod("getA")),
					new ReflectiveMethodInformation(SuperClass.class.getDeclaredMethod("getC")));
			assertContainsExactly(expectedMethods, methods);
		}

		@Test
		@DisplayName("methodInformation() returns method information including inherited methods.")
		void methodInformation_returnsInheritedMethodInformation() throws Exception {
			List<MethodInformation> methods = classInformation.methodInformation();

			var expectedMethods = List.of(new ReflectiveMethodInformation(Foo.class.getDeclaredMethod("getB")),
					new ReflectiveMethodInformation(Foo.class.getDeclaredMethod("getC")),
					new ReflectiveMethodInformation(SuperClass.class.getDeclaredMethod("getA")),
					new ReflectiveMethodInformation(SuperClass.class.getDeclaredMethod("getC")));
			assertContainsExactly(expectedMethods, methods);
		}

		@Test
		@DisplayName(
				"repeatableAnnotation(Class<? extends Annotation>) only returns the overridden annotation from the class.")
		void repeatableAnnotation_returnsOnlyOverriddenAnnotations() {
			Collection<AnnotationInfo> annotations = classInformation.repeatableAnnotation(RepeatedAnnotation.class);

			var expectedAnnotations = List.of(newAnnotationBuilder(RepeatedAnnotation.class).value("c").build(),
					newAnnotationBuilder(RepeatedAnnotation.class).value("d").build());
			assertEquals(expectedAnnotations, annotations);
		}
	}

	@Nested
	@DisplayName("with generic class")
	class WithGenericClass {
		@SuppressWarnings("unused")
		static class GenericClass<@Nullable T, X extends @NonNull T> {}

		@SuppressWarnings("rawtypes")
		private final ReflectiveClassInformation<GenericClass> classInformation =
				new ReflectiveClassInformation<>(GenericClass.class);

		@Test
		@DisplayName("numberOfTypeParameters() returns the number of type parameters.")
		void numberOfTypeParameters_returnsNumberOfTypeParameters() {
			int numberOfTypeParameters = classInformation.numberOfTypeParameters();

			assertEquals(2, numberOfTypeParameters);
		}

		@Test
		@DisplayName("typeParameters() returns the class' type parameters.")
		void typeParameters_returnsClassTypeParameters() {
			List<TypeVariable> typeParameters = classInformation.typeParameters();

			List<TypeVariableInstance> expectedTypeParameters = List.of(
					new DeclaredTypeVariableInstance(classInformation, "T", List.of(TypeFactory.getInstance().ofObject()),
							List.of()),
					new DeclaredTypeVariableInstance(classInformation, "X", List.of(TypeFactory.getInstance().of(String.class)),
							List.of()));
			assertEquals(expectedTypeParameters, typeParameters);
		}

		@Test
		@DisplayName("typeParameterInstances() returns the class' type parameters.")
		void typeParameterInstances_returnsClassTypeParameters() {
			List<TypeVariableInstance> typeParameters = classInformation.typeParameterInstances();

			List<TypeVariableInstance> expectedTypeParameters = List.of(
					new DeclaredTypeVariableInstance(classInformation, "T", List.of(TypeFactory.getInstance().ofObject()),
							List.of()),
					new DeclaredTypeVariableInstance(classInformation, "X", List.of(TypeFactory.getInstance().of(String.class)),
							List.of()));
			assertEquals(expectedTypeParameters, typeParameters);
		}

		@Test
		@DisplayName("typeParameterInstances() includes any annotations declared on the type parameter.")
		void typeParameterInstances_includesDeclaredAnnotations() {
			List<TypeVariableInstance> typeParameters = classInformation.typeParameterInstances();

			List<AnnotationInformation> expectedAnnotations =
					List.of(new ReflectiveAnnotationInformation<>(GenericClass.class.getTypeParameters()[0].getAnnotations()[0]));
			assertEquals(expectedAnnotations, typeParameters.getFirst().typeAnnotations());
		}

		@Test
		@DisplayName("typeParameterInstances() includes the default bounds for an unbound type parameter.")
		void typeParameterInstances_includesDefaultBoundsOnUnboundTypeParameter() {
			List<TypeVariableInstance> typeParameters = classInformation.typeParameterInstances();

			List<TypeInstance> expectedBounds = List.of(typeFactory.ofObject());
			assertEquals(expectedBounds, typeParameters.getFirst().bounds());
		}

		@Test
		@DisplayName("typeParameterInstances() includes the bounds of a type parameter.")
		void typeParameterInstances_includesTypeParameterBounds() {
			List<TypeVariableInstance> typeParameters = classInformation.typeParameterInstances();

			List<TypeInstance> expectedBounds =
					List.of(new DeclaredTypeVariableInstance(classInformation, "T", List.of(), List.of()));
			assertEquals(expectedBounds, typeParameters.get(1).bounds());
		}

		@Test
		@DisplayName("typeParameterInstances() includes any annotations declared on the bounds of the type parameter.")
		void typeParameterInstances_includesDeclaredBoundsAnnotations() {
			List<TypeVariableInstance> typeParameters = classInformation.typeParameterInstances();

			List<AnnotationInformation> expectedBoundsAnnotations = List.of(new ReflectiveAnnotationInformation<>(
					GenericClass.class.getTypeParameters()[1].getAnnotatedBounds()[0].getAnnotations()[0]));
			assertEquals(expectedBoundsAnnotations, typeParameters.get(1).bounds().getFirst().annotations());
		}
	}

	@Nested
	@DisplayName("with Object class")
	class WithObjectClass {

		private final ReflectiveClassInformation<Object> classInformation = new ReflectiveClassInformation<>(Object.class);

		@Test
		@DisplayName("superClass() returns null.")
		void superClass_returnsNull() {
			TypeInstance typeInstance = classInformation.superClass();

			assertNull(typeInstance);
		}

		@Test
		@DisplayName("superClassDeclaration() returns null.")
		void superClassDeclaration_returnsNull() {
			ClassInformation<?> superClassDeclaration = classInformation.superClassDeclaration();

			assertNull(superClassDeclaration);
		}
	}

	@Nested
	@DisplayName("with private concrete class")
	class WithPrivateConcreteClass {
		private static class PrivateConcreteClass {}

		private final ReflectiveClassInformation<PrivateConcreteClass> classInformation =
				new ReflectiveClassInformation<>(PrivateConcreteClass.class);

		@Test
		@DisplayName("modifiers() returns the private and static modifiers.")
		void modifiers_returnsThePrivateModifier() {
			int modifiers = classInformation.modifiers();

			int expectedModifiers = PRIVATE | STATIC;
			assertEquals(expectedModifiers, modifiers);
		}

		@Nested
		@DisplayName("with non-static package-friendly nested class")
		class WithNonStaticNestedClass {
			@SuppressWarnings("InnerClassMayBeStatic")
			class NonStaticNestedClass {}

			private final ReflectiveClassInformation<NonStaticNestedClass> classInformation =
					new ReflectiveClassInformation<>(NonStaticNestedClass.class);

			@Test
			@DisplayName("modifiers() returns no modifiers.")
			void modifiers_returnsNoModifiers() {
				int modifiers = classInformation.modifiers();

				int expectedModifiers = 0;
				assertEquals(expectedModifiers, modifiers);
			}
		}
	}

	@Nested
	@DisplayName("with concrete class with multiple super interfaces.")
	class WithMultipleSuperInterfaces {
		interface MyInterface {
			int value = 42;
		}

		static class ConcretedClassWithSuperInterfaces implements Runnable, Cloneable, MyInterface {
			@Override
			public void run() {
			}

			@Override
			public ConcretedClassWithSuperInterfaces clone() {
				try {
					return (ConcretedClassWithSuperInterfaces) super.clone();
				} catch (CloneNotSupportedException e) {
					throw new AssertionError();
				}
			}
		}

		private final ReflectiveClassInformation<ConcretedClassWithSuperInterfaces> classInformation =
				new ReflectiveClassInformation<>(ConcretedClassWithSuperInterfaces.class);

		@Test
		@DisplayName("superInterfaceInformation() returns class information for all super interfaces.")
		void superInterfaceInformation_returnsSuperInterfaceClassInformation() {
			List<ClassInformation<?>> superInterfaceInformation = classInformation.superInterfaceInformation();


			List<ClassInformation<?>> expectedSuperInterfaceInformation =
					List.of(new ReflectiveClassInformation<>(Runnable.class), new ReflectiveClassInformation<>(Cloneable.class),
							new ReflectiveClassInformation<>(MyInterface.class));
			assertContainsExactly(expectedSuperInterfaceInformation, superInterfaceInformation);
		}

		@Test
		@DisplayName("fieldInformation() returns field information from super interfaces.")
		void fieldInformation_returnsFieldInformationFromSuperInterfaces() throws Exception {
			List<FieldInformation> fieldInformation = classInformation.fieldInformation();

			List<FieldInformation> expectedFieldInformation =
					List.of(new ReflectiveFieldInformation(MyInterface.class.getDeclaredField("value")));
			assertEquals(expectedFieldInformation, fieldInformation);
		}
	}

	@Nested
	@DisplayName("with enum class")
	class WithEnumClass {
		enum EnumClass {
			FOO(1);

			private final int foo;

			EnumClass(int foo) {
				this.foo = foo;
			}

			public int foo() {
				return foo;
			}
		}

		private final ReflectiveClassInformation<EnumClass> classInformation =
				new ReflectiveClassInformation<>(EnumClass.class);

		@Test
		@DisplayName("isEnum() returns true.")
		void isEnum_returnsTrue() {
			boolean isEnum = classInformation.isEnum();

			assertTrue(isEnum);
		}

		@Test
		@DisplayName("isRecord() returns false.")
		void isRecord_returnsFalse() {
			boolean isRecord = classInformation.isRecord();

			assertFalse(isRecord);
		}

		@Test
		@DisplayName("isAnnotation() returns false.")
		void isAnnotation_returnsFalse() {
			boolean isAnnotation = classInformation.isAnnotation();

			assertFalse(isAnnotation);
		}

		@Test
		@DisplayName("isInterface() returns false.")
		void isInterface_returnsFalse() {
			boolean isInterface = classInformation.isInterface();

			assertFalse(isInterface);
		}

		@Test
		@DisplayName("isPlainClass() returns false.")
		void isPlainClass_returnsFalse() {
			boolean isPlainClass = classInformation.isPlainClass();

			assertFalse(isPlainClass);
		}

		@Test
		@DisplayName("fieldInformation() returns the fields in the enum.")
		void fieldInformation_returnsEnumFields() throws Exception {
			List<FieldInformation> fields = classInformation.fieldInformation();

			List<FieldInformation> expectedFields =
					Stream.concat(Stream.of(EnumClass.class.getDeclaredField("FOO"), EnumClass.class.getDeclaredField("foo")),
							      Arrays.stream(Enum.class.getDeclaredFields()).filter(field -> !field.isSynthetic()))
					      .map(ReflectiveFieldInformation::new)
					      .collect(toUnmodifiableList());
			assertEquals(expectedFields, fields);
		}

		@Test
		@DisplayName("methodInformation() returns methods from the enum class itself.")
		void methodInformation_returnsClassMethods() throws Exception {
			List<MethodInformation> methods = classInformation.methodInformation();

			ReflectiveMethodInformation expectedMethod = new ReflectiveMethodInformation(EnumClass.class.getMethod("foo"));
			assertContains(expectedMethod, methods);
		}

		@Test
		@DisplayName("methodInformation() includes methods from the Enum class.")
		void methodInformation_returnsAllMethodsDefinedInEnumClass() {
			List<MethodInformation> methods = classInformation.methodInformation();

			List<MethodInformation> enumMethods =
					methods.stream().filter(method -> method.declaringClass().name().equals(Enum.class.getName())).toList();
			List<MethodInformation> expectedEnumMethods =
					Stream.concat(Arrays.stream(Enum.class.getMethods()), Arrays.stream(Enum.class.getDeclaredMethods()))
					      .distinct()
					      .filter(method -> method.getDeclaringClass().equals(Enum.class))
					      .filter(method -> !method.isSynthetic())
					      .map(ReflectiveMethodInformation::new)
					      .collect(toUnmodifiableList());
			assertContainsExactly(expectedEnumMethods, enumMethods);
		}
	}

	@Nested
	@DisplayName("with interface")
	class WithInterface {
		interface Interface extends Runnable {

		}

		private final ReflectiveClassInformation<Interface> classInformation =
				new ReflectiveClassInformation<>(Interface.class);

		@Test
		@DisplayName("isInterface() returns true.")
		void isInterface_returnsTrue() {
			boolean isInterface = classInformation.isInterface();

			assertTrue(isInterface);
		}

		@Test
		@DisplayName("isEnum() returns false.")
		void isEnum_returnsFalse() {
			boolean isEnum = classInformation.isEnum();

			assertFalse(isEnum);
		}

		@Test
		@DisplayName("isAnnotation() returns false.")
		void isAnnotation_returnsFalse() {
			boolean isAnnotation = classInformation.isAnnotation();

			assertFalse(isAnnotation);
		}

		@Test
		@DisplayName("isRecord() returns false.")
		void isRecord_returnsFalse() {
			boolean isRecord = classInformation.isRecord();

			assertFalse(isRecord);
		}

		@Test
		@DisplayName("isPlainClass() returns false.")
		void isPlainClass_returnsFalse() {
			boolean isPlainClass = classInformation.isPlainClass();

			assertFalse(isPlainClass);
		}

		@Test
		@DisplayName("superClass() returns null.")
		void superClass_returnsNull() {
			TypeInstance typeInstance = classInformation.superClass();

			assertNull(typeInstance);
		}

		@Test
		@DisplayName("superClassDeclaration() returns null.")
		void superClassDeclaration_returnsNull() {
			ClassInformation<?> superClassInformation = classInformation.superClassDeclaration();

			assertNull(superClassInformation);
		}

		@Test
		@DisplayName("superInterfaceTypes() returns type instances for the super interfaces.")
		void superInterfaceTypes_returnsTypeInstances() {
			List<TypeInstance> superInterfaceTypes = classInformation.superInterfaceTypes();

			assertContainsExactly(List.of(TypeFactory.getInstance().of(Runnable.class)), superInterfaceTypes);
		}

		@Test
		@DisplayName("superInterfaceInformation() returns class information for the super interfaces.")
		void superInterfaceInformation_returnsClassInformation() {
			List<ClassInformation<?>> superInterfaceInformation = classInformation.superInterfaceInformation();

			assertEquals(List.of(new ReflectiveClassInformation<>(Runnable.class)), superInterfaceInformation);
		}

		@Test
		@DisplayName("superInterfaces() returns the types of the super interfaces.")
		void superInterfaces_returnsSuperInterfaceTypes() {
			List<Type> superInterfaces = classInformation.superInterfaces();

			assertEquals(List.of(TypeFactory.getInstance().of(Runnable.class)), superInterfaces);
		}

		@Test
		@DisplayName("superInterfacesDeclarations() returns class information for the super interfaces.")
		void superInterfacesDeclarations_returnsClassInformation() {
			List<ClassInfo> superInterfacesDeclarations = classInformation.superInterfacesDeclarations();

			assertEquals(List.of(new ReflectiveClassInformation<>(Runnable.class)), superInterfacesDeclarations);
		}

		@Test
		@DisplayName("fields() returns empty list")
		void fields_returnsEmptyList() {
			Collection<FieldInfo> fields = classInformation.fields();

			assertEquals(List.of(), fields);
		}

		@Test
		@DisplayName("fieldInformation() returns empty list.")
		void fieldInformation_returnsEmptyList() {
			List<FieldInformation> fields = classInformation.fieldInformation();

			assertEquals(List.of(), fields);
		}
	}

	@Nested
	@DisplayName("with record class")
	class WithRecordClass {
		record RecordClass(String a, int b) {}

		private final ReflectiveClassInformation<RecordClass> classInformation =
				new ReflectiveClassInformation<>(RecordClass.class);

		@Test
		@DisplayName("recordComponentInformation() returns the record components.")
		void recordComponentInformation_returnsTheRecordComponents() {
			List<RecordComponentInformation<RecordClass>> recordComponents = classInformation.recordComponentInformation();

			List<RecordComponentInformation<RecordClass>> expectedRecordComponents = List.of(
					new ReflectiveRecordComponentInformation<>(classInformation, RecordClass.class.getRecordComponents()[0]),
					new ReflectiveRecordComponentInformation<>(classInformation, RecordClass.class.getRecordComponents()[1]));
			assertEquals(expectedRecordComponents, recordComponents);
		}

		@Test
		@DisplayName("recordComponents() returns the record components.")
		void recordComponents_returnsTheRecordComponents() {
			Collection<RecordComponentInfo> recordComponents = classInformation.recordComponents();

			List<RecordComponentInfo> expectedRecordComponents = List.of(
					new ReflectiveRecordComponentInformation<>(classInformation, RecordClass.class.getRecordComponents()[0]),
					new ReflectiveRecordComponentInformation<>(classInformation, RecordClass.class.getRecordComponents()[1]));
			assertEquals(expectedRecordComponents, recordComponents);
		}

		@Test
		@DisplayName("superClass() returns type instance for record class.")
		void superClass_returnsRecordTypeInstance() {
			TypeInstance typeInstance = classInformation.superClass();

			var expectedTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(Record.class));
			assertEquals(expectedTypeInstance, typeInstance);
		}

		@Test
		@DisplayName("isRecord() returns true.")
		void isRecord_returnsTrue() {
			boolean isRecord = classInformation.isRecord();

			assertTrue(isRecord);
		}

		@Test
		@DisplayName("isPlainClass() returns false.")
		void isPlainClass_returnsFalse() {
			boolean isPlainClass = classInformation.isPlainClass();

			assertFalse(isPlainClass);
		}

		@Test
		@DisplayName("isInterface() returns false.")
		void isInterface_returnsFalse() {
			boolean isInterface = classInformation.isInterface();

			assertFalse(isInterface);
		}

		@Test
		@DisplayName("isEnum() returns false.")
		void isEnum_returnsFalse() {
			boolean isEnum = classInformation.isEnum();

			assertFalse(isEnum);
		}
	}

	@Nested
	@DisplayName("with annotation class")
	class WithAnnotationClass {
		@interface MyAnnotation {}

		private final ReflectiveClassInformation<MyAnnotation> classInformation =
				new ReflectiveClassInformation<>(MyAnnotation.class);

		@Test
		@DisplayName("isPlainClass() returns false.")
		void isPlainClass_returnsFalse() {
			boolean isPlainClass = classInformation.isPlainClass();

			assertFalse(isPlainClass);
		}

		@Test
		@DisplayName("isAnnotation() returns true.")
		void isAnnotation_returnsTrue() {
			boolean isAnnotation = classInformation.isAnnotation();

			assertTrue(isAnnotation);
		}

		@Test
		@DisplayName("isInterface() returns false.")
		void isInterface_returnsFalse() {
			boolean isInterface = classInformation.isInterface();

			assertFalse(isInterface);
		}

		@Test
		@DisplayName("isEnum() returns false.")
		void isEnum_returnsFalse() {
			boolean isEnum = classInformation.isEnum();

			assertFalse(isEnum);
		}

		@Test
		@DisplayName("isRecord() returns false.")
		void isRecord_returnsFalse() {
			boolean isRecord = classInformation.isRecord();

			assertFalse(isRecord);
		}
	}

	@Nested
	@DisplayName("with generic superclass with type parameters defined")
	class WithGenericSuperclassWithTypeParametersDefined {
		@Retention(RUNTIME)
		@Target(ElementType.TYPE_USE)
		@interface MyAnnotation {}

		@SuppressWarnings("unused")
		static class SuperClass<E> {}

		static class SubClass extends @MyAnnotation SuperClass<String> {}

		private final ReflectiveClassInformation<SubClass> reflectiveClassInformation =
				new ReflectiveClassInformation<>(SubClass.class);

		@Test
		@DisplayName("superClass() returns a parameterized type.")
		void superClass_returnsParameterizedType() {
			TypeInstance superClass = reflectiveClassInformation.superClass();

			var expectedSuperClass = new ParameterizedTypeInstance(typeFactory.of(SuperClass.class).asClass(),
					List.of(typeFactory.of(String.class)), List.of(
					new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(MyAnnotation.class)).build()));
			assertEquals(expectedSuperClass, superClass);
		}

	}

	@Nested
	@DisplayName("with generic superinterface with type parameters defined")
	class WithGenericSuperinterfaceWithTypeParametersDefined {
		@SuppressWarnings("unused")
		interface SuperInterface<E> {}

		static class SubClass implements SuperInterface<String> {}

		private final ReflectiveClassInformation<SubClass> classInformation =
				new ReflectiveClassInformation<>(SubClass.class);

		@Test
		@DisplayName("superInterfaceTypes() returns the parameterized interface type.")
		void superInterfaceTypes_returnsParameterizedInterfaceType() {
			List<TypeInstance> superInterfaceTypes = classInformation.superInterfaceTypes();

			var expectedSuperClass = new ParameterizedTypeInstance(typeFactory.of(SuperInterface.class).asClass(),
					List.of(typeFactory.of(String.class)), List.of());
			assertEquals(List.of(expectedSuperClass), superInterfaceTypes);

		}
	}

	@Nested
	@DisplayName("with class in the unnamed package")
	class WithClassInUnnamedPackage {

		private ReflectiveClassInformation<?> classInformation;

		@BeforeEach
		void setUp() throws Exception {
			Class<?> unnamedPackageClass = Class.forName("UnnamedPackageClass");
			classInformation = new ReflectiveClassInformation<>(unnamedPackageClass);
		}

		@Test
		@DisplayName("packageInfo() returns null.")
		void packageInfo_returnsNull() {
			PackageInformation packageInformation = classInformation.packageInfo();

			assertNull(packageInformation);
		}
	}

	@Nested
	@DisplayName("with class with overridden method")
	class WithClassWithOverriddenMethod {
		private static class ClassWithToString {
			@Override
			public String toString() {
				return super.toString();
			}
		}

		private ReflectiveClassInformation<?> classInformation;

		@BeforeEach
		void setUp() {
			classInformation = new ReflectiveClassInformation<>(ClassWithToString.class);
		}

		@Test
		@DisplayName("allMethods() returns overridden method only once.")
		void allMethods_returnsOverriddenMethodOnly() throws Exception {
			List<MethodInformation> methods = classInformation.allMethods();

			List<MethodInformation> toStringMethods =
					methods.stream().filter(method -> method.name().equals("toString")).toList();
			assertEquals(List.of(new ReflectiveMethodInformation(ClassWithToString.class.getMethod("toString"))), toStringMethods);
		}
	}

	private static <T> void assertContainsExactly(Collection<? extends T> expected, Collection<T> actual) {
		assertAll(Stream.concat(expected.stream().map(a -> () -> assertContains(a, actual)), Stream.of(
				() -> assertEquals(expected.size(), actual.size(),
						String.format("Collection was expected to be of size %d, but is of size %d: %s.", expected.size(),
								actual.size(), actual))))

		);
	}

	private static <T> void assertContains(T expected, Collection<T> actual) {
		assertTrue(actual.contains(expected),
				String.format("Expected collection to contain %s, but collection was: %s.", expected, actual));
	}

	private static SyntheticAnnotationInformationBuilder newAnnotationBuilder(
			Class<? extends Annotation> annotationType) {
		return new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(annotationType));
	}
}