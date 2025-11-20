package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;


public record FieldConfiguration(FieldInformation info, List<AnnotationInformation> annotations) implements
		FieldConfig, AnnotatedConfiguration {

	public FieldConfiguration {
		annotations = new ArrayList<>(annotations);
	}

	@Override
	public FieldConfiguration addAnnotation(Class<? extends Annotation> annotationType) {
		return addAnnotation(AnnotationBuilder.of(annotationType).build());
	}

	@Override
	public FieldConfiguration addAnnotation(AnnotationInfo annotation) {
		annotations().add(AnnotationInformation.fromAnnotationInfo(annotation));
		return this;
	}

	@Override
	public FieldConfiguration addAnnotation(Annotation annotation) {
		annotations().add(new ReflectiveAnnotationInformation<>(annotation));
		return this;
	}

	@Override
	public FieldConfiguration removeAnnotation(Predicate<AnnotationInfo> predicate) {
		annotations().removeIf(predicate);
		return this;
	}

	@Override
	public FieldConfiguration removeAllAnnotations() {
		annotations().clear();
		return this;
	}

	public static FieldConfiguration fromFieldInformation(FieldInformation fieldInformation) {
		return new FieldConfiguration(fieldInformation, fieldInformation.annotationInformation());
	}
}
