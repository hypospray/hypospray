package eu.jbeernink.hypospray.model.information.reflection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record ReflectiveFieldInformation(Field fieldInstance) implements FieldInformation {
	@Override
	public ClassInformation<?> declaringClass() {
		return new ReflectiveClassInformation<>(fieldInstance.getDeclaringClass());
	}

	@Override
	public String name() {
		return fieldInstance.getName();
	}

	@Override
	public TypeInstance type() {
		return TypeFactory.getInstance().fromAnnotatedType(fieldInstance.getAnnotatedType());
	}

	@Override
	public int modifiers() {
		return fieldInstance.getModifiers();
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return Arrays.stream(fieldInstance.getDeclaredAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(Collectors.toUnmodifiableList());
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case FieldInformation other -> Objects.equals(declaringClass(), other.declaringClass()) && Objects.equals(name(), other.name());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(declaringClass(), name());
	}
}
