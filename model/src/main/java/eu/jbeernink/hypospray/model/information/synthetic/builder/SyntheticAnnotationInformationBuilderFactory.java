package eu.jbeernink.hypospray.model.information.synthetic.builder;

import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;

/// Factory to create [SyntheticAnnotationInformationBuilder] instances that create [SyntheticAnnotationInformation] instances.
public class SyntheticAnnotationInformationBuilderFactory implements AnnotationBuilderFactory {

	@Override
	public SyntheticAnnotationInformationBuilder create(Class<? extends Annotation> annotationType) {
		return new SyntheticAnnotationInformationBuilder(ClassInformationSource.getInstance()
		                                                                       .getClassInformation(annotationType));
	}

	@Override
	public SyntheticAnnotationInformationBuilder create(ClassInfo annotationType) {
		if (!annotationType.isAnnotation()) {
			throw new IllegalArgumentException("Class must be an annotation type: %s".formatted(annotationType.name()));
		}
		return switch (annotationType) {
			case ClassInformation<?> classInformation -> {
				// Should be safe to cast, we already know this represents an annotation type.
				@SuppressWarnings("unchecked") ClassInformation<? extends Annotation> annotationClassInformation =
						(ClassInformation<? extends Annotation>) classInformation;

				yield new SyntheticAnnotationInformationBuilder(annotationClassInformation);
			}
			case ClassInfo _ -> throw new IllegalArgumentException(
					"Only ClassInfo instances created by the container are supported: %s".formatted(
							annotationType.getClass()));
		};
	}
}
