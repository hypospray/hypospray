package eu.jbeernink.hypospray.model.information;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Modifier;
import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.types.TypeInstance;

public sealed interface ExecutableInformation extends MethodInfo, AnnotatedDeclaration, ClassMember permits
		MethodInformation, ConstructorInformation {

	@Override
	@Nullable
	TypeInstance receiverType();

	default String methodIdentifier() {
		String parameters = parameterTypes().stream().map(TypeInstance::descriptorString).collect(joining());
		return switch (this) {
			case MethodInformation methodInformation ->
					"%s[(%s)%s]".formatted(methodInformation.name(), parameters, returnType().descriptorString());
			case ConstructorInformation<?> _ -> "new[(%s)%s]".formatted(parameters, returnType().descriptorString());
		};
	}

	@Override
	default List<ParameterInfo> parameters() {
		return List.copyOf(parameterInformation());
	}

	List<ParameterInformation> parameterInformation();

	/// The types of parameter this executable accepts.
	///
	/// The types will appear in the same order as they do in this executable.
	///
	/// @return a list of parameter types.
	default List<TypeInstance> parameterTypes() {
		return parameterInformation().stream().map(ParameterInformation::type).toList();
	}

	TypeInstance returnType();

	List<TypeInstance> throwsTypeInstances();

	@Override
	default List<Type> throwsTypes() {
		return List.copyOf(throwsTypeInstances());
	}

	List<AnnotationInformation> annotationInformation();

	// TODO move reliance on mutability.
	@Deprecated
	default List<ParameterInformation> mutableParameters() {
		throw new UnsupportedOperationException();
	}

	default List<AnnotationInfo> annotations() {
		return List.copyOf(annotationInformation());
	}

	@Override
	default boolean isConstructor() {
		return switch (this) {
			case ConstructorInformation<?> _ -> true;
			case MethodInformation _ -> false;
		};
	}

	@Override
	default boolean isStatic() {
		return Modifier.isStatic(modifiers());
	}

	@Override
	default boolean isAbstract() {
		return Modifier.isAbstract(modifiers());
	}

	@Override
	default boolean isFinal() {
		return Modifier.isFinal(modifiers());
	}
}
