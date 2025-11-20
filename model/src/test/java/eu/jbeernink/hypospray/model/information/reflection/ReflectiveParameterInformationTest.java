package eu.jbeernink.hypospray.model.information.reflection;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ExecutableInformation;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilder;
import eu.jbeernink.hypospray.model.types.TypeInstance;

class ReflectiveParameterInformationTest {

	@Retention(RUNTIME)
	@Target(TYPE_USE)
	@interface TypeAnnotation {}

	@Retention(RUNTIME)
	@Target(PARAMETER)
	@interface ParameterAnnotation {}

	@SuppressWarnings("unused")
	static class TestClass {

		@Inject
		private TestClass(Object parameter) {
		}

		private void method(@Named("namedParameter") String parameter1,
		                    @TypeAnnotation @ParameterAnnotation Map<? extends CharSequence, String> parameter2) {
		}
	}


	@Nested
	@DisplayName("with method parameter")
	class WithMethodParameter {

		private ReflectiveParameterInformation parameterInformation;
		private Method method;

		@BeforeEach
		void setup() throws Exception {
			method = TestClass.class.getDeclaredMethod("method", String.class, Map.class);
			Parameter parameter = method.getParameters()[1];
			parameterInformation = new ReflectiveParameterInformation(parameter);
		}

		@Test
		@DisplayName("name() returns the parameter name")
		void name_returnsParameterName() {
			String name = parameterInformation.name();

			assertEquals("arg1", name);
		}

		@Test
		@DisplayName("type() returns the type information of the parameter.")
		void type_returnsParameterTypeInformation() {
			var typeFactory = TypeFactory.getInstance();

			TypeInstance type = parameterInformation.type();

			var expectedType =
					typeFactory.parameterized(Map.class, typeFactory.wildcardWithUpperBound(typeFactory.of(CharSequence.class)),
							           typeFactory.of(String.class))
					           .withTypeAnnotations(List.of(newAnnotationBuilder(TypeAnnotation.class).build(),
							           newAnnotationBuilder(ParameterAnnotation.class).build()));
			assertEquals(expectedType, type);
		}

		@Test
		@DisplayName("annotations() returns the parameter annotations.")
		void annotations_returnsEmptyList() {
			Collection<AnnotationInfo> annotations = parameterInformation.annotations();

			List<AnnotationInformation> expectedParameterAnnotations =
					List.of(newAnnotationBuilder(ParameterAnnotation.class).build());
			assertEquals(expectedParameterAnnotations, annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns parameter annotations.")
		void annotationInformation_returnsEmptyList() {
			List<AnnotationInformation> annotationInformation = parameterInformation.annotationInformation();

			List<AnnotationInformation> expectedParameterAnnotations =
					List.of(newAnnotationBuilder(ParameterAnnotation.class).build());
			assertEquals(expectedParameterAnnotations, annotationInformation);
		}
	}

	@Nested
	@DisplayName("with annotated method parameter")
	class WithAnnotatedMethodParameter {

		private ReflectiveParameterInformation parameterInformation;
		private Method method;

		@BeforeEach
		void setup() throws Exception {
			method = TestClass.class.getDeclaredMethod("method", String.class, Map.class);
			Parameter parameter = method.getParameters()[0];
			parameterInformation = new ReflectiveParameterInformation(parameter);
		}

		@Test
		@DisplayName("annotations() returns a list of annotation information for the parameter.")
		void annotations_returnsParameterAnnotations() {
			Collection<AnnotationInfo> annotations = parameterInformation.annotations();

			var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("namedParameter")));
			assertEquals(expectedAnnotations, annotations);
		}

		@Test
		@DisplayName("annotationInformation() returns a list of annotation information for the parameter.")
		void annotationInformation_returnsParameterAnnotation() {
			List<AnnotationInformation> annotationInformation = parameterInformation.annotationInformation();

			var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("namedParameter")));
			assertEquals(expectedAnnotations, annotationInformation);
		}
	}

	@Nested
	@DisplayName("with method parameter with repeated annotation")
	class WithMethodParameterWithRepeatedAnnotation {

		@Retention(RUNTIME)
		@Target(PARAMETER)
		public @interface RepeatedAnnotationHolder {
			RepeatedAnnotation[] value();
		}

		@Repeatable(RepeatedAnnotationHolder.class)
		@Retention(RUNTIME)
		@Target(PARAMETER)
		public @interface RepeatedAnnotation {
			String value();
		}

		void method(@RepeatedAnnotation("a") @RepeatedAnnotation("b") String string) {
		}

		private ReflectiveParameterInformation parameterInformation;

		@BeforeEach
		void setup() throws Exception {
			parameterInformation = new ReflectiveParameterInformation(
					WithMethodParameterWithRepeatedAnnotation.class.getDeclaredMethod("method", String.class).getParameters()[0]);
		}

		@Test
		@DisplayName("annotationInformation() returns only the wrapper annotation.")
		void annotationInformation_returnsParameterAnnotations() {
			List<AnnotationInformation> annotations = parameterInformation.annotationInformation();

			var foo = new SyntheticAnnotationInformationBuilder(
					new ReflectiveClassInformation<>(RepeatedAnnotationHolder.class)).value(new AnnotationInfo[]{
					new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(RepeatedAnnotation.class)).value(
							"a").build(),
					new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(RepeatedAnnotation.class)).value(
							"b").build()}).build();
			assertEquals(List.of(foo), annotations);
		}

		@Test
		@DisplayName("repeatableAnnotation(Class<? extends Annotation>) returns instances of the repeated annotation.")
		void repeatableAnnotation_returnsRepeatedAnnotations() {
			Collection<AnnotationInfo> annotations = parameterInformation.repeatableAnnotation(RepeatedAnnotation.class);

			var expectedAnnotations = List.of(
					new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(RepeatedAnnotation.class)).value(
							"a").build(),
					new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(RepeatedAnnotation.class)).value(
							"b").build());
			assertEquals(expectedAnnotations, annotations);
		}

	}

	@Nested
	@DisplayName("with constructor parameter")
	class WithConstructorParameter {

		private ReflectiveParameterInformation parameterInformation;
		private Constructor<TestClass> constructor;

		@BeforeEach
		void setup() throws Exception {
			constructor = TestClass.class.getDeclaredConstructor(Object.class);
			Parameter parameter = constructor.getParameters()[0];
			parameterInformation = new ReflectiveParameterInformation(parameter);
		}

		@Test
		@DisplayName("declaringMethod() returns information about the declaring constructor.")
		void declaringMethod_returnsConstructorInformation() {
			ExecutableInformation declaringConstructor = parameterInformation.declaringMethod();

			assertEquals(new ReflectiveConstructorInformation<TestClass>(constructor), declaringConstructor);
		}
	}

	private static SyntheticAnnotationInformationBuilder newAnnotationBuilder(Class<? extends Annotation> annotation) {
		return new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(annotation));
	}
}