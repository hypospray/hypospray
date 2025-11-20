package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.ParameterConfig;
import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.reference.LateReference;

public record ParameterConfiguration(ParameterInformation info, List<AnnotationInformation> annotations,
                                     LateReference<ExecutableConfiguration> annotatedCallable) implements
		ParameterConfig, AnnotatedConfiguration {

	public ParameterConfiguration {
		annotations = new ArrayList<>(annotations);
	}

	@Override
	public ParameterConfiguration addAnnotation(Class<? extends Annotation> annotationType) {
		return addAnnotation(AnnotationBuilder.of(annotationType).build());
	}

	@Override
	public ParameterConfiguration addAnnotation(AnnotationInfo annotation) {
		if (annotation instanceof AnnotationInformation annotationInfo) {
			annotations.add(annotationInfo);
		} else {
			throw new IllegalArgumentException(
					"Annotations must be created using AnnotationBuilder. Unsupported AnnotationInfo: " + annotation);
		}

		return this;
	}

	@Override
	public ParameterConfiguration addAnnotation(Annotation annotation) {
		return addAnnotation(new ReflectiveAnnotationInformation<>(annotation));
	}

	@Override
	public ParameterConfiguration removeAnnotation(Predicate<AnnotationInfo> predicate) {
		annotations.removeIf(predicate);

		return this;
	}

	@Override
	public ParameterConfiguration removeAllAnnotations() {
		annotations.clear();

		return this;
	}

	public static ParameterConfiguration fromParameterInformation(ParameterInformation parameterInformation) {
		return new ParameterConfiguration(parameterInformation, parameterInformation.annotationInformation(),
				new LateReference<>());
	}
}
