package eu.jbeernink.hypospray.model.information.synthetic.builder;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.PackageInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;
import jakarta.inject.Named;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("SyntheticAnnotationInformationBuilderFactory")
class SyntheticAnnotationInformationBuilderFactoryTest {

	private final SyntheticAnnotationInformationBuilderFactory factory = new SyntheticAnnotationInformationBuilderFactory();

	@Test
	@DisplayName("create(Class<? extends Annotation>) creates a new builder for the given annotation type.")
	void create_withClass_createsNewBuilderForGivenAnnotationType() {
		Class<Named> annotationClass = Named.class;

		AnnotationBuilder annotationBuilder = factory.create(annotationClass);

		AnnotationInfo info = annotationBuilder.build();
		assertEquals(annotationClass.getName(), info.name());
	}

	@Test
	@DisplayName("create(ClassInfo) with a class that is not an annotation, throws IllegalArgumentException.")
	void create_withNonAnnotationClassInfo_throwsIllegalArgumentException() {
		var classInfo = new ReflectiveClassInformation<>(Object.class);

		var exception = assertThrows(IllegalArgumentException.class, () -> factory.create(classInfo));

		assertEquals("Class must be an annotation type: java.lang.Object", exception.getMessage());
	}

	@Test
	@DisplayName("create(ClassInfo) with custom ClassInfo type, throws IllegalArgumentException.")
	void create_withCustomClassInfo_throwsIllegalArgumentException() {
		var classInfo = new CustomClassInfo();

		var exception = assertThrows(IllegalArgumentException.class, () -> factory.create(classInfo));

		assertEquals("Only ClassInfo instances created by the container are supported: class eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderFactoryTest$CustomClassInfo", exception.getMessage());
	}


	private static class CustomClassInfo implements ClassInfo {

		@Override
		public String name() {
			return "";
		}

		@Override
		public String simpleName() {
			return "";
		}

		@Override
		public PackageInfo packageInfo() {
			return null;
		}

		@Override
		public List<TypeVariable> typeParameters() {
			return List.of();
		}

		@Override
		public Type superClass() {
			return null;
		}

		@Override
		public ClassInfo superClassDeclaration() {
			return null;
		}

		@Override
		public List<Type> superInterfaces() {
			return List.of();
		}

		@Override
		public List<ClassInfo> superInterfacesDeclarations() {
			return List.of();
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
			return true;
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
			return List.of();
		}

		@Override
		public Collection<MethodInfo> methods() {
			return List.of();
		}

		@Override
		public Collection<FieldInfo> fields() {
			return List.of();
		}

		@Override
		public Collection<RecordComponentInfo> recordComponents() {
			return List.of();
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