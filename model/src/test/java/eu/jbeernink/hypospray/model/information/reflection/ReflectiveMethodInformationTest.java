package eu.jbeernink.hypospray.model.information.reflection;

import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.STATIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

@DisplayName("ReflectiveMethodInformation")
class ReflectiveMethodInformationTest {

	@Nested
	@DisplayName("with instance method")
	class WithInstanceMethod {

		@SuppressWarnings("unused")
		static class TestClass {
			protected final Runnable method(String a, int b) {
				return () -> {};
			}
		}

		private Method method;

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			method = TestClass.class.getDeclaredMethod("method", String.class, Integer.TYPE);

			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("name() returns the method name.")
		void name_returnsMethodName() {
			String name = methodInformation.name();

			assertEquals("method", name);
		}

		@Test
		@DisplayName("modifiers() returns the method's modifiers.")
		void modifiers_returnsMethodModifiers() {
			int modifiers = methodInformation.modifiers();

			assertEquals(PROTECTED | FINAL, modifiers);
		}

		@Test
		@DisplayName("methodIdentifier() returns the method identifier.")
		void methodIdentifier_returnsMethodIdentifier() {
			String identifier = methodInformation.methodIdentifier();

			assertEquals("method[(Ljava/lang/String;I)Ljava/lang/Runnable;]", identifier);
		}

		@Test
		@DisplayName("declaringClass() returns the declaring class information.")
		void declaringClass_returnsDeclaringClassInformation() {
			ClassInformation<?> declaringClass = methodInformation.declaringClass();

			assertEquals(new ReflectiveClassInformation<>(TestClass.class), declaringClass);
		}

		@Test
		@DisplayName("receiverType() returns the type of the class.")
		void receiverType_returnsClassTypeInstance() {
			TypeInstance typeInstance = methodInformation.receiverType();

			assertEquals(TypeFactory.getInstance().of(TestClass.class), typeInstance);
		}

		@Test
		@DisplayName("parameters() returns a list of information about the parameters.")
		void parameters_returnsParameterInformation() {
			List<ParameterInfo> parameters = methodInformation.parameters();

			var expectedParameters = List.of(new ReflectiveParameterInformation(method.getParameters()[0]),
					new ReflectiveParameterInformation(method.getParameters()[1]));
			assertEquals(expectedParameters, parameters);
		}

		@Test
		@DisplayName("parameterInformation() returns a list of information about the parameters.")
		void parameterInformation_returnsParameterInformation() {
			List<ParameterInformation> parameters = methodInformation.parameterInformation();

			var expectedParameters = List.of(new ReflectiveParameterInformation(method.getParameters()[0]),
					new ReflectiveParameterInformation(method.getParameters()[1]));
			assertEquals(expectedParameters, parameters);
		}

		@Test
		@DisplayName("returnType() returns the return type.")
		void returnType_returnsReturnType() {
			TypeInstance returnType = methodInformation.returnType();

			assertEquals(TypeFactory.getInstance().of(Runnable.class), returnType);
		}

		@Test
		@DisplayName("throwsTypes() returns an empty list.")
		void throwsTypes_returnsEmptyList() {
			List<Type> throwsTypes = methodInformation.throwsTypes();

			assertEquals(List.of(), throwsTypes);
		}

		@Test
		@DisplayName("throwsTypeInstances() returns an empty list.")
		void throwsTypeInstances_returnsEmptyList() {
			List<TypeInstance> throwsTypes = methodInformation.throwsTypeInstances();

			assertEquals(List.of(), throwsTypes);
		}

		@Test
		@DisplayName("annotations() returns an empty list.")
		void annotations_returnsEmptyList() {
			List<AnnotationInfo> annotations = methodInformation.annotations();

			assertEquals(List.of(), annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns an empty list.")
		void annotationInformation_returnsEmptyList() {
			List<AnnotationInformation> annotations = methodInformation.annotationInformation();

			assertEquals(List.of(), annotations);
		}
	}

	@Nested
	@DisplayName("with method that throws")
	class WithMethodThatThrows {
		static class TestClass {
			@SuppressWarnings("RedundantThrows")
			protected final void throwsMethod() throws IOException, InterruptedException {
			}
		}

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			Method method = TestClass.class.getDeclaredMethod("throwsMethod");

			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("throwsTypes() returns the types of the declared exceptions.")
		void throwsTypes_returnsDeclaredExceptions() {
			List<Type> throwsTypes = methodInformation.throwsTypes();

			var expectedThrowsTypes = List.of(TypeFactory.getInstance().of(IOException.class),
					TypeFactory.getInstance().of(InterruptedException.class));
			assertEquals(expectedThrowsTypes, throwsTypes);
		}

		@Test
		@DisplayName("throwsTypeInstances() returns the types of the declared exceptions.")
		void throwsTypeInstances_returnsDeclaredExceptions() {
			List<TypeInstance> throwsTypes = methodInformation.throwsTypeInstances();

			var expectedThrowsTypes = List.of(TypeFactory.getInstance().of(IOException.class),
					TypeFactory.getInstance().of(InterruptedException.class));
			assertEquals(expectedThrowsTypes, throwsTypes);
		}
	}

	@Nested
	@DisplayName("with instance method with void return type")
	class WithInstanceMethodWithVoidReturnType {
		@SuppressWarnings("unused")
		static class TestClass {
			protected final void voidMethod(String a, int b) {
			}
		}

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			Method method = TestClass.class.getDeclaredMethod("voidMethod", String.class, Integer.TYPE);

			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("returnType() returns the void type.")
		void returnType_returnsVoidType() {
			TypeInstance returnType = methodInformation.returnType();

			assertEquals(TypeFactory.getInstance().ofVoid(), returnType);
		}

		@Test
		@DisplayName("methodIdentifier() returns the method identifier.")
		void methodIdentifier_returnsMethodIdentifier() {
			String methodIdentifier = methodInformation.methodIdentifier();

			assertEquals("voidMethod[(Ljava/lang/String;I)V]", methodIdentifier);
		}
	}

	@Nested
	@DisplayName("with instance method without parameters")
	class WithInstanceMethodWithoutParameters {
		static class TestClass {
			protected final void parameterLessMethod() {
			}
		}

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			Method method = TestClass.class.getDeclaredMethod("parameterLessMethod");

			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("parameters() returns an empty list.")
		void parameters_returnsEmptyList() {
			List<ParameterInfo> parameters = methodInformation.parameters();

			assertEquals(List.of(), parameters);
		}

		@Test
		@DisplayName("parameterInformation() returns an empty list.")
		void parameterInformation_returnsEmptyList() {
			List<ParameterInformation> parameters = methodInformation.parameterInformation();

			assertEquals(List.of(), parameters);
		}
	}

	@Nested
	@DisplayName("with annotated method")
	class WithAnnotatedMethod {
		static class TestClass {
			@RequestScoped
			void annotatedMethod() {
			}
		}

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			Method method = TestClass.class.getDeclaredMethod("annotatedMethod");

			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("annotations() returns information about the method's annotations.")
		void annotations_returnsMethodAnnotationInformation() {
			List<AnnotationInfo> annotations = methodInformation.annotations();

			var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(RequestScoped.Literal.INSTANCE));
			assertEquals(expectedAnnotations, annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns information about the method's annotations.")
		void annotationInformation_returnsMethodAnnotationInformation() {
			List<AnnotationInformation> annotations = methodInformation.annotationInformation();

			var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(RequestScoped.Literal.INSTANCE));
			assertEquals(expectedAnnotations, annotations);
		}
	}

	@Nested
	@DisplayName("with static method")
	class WithStaticMethod {

		static void staticMethod() {
		}

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			Method method = WithStaticMethod.class.getDeclaredMethod("staticMethod");

			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("modifiers() returns the static modifier.")
		void modifiers_returnsStaticModifier() {
			int modifiers = methodInformation.modifiers();

			assertEquals(STATIC, modifiers);
		}

		@Test
		@DisplayName("receiverType() returns null.")
		void receiverType_returnsNull() {
			TypeInstance typeInstance = methodInformation.receiverType();

			assertNull(typeInstance);
		}
	}

	@Nested
	@DisplayName("with parameterized method")
	class WithParameterizedMethod {

		<T extends CharSequence> T testMethod(List<T> input) {
			return input.getFirst();
		}

		private ReflectiveMethodInformation methodInformation;

		@BeforeEach
		void setup() throws Exception {
			Method method = WithParameterizedMethod.class.getDeclaredMethod("testMethod", List.class);
			methodInformation = new ReflectiveMethodInformation(method);
		}

		@Test
		@DisplayName("typeParameters() returns the list of type parameters.")
		void typeParameters_returnsTypeParameters() {
			TypeFactory typeFactory = TypeFactory.getInstance();

			List<TypeVariable> typeParameters = methodInformation.typeParameters();

			var expectedTypeParameters = List.of(
					typeFactory.typeVariable(methodInformation, "T", List.of(typeFactory.of(CharSequence.class)), List.of()));
			assertEquals(expectedTypeParameters, typeParameters);
		}
	}
}