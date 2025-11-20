package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.reference.LateReference;

public record ConstructorConfiguration<T>(ConstructorInformation<T> info, List<AnnotationInformation> annotations,
                                          List<ParameterConfiguration> parameterConfigurations) implements
		ExecutableConfiguration {

	public ConstructorConfiguration {
		annotations = new ArrayList<>(annotations);
		parameterConfigurations = List.copyOf(parameterConfigurations);
	}

	@Override
	public ConstructorConfiguration<T> addAnnotation(Class<? extends Annotation> annotationType) {
		return addAnnotation(AnnotationBuilder.of(annotationType).build());
	}

	@Override
	public ConstructorConfiguration<T> addAnnotation(AnnotationInfo annotation) {
		if (annotation instanceof AnnotationInformation annotationInfo) {
			annotations.add(annotationInfo);

			return this;
		}

		throw new IllegalArgumentException("Unsupported annotation");
	}

	@Override
	public ConstructorConfiguration<T> addAnnotation(Annotation annotation) {
		return addAnnotation(new ReflectiveAnnotationInformation<>(annotation));
	}

	@Override
	public ConstructorConfiguration<T> removeAnnotation(Predicate<AnnotationInfo> predicate) {
		annotations.removeIf(predicate);
		return this;
	}

	@Override
	public ConstructorConfiguration<T> removeAllAnnotations() {
		annotations.clear();

		return this;
	}

	public static <T> ConstructorConfiguration<T> fromConstructorInformation(ConstructorInformation<T> info) {
		var recursiveReference = new LateReference<ExecutableConfiguration>();
		List<ParameterConfiguration> parameters = info.parameterInformation()
		                                              .stream()
		                                              .map(parameter -> new ParameterConfiguration(parameter,
				                                                new ArrayList<>(parameter.annotationInformation()),
				                                                recursiveReference))
		                                              .toList();
		ConstructorConfiguration<T> constructorConfiguration =
				new ConstructorConfiguration<>(info, new ArrayList<>(info.annotationInformation()), parameters);

		recursiveReference.setValue(constructorConfiguration);

		return constructorConfiguration;
	}

}
