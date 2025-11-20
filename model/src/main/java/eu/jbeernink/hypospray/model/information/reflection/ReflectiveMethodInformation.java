package eu.jbeernink.hypospray.model.information.reflection;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.lang.model.types.TypeVariable;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record ReflectiveMethodInformation(Method methodInstance) implements MethodInformation {

	@Override
	public @Nullable TypeInstance receiverType() {
		if (isStatic()) {
			return null;
		}
		return TypeFactory.getInstance().fromAnnotatedType(methodInstance.getAnnotatedReceiverType());
	}

	@Override
	public List<TypeVariable> typeParameters() {
		TypeFactory typeFactory = TypeFactory.getInstance();
		return Arrays.stream(methodInstance.getTypeParameters())
		             .map(typeFactory::fromJavaType)
		             .map(TypeInstance::asTypeVariable)
		             .collect(toUnmodifiableList());
	}

	@Override
	public int modifiers() {
		return methodInstance.getModifiers();
	}

	@Override
	public ClassInformation<?> declaringClass() {
		return new ReflectiveClassInformation<>(methodInstance.getDeclaringClass());
	}

	@Override
	public List<ParameterInformation> parameterInformation() {
		return Arrays.stream(methodInstance.getParameters())
		             .map(ReflectiveParameterInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public String name() {
		return methodInstance.getName();
	}

	@Override
	public TypeInstance returnType() {
		return TypeFactory.getInstance().of(methodInstance.getReturnType());
	}

	@Override
	public List<TypeInstance> throwsTypeInstances() {
		return Arrays.stream(methodInstance.getAnnotatedExceptionTypes())
		             .map(TypeFactory.getInstance()::fromAnnotatedType)
		             .toList();
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return Arrays.stream(methodInstance.getDeclaredAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return switch (obj) {
			case MethodInformation other ->
					Objects.equals(declaringClass(), other.declaringClass()) && Objects.equals(name(), other.name()) &&
					Objects.equals(parameterDescriptors(parameterTypes()), parameterDescriptors(other.parameterTypes()));
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(declaringClass(), name(), parameterDescriptors(parameterTypes()));
	}

	private static List<String> parameterDescriptors(List<TypeInstance> parameterTypes) {
		return parameterTypes.stream().map(TypeInstance::descriptorString).toList();
	}
}
