package eu.jbeernink.hypospray.model.information.synthetic.builder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticPackageInformation;

@DisplayName("SyntheticPackageInformationBuilder")
class SyntheticPackageInformationBuilderTest {

	@Test
	@DisplayName("addAnnotation(Annotation) adds the annotation to the builder.")
	void addAnnotation_annotation_addsAnnotationToBuilder() {
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("eu.jbeernink.hypospray.test");
		var annotation = NamedLiteral.of("test");

		builder.addAnnotation(annotation);

		SyntheticPackageInformation packageInformation = builder.build();
		var expectedAnnotations = List.of(new ReflectiveAnnotationInformation<>(annotation));
		assertEquals(expectedAnnotations, packageInformation.annotationInformation());
	}

	@Test
	@DisplayName("addAnnotation(AnnotationInformation) adds the annotation to the builder.")
	void addAnnotation_annotationInformation_addsAnnotationToBuilder() {
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("test");
		var annotation = new SyntheticAnnotationInformationBuilderFactory().create(Named.class)
		                                                                   .member("value", "annotationInfo")
		                                                                   .build();

		builder.addAnnotation(annotation);

		SyntheticPackageInformation packageInformation = builder.build();
		var expectedAnnotations = List.of(annotation);
		assertEquals(expectedAnnotations, packageInformation.annotationInformation());
	}

	@Test
	@DisplayName("addAnnotation(Class<?>, Consumer<AnnotationBuilder>) adds the annotation to the builder.")
	void addAnnotation_class_consumer_addsAnnotationToBuilder() {
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("test");

		builder.addAnnotation(Named.class, annotationBuilder -> annotationBuilder.value("This is a test."));

		var expectedAnnotations = List.of(
				new SyntheticAnnotationInformationBuilderFactory().create(Named.class).value("This is a test.").build());
		assertEquals(expectedAnnotations, builder.build().annotationInformation());
	}

	@Test
	@DisplayName(
			"addAnnotation(ClassInformation<? extends Annotation>, Consumer<AnnotationBuilder>) adds the annotation to the builder.")
	void addAnnotation_classInformation_consumer_addsAnnotationToBuilder() {
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("test");

		builder.addAnnotation(new ReflectiveClassInformation<>(Named.class),
				annotationBuilder -> annotationBuilder.value("Still testing."));

		var expectedAnnotations =
				List.of(new SyntheticAnnotationInformationBuilderFactory().create(Named.class).value("Still testing.").build());
		assertEquals(expectedAnnotations, builder.build().annotationInformation());
	}

	@Test
	@DisplayName(
			"addAllAnnotations(Iterable<Annotation>) with a collection of annotations, adds all annotations to the builder.")
	void addAllAnnotations_withCollection_addsAnnotationsToBuilder() {
		var syntheticAnnotationInformationBuilderFactory = new SyntheticAnnotationInformationBuilderFactory();
		AnnotationInformation annotation1 =
				syntheticAnnotationInformationBuilderFactory.create(Named.class).member("value", "test").build();
		AnnotationInformation annotation2 = syntheticAnnotationInformationBuilderFactory.create(Vetoed.class).build();
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("test");

		builder.addAllAnnotations(List.of(annotation1, annotation2));

		SyntheticPackageInformation packageInformation = builder.build();
		var expectedAnnotations = List.of(annotation1, annotation2);
		assertEquals(expectedAnnotations, packageInformation.annotationInformation());
	}

	@Test
	@DisplayName(
			"addAllAnnotations(Iterable<Annotation>) with an iterable containing annotations, adds all annotations to the builder.")
	void addAllAnnotations_withIterable_addsAnnotationsToBuilder() {
		var syntheticAnnotationInformationBuilderFactory = new SyntheticAnnotationInformationBuilderFactory();
		AnnotationInformation annotation1 =
				syntheticAnnotationInformationBuilderFactory.create(Named.class).member("value", "testing").build();
		AnnotationInformation annotation2 = syntheticAnnotationInformationBuilderFactory.create(Singleton.class).build();
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("test");
		Iterable<AnnotationInformation> annotations = Stream.of(annotation1, annotation2)::iterator;

		builder.addAllAnnotations(annotations);

		SyntheticPackageInformation packageInformation = builder.build();
		var expectedAnnotations = List.of(annotation1, annotation2);
		assertEquals(expectedAnnotations, packageInformation.annotationInformation());
	}

	@Test
	@DisplayName("build() returns the expected package information.")
	void build_returnsExpectedPackageInformation() {
		var builder = SyntheticPackageInformationBuilder.newPackageBuilder("eu.jbeernink.hypospray.test")
		                                                .addAnnotation(Vetoed.Literal.INSTANCE);

		SyntheticPackageInformation packageInformation = builder.build();


		var expectedPackageInformation = new SyntheticPackageInformation("eu.jbeernink.hypospray.test",
				List.of(new ReflectiveAnnotationInformation<>(Vetoed.Literal.INSTANCE)));
		assertEquals(expectedPackageInformation, packageInformation);
	}

	@Test
	@DisplayName("newPackageBuilder(PackageInformation) creates a new builder based on the given package information.")
	void newPackageBuilder_packageInformation_createsNewPackageInformation() {
		var packageInformation = new SyntheticPackageInformation("test",
				List.of(new ReflectiveAnnotationInformation<>(NamedLiteral.of("test"))));

		var builder = SyntheticPackageInformationBuilder.newPackageBuilder(packageInformation);

		var actualPackageInformation = builder.build();
		assertAll(() -> assertEquals(packageInformation, actualPackageInformation),
				() -> assertEquals(packageInformation.annotationInformation(),
						actualPackageInformation.annotationInformation()));
	}
}