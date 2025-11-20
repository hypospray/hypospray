package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;


public record ClassConfiguration<T>(ClassInformation<T> info, List<AnnotationInformation> annotations,
                                    List<ConstructorConfiguration<T>> constructorConfigurations,
                                    List<MethodConfiguration> methodConfigurations,
                                    List<FieldConfiguration> fieldConfigurations) implements ClassConfig,
		AnnotatedConfiguration {

	public ClassConfiguration {
		annotations = new ArrayList<>(annotations);
		constructorConfigurations = List.copyOf(constructorConfigurations);
		methodConfigurations = List.copyOf(methodConfigurations);
		fieldConfigurations = List.copyOf(fieldConfigurations);
	}

	@Override
	public List<MethodConfig> constructors() {
		return List.copyOf(constructorConfigurations);
	}

	public List<MethodConfig> methods() {
		return List.copyOf(methodConfigurations);
	}

	@Override
	public ClassConfiguration<T> addAnnotation(Class<? extends Annotation> annotationType) {
		return addAnnotation(AnnotationBuilder.of(annotationType).build());
	}

	@Override
	public ClassConfiguration<T> addAnnotation(AnnotationInfo annotation) {
		return addAnnotation(AnnotationInformation.fromAnnotationInfo(annotation));
	}

	@Override
	public ClassConfiguration<T> addAnnotation(Annotation annotation) {
		return addAnnotation(new ReflectiveAnnotationInformation<>(annotation));
	}

	public ClassConfiguration<T> addAnnotation(AnnotationInformation annotationInformation) {
		annotations.add(annotationInformation);
		return this;
	}

	@Override
	public ClassConfiguration<T> removeAnnotation(Predicate<AnnotationInfo> predicate) {
		annotations.removeIf(predicate);
		return this;
	}

	@Override
	public ClassConfiguration<T> removeAllAnnotations() {
		annotations.clear();

		return this;
	}

	public static <T> ClassConfiguration<T> fromClassInfo(ClassInformation<T> info) {
		return new ClassConfiguration<>(info, new ArrayList<>(info.annotationInformation()), getConstructors(info),
				getMethods(info), getFields(info));
	}

	private static <T> List<ConstructorConfiguration<T>> getConstructors(ClassInformation<T> info) {
		return info.constructorInformation().stream().map(ConstructorConfiguration::fromConstructorInformation).toList();
	}

	private static <T> List<MethodConfiguration> getMethods(ClassInformation<T> info) {
		return info.methodInformation().stream().map(MethodConfiguration::fromMethodInfo).toList();
	}

	private static <T> List<FieldConfiguration> getFields(ClassInformation<T> info) {
		return info.fieldInformation().stream().map(FieldConfiguration::fromFieldInformation).toList();
	}

	@Override
	public Collection<FieldConfig> fields() {
		return List.copyOf(fieldConfigurations);
	}

	public boolean isVetoed() {
		return hasAnnotation(Vetoed.class) || info.packageInfo().hasAnnotation(Vetoed.class);
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case ClassConfiguration<?>(ClassInformation<?> otherInfo, _, _, _, _) -> info.equals(otherInfo);
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return info.hashCode();
	}
}
