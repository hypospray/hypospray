package eu.jbeernink.hypospray.model.information.reflection;

import static eu.jbeernink.hypospray.model.todo.Todo.warnNotYetImplemented;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.TypeVariable;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// [ConstructorInformation] that wraps a [Constructor] using the reflection API.
public record ReflectiveConstructorInformation<T>(Constructor<T> constructorInstance) implements
		ConstructorInformation<T> {
	@Override
	public List<ParameterInformation> parameterInformation() {
		if (declaringClass().isEnum()) {
			// Skip the first two parameters in an enum constructor.
			return Arrays.stream(constructorInstance.getParameters())
			             .skip(2)
			             .map(ReflectiveParameterInformation::new)
			             .collect(toUnmodifiableList());
		}

		return Arrays.stream(constructorInstance.getParameters())
		             .filter(parameter -> !parameter.isSynthetic())
		             .map(ReflectiveParameterInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public ClassInformation<T> declaringClass() {
		return new ReflectiveClassInformation<>(constructorInstance.getDeclaringClass());
	}

	@Override
	public String name() {
		return constructorInstance.getName();
	}

	@Override
	public TypeInstance returnType() {
		return TypeFactory.getInstance().fromAnnotatedType(constructorInstance.getAnnotatedReturnType());
	}

	@Override
	public List<TypeVariable> typeParameters() {
		warnNotYetImplemented();
		return List.of();
	}

	@Override
	public int modifiers() {
		return constructorInstance.getModifiers();
	}

	@Override
	public List<TypeInstance> throwsTypeInstances() {
		warnNotYetImplemented();
		return List.of();
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return Arrays.stream(constructorInstance.getAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
		return Arrays.stream(annotationType.getAnnotationsByType(annotationType))
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case ConstructorInformation<?> other -> Objects.equals(declaringClass(), other.declaringClass()) &&
			                                        Objects.equals(parameterInformation(), other.parameterInformation());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(declaringClass(), parameterInformation());
	}
}
