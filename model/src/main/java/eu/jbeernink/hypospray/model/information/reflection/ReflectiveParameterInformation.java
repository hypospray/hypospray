package eu.jbeernink.hypospray.model.information.reflection;

import static eu.jbeernink.hypospray.model.todo.Todo.unimplemented;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.ExecutableInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record ReflectiveParameterInformation(Parameter parameter) implements ParameterInformation {
	@Override
	public String name() {
		return parameter.getName();
	}

	@Override
	public TypeInstance type() {
		AnnotatedType annotatedType = parameter.getAnnotatedType();
		TypeInstance typeInstance = TypeFactory.getInstance().fromAnnotatedType(annotatedType);

		if (annotationInformation().isEmpty()) {
			return typeInstance;
		}

		List<AnnotationInformation> allAnnotations =
				Stream.concat(typeInstance.typeAnnotations().stream(), annotationInformation().stream()).distinct().toList();
		return typeInstance.withTypeAnnotations(allAnnotations);
	}

	@Override
	public ExecutableInformation declaringMethod() {
		Executable declaringExecutable = parameter.getDeclaringExecutable();
		return switch (declaringExecutable) {
			case Constructor<?> constructor -> newConstructorInformation(constructor);
			case Method _ -> unimplemented();
		};
	}

	private static <T> ConstructorInformation<T> newConstructorInformation(Constructor<T> constructor) {
		return new ReflectiveConstructorInformation<>(constructor);
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return Arrays.stream(parameter.getAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
		// Overridden to provide a more efficient implementation when using reflection.
		return Arrays.stream(parameter.getAnnotationsByType(annotationType))
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public Parameter parameterInstance() {
		return parameter;
	}
}
