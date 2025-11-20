package eu.jbeernink.hypospray.model.information.synthetic.builder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;
import eu.jbeernink.hypospray.model.information.builder.PackageBuilder;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticPackageInformation;

/// Builder for [SyntheticPackageInformation] instances.
public final class SyntheticPackageInformationBuilder implements PackageBuilder<SyntheticPackageInformationBuilder> {

	private final String packageName;
	private final List<AnnotationInformation> annotations = new ArrayList<>();

	private SyntheticPackageInformationBuilder(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public SyntheticPackageInformationBuilder addAnnotation(AnnotationInformation annotationInformation) {
		annotations.add(annotationInformation);
		return this;
	}

	@Override
	public SyntheticPackageInformationBuilder addAllAnnotations(Iterable<AnnotationInformation> annotations) {
		switch (annotations) {
			case Collection<AnnotationInformation> collection -> this.annotations.addAll(collection);
			case Iterable<AnnotationInformation> _ -> annotations.forEach(this::addAnnotation);
		}
		return this;
	}

	@Override
	public SyntheticPackageInformationBuilder addAnnotation(ClassInformation<? extends Annotation> annotationClass,
	                                                        Consumer<AnnotationBuilder> annotationCreator) {
		var annotationBuilder = new SyntheticAnnotationInformationBuilder(annotationClass);
		annotationCreator.accept(annotationBuilder);

		annotations.add(annotationBuilder.build());

		return this;
	}

	public SyntheticPackageInformation build() {
		return new SyntheticPackageInformation(packageName, annotations);
	}

	/// Create a new builder for the given package name.
	///
	/// @param packageName the package name of the package to build.
	/// @return a synthetic package information builder.
	public static SyntheticPackageInformationBuilder newPackageBuilder(String packageName) {
		return new SyntheticPackageInformationBuilder(packageName);
	}

	/// Create a new builder from the given [PackageInformation].
	///
	/// @param packageInformation the package information to create the builder from.
	/// @return a synthetic package information builder.
	public static SyntheticPackageInformationBuilder newPackageBuilder(PackageInformation packageInformation) {
		return new SyntheticPackageInformationBuilder(packageInformation.name()).addAllAnnotations(
				packageInformation.annotationInformation());
	}
}
