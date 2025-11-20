package eu.jbeernink.hypospray.model.information.builder;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticPackageInformationBuilder;

/// Builder for a [PackageInformation] instance.
///
/// @param <E> the type of the builder class.
public sealed interface PackageBuilder<E extends PackageBuilder<E>> permits SyntheticPackageInformationBuilder {

	/// Add the given [Annotation] to the package.
	///
	/// @param annotation the annotation to add.
	/// @return the current builder.
	default E addAnnotation(Annotation annotation) {
		return addAnnotation(new ReflectiveAnnotationInformation<>(annotation));
	}

	/// Add the given [AnnotationInformation] to the package.
	///
	/// @param annotationInformation the annotation information to add.
	/// @return the current builder.
	E addAnnotation(AnnotationInformation annotationInformation);

	default E addAnnotation(Class<? extends Annotation> annotationClass, Consumer<AnnotationBuilder> annotationCreator) {
		return addAnnotation(ClassInformationSource.getInstance().getClassInformation(annotationClass), annotationCreator);
	}

	E addAnnotation(ClassInformation<? extends Annotation> annotationClass,
	                Consumer<AnnotationBuilder> annotationCreator);

	E addAllAnnotations(Iterable<AnnotationInformation> annotations);
}
