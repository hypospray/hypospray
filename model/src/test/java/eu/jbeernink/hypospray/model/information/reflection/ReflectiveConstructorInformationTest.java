package eu.jbeernink.hypospray.model.information.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;

@DisplayName("ReflectiveConstructorInformation")
class ReflectiveConstructorInformationTest {


	@Nested
	@DisplayName("with constructor without type variables")
	class ConstructorWithoutTypeVariables {

		private Constructor<WithoutTypeVariables> constructor;

		@SuppressWarnings("unused")
		static class WithoutTypeVariables {
			@Inject
			public WithoutTypeVariables(String a, int b) {
			}
		}

		private ReflectiveConstructorInformation<WithoutTypeVariables> constructorInformation;

		@BeforeEach
		void setup() throws Exception {
			constructor = WithoutTypeVariables.class.getConstructor(String.class, int.class);

			constructorInformation = new ReflectiveConstructorInformation<>(constructor);
		}

		@Test
		@DisplayName("name() returns the binary name of the declaring class.")
		void name_returnsDeclaringClassBinaryName() {
			String name = constructorInformation.name();

			assertEquals(
					"eu.jbeernink.hypospray.model.information.reflection.ReflectiveConstructorInformationTest$ConstructorWithoutTypeVariables$WithoutTypeVariables",
					name);
		}

		@Test
		@DisplayName("annotationInformation() returns the annotations declared on the constructor.")
		void annotationInformation_returnsDeclaredAnnotations() {
			List<AnnotationInformation> annotations = constructorInformation.annotationInformation();

			var expectedAnnotations =
					List.of(new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Inject.class), Map.of()));
			assertEquals(expectedAnnotations, annotations);
		}

		@Test
		@DisplayName("parameters() returns information about the parameters.")
		void parameters_returnsParameterInformation() {
			List<ParameterInfo> parameters = constructorInformation.parameters();

			var expectedParameters = List.of(new ReflectiveParameterInformation(constructor.getParameters()[0]),
					new ReflectiveParameterInformation(constructor.getParameters()[1]));
			assertEquals(expectedParameters, parameters);
		}

		@Test
		@DisplayName("methodIdentifier() returns the method identifier.")
		void methodIdentifier_returnsMethodIdentifier() {
			String methodIdentifier = constructorInformation.methodIdentifier();

			assertEquals("new[(Ljava/lang/String;I)Leu/jbeernink/hypospray/model/information/reflection/ReflectiveConstructorInformationTest$ConstructorWithoutTypeVariables$WithoutTypeVariables;]", methodIdentifier);
		}
	}

	@Nested
	@DisplayName("with constructor with type annotations")
	class ConstructorWithTypeAnnotations {

		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.TYPE_USE)
		@interface MyTypeAnnotation {}

		static class WithTypeAnnotations {

			@MyTypeAnnotation
			public WithTypeAnnotations() {
			}
		}

		private ReflectiveConstructorInformation<WithTypeAnnotations> constructor;

		@BeforeEach
		void setup() throws Exception {
			constructor = new ReflectiveConstructorInformation<>(WithTypeAnnotations.class.getConstructor());
		}

		@Test
		@DisplayName("returnType() returns the type of the constructor.")
		void returnType_returnsTypeOfConstructor() throws Exception {
			TypeInstance typeInstance = constructor.returnType();

			var expectedTypeInstance = new ClassTypeInstance(new ReflectiveClassInformation<>(WithTypeAnnotations.class),
					List.of(new ReflectiveAnnotationInformation<>(WithTypeAnnotations.class.getConstructor()
					                                                                       .getAnnotatedReturnType()
					                                                                       .getAnnotation(
							                                                                       MyTypeAnnotation.class))));
			assertEquals(expectedTypeInstance, typeInstance);
		}
	}

	@Nested
	@DisplayName("with enum constructor")
	class WithEnumConstructor {

		private Constructor<MyEnum> actualConstructor;

		enum MyEnum {
			FOO(1);
			private final int value;

			MyEnum(int value) {
				this.value = value;
			}
		}

		private ReflectiveConstructorInformation<MyEnum> constructor;

		@BeforeEach
		void setup() throws Exception {
			actualConstructor = MyEnum.class.getDeclaredConstructor(String.class, Integer.TYPE, Integer.TYPE);
			constructor = new ReflectiveConstructorInformation<>(actualConstructor);
		}

		@Test
		@DisplayName("parameters() returns only the non-synthetic parameters.")
		void parameters_returnsOnlyNonSyntheticParameters() {
			List<ParameterInfo> parameters = constructor.parameters();

			var expectedParameters = List.of(new ReflectiveParameterInformation(actualConstructor.getParameters()[2]));
			assertEquals(expectedParameters, parameters);
		}

		@Test
		@DisplayName("parameterInformation() returns only the non-synthetic parameters.")
		void parameterInformation_returnsOnlyNonSyntheticParameters() {
			List<ParameterInformation> parameters = constructor.parameterInformation();

			var expectedParameters = List.of(new ReflectiveParameterInformation(actualConstructor.getParameters()[2]));
			assertEquals(expectedParameters, parameters);
		}
	}

}