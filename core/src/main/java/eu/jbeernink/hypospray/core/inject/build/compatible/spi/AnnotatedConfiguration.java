package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Qualifier;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;

public sealed interface AnnotatedConfiguration permits ClassConfiguration, ExecutableConfiguration,
		ParameterConfiguration, FieldConfiguration {

	List<AnnotationInformation> annotations();

	default boolean hasAnnotation(Class<? extends Annotation> annotationType) {
		return annotations().stream()
		                    .anyMatch(
				                    annotationInfo -> annotationInfo.declaration().name().equals(annotationType.getName()));
	}

	default Optional<AnnotationInformation> findAnnotation(Class<? extends Annotation> annotationType) {
		return annotations().stream()
		                    .filter(info -> info.declaration().name().equals(annotationType.getName()))
		                    .collect(findOnly(() -> new RuntimeException("Duplicate annotation.")
				                    // TODO improve exception
		                    ));
	}

	default List<AnnotationInformation> qualifiers() {
		return annotations().stream()
		                    .filter(annotation -> annotation.declaration().hasAnnotation(Qualifier.class))
		                    .toList();
	}
}
