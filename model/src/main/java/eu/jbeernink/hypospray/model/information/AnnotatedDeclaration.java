package eu.jbeernink.hypospray.model.information;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.AnnotationTarget;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.annotation.ArrayValue;
import eu.jbeernink.hypospray.model.annotation.NestedAnnotationValue;

public sealed interface AnnotatedDeclaration extends AnnotationTarget permits ClassInformation, ClassMember,
		ExecutableInformation, FieldInformation, PackageInformation, ParameterInformation, RecordComponentInformation {

	List<AnnotationInformation> annotationInformation();

	@Override
	default Collection<AnnotationInfo> annotations() {
		return List.copyOf(annotationInformation());
	}

	@Override
	default boolean hasAnnotation(Class<? extends Annotation> annotationType) {
		return hasAnnotation(annotationInfo -> switch (annotationInfo) {
			case AnnotationInformation annotationInformation ->
					annotationType.getName().equals(annotationInformation.declaration().name());
			case AnnotationInfo info -> throw new IllegalStateException(
					"Only AnnotationInfo instances created by the container are supported: %s".formatted(info.getClass()));
		});
	}

	default List<AnnotationInformation> findAnnotations(Predicate<AnnotationInformation> predicate) {
		return annotationInformation().stream().filter(predicate).toList();
	}

	@Override
	default boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
		return annotations().stream().anyMatch(predicate);
	}

	@Override
	default <T extends Annotation> @Nullable AnnotationInfo annotation(Class<T> annotationType) {
		List<AnnotationInformation> matchingAnnotations = findAnnotations(
				annotationInformation -> annotationInformation.declaration().name().equals(annotationType.getName()));

		return matchingAnnotations.stream()
		                          .collect(findOnly(() -> new IllegalStateException(
				                          "More than one annotation found of type: %s".formatted(annotationType.getName()))))
		                          .orElse(null);
	}

	@Override
	default <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
		return annotationInformation().stream().flatMap((annotationInformation) -> {
			if (annotationInformation.declaration().name().equals(annotationType.getName())) {
				return Stream.of(annotationInformation);
			} else if (isContainerAnnotationFor(annotationInformation, annotationType)) {
				return annotationInformation.value().asArray().stream().map(AnnotationMember::asNestedAnnotation);
			}
			return Stream.empty();
		}).toList();
	}

	@Override
	default Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
		return annotations().stream().filter(predicate).toList();
	}

	private static boolean isContainerAnnotationFor(AnnotationInformation annotationInformation,
	                                                Class<? extends Annotation> annotationType) {
		if (annotationInformation.hasValue() && annotationInformation.value() instanceof ArrayValue(var values) &&
		    !values.isEmpty()) {
			if (values.getFirst() instanceof NestedAnnotationValue(AnnotationInformation annotation) &&
			    annotation.declaration().name().equals(annotationType.getName()) &&
			    annotation.declaration().hasAnnotation(Repeatable.class)) {
				return annotation.declaration()
				                 .annotation(Repeatable.class)
				                 .value()
				                 .asType()
				                 .asClass()
				                 .declaration()
						.equals(annotationInformation.declaration());
			}
		}
		return false;
	}
}
