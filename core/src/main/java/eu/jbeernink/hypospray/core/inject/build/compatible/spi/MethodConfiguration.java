package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.ParameterConfig;
import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;

public record MethodConfiguration(MethodInformation info, List<AnnotationInformation> annotations,
                                  List<ParameterConfiguration> parameterConfigurations) implements
		ExecutableConfiguration {

	public MethodConfiguration {
		annotations = new ArrayList<>(annotations);
		parameterConfigurations = List.copyOf(parameterConfigurations);
	}

	@Override
	public MethodConfiguration addAnnotation(Class<? extends Annotation> annotationType) {
		return addAnnotation(AnnotationBuilder.of(annotationType).build());
	}

	@Override
	public MethodConfiguration addAnnotation(AnnotationInfo annotation) {
		return switch (annotation) {
			case AnnotationInformation annotationInformation -> addAnnotation(annotationInformation);
			case AnnotationInfo ignored -> throw new IllegalArgumentException("Unsupported annotation info: " + annotation);
		};
	}

	public MethodConfiguration addAnnotation(AnnotationInformation annotation) {
		annotations.add(annotation);

		return this;
	}

	@Override
	public MethodConfiguration addAnnotation(Annotation annotation) {
		return addAnnotation(new ReflectiveAnnotationInformation<>(annotation));
	}

	@Override
	public MethodConfiguration removeAnnotation(Predicate<AnnotationInfo> predicate) {
		annotations.removeIf(predicate);
		return this;
	}

	@Override
	public MethodConfiguration removeAllAnnotations() {
		annotations.clear();
		return this;
	}

	public static MethodConfiguration fromMethodInfo(MethodInformation info) {
		return new MethodConfiguration(info, info.annotationInformation(),
				info.parameterInformation().stream().map(ParameterConfiguration::fromParameterInformation).toList());
	}

	@Override
	public List<ParameterConfig> parameters() {
		return List.copyOf(parameterConfigurations);
	}
}
