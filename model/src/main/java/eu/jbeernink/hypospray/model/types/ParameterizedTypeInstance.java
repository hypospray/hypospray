package eu.jbeernink.hypospray.model.types;

import static eu.jbeernink.hypospray.model.todo.Todo.warnNotYetImplemented;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.enterprise.lang.model.types.ParameterizedType;
import jakarta.enterprise.lang.model.types.Type;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.types.reflection.ParameterizedTypeImpl;

public record ParameterizedTypeInstance(ClassTypeInstance genericClass,
                                        List<TypeInstance> typeArgumentInstances, List<AnnotationInformation> typeAnnotations) implements TypeInstance,
		ParameterizedType {

	public ParameterizedTypeInstance {
		if (genericClass.declaration().numberOfTypeParameters() != typeArgumentInstances.size()) {
			throw new IllegalStateException("Incorrect number of type-arguments, expected %d but got %s".formatted(
					genericClass.declaration().typeParameterInstances().size(), typeArgumentInstances));
		}
		typeArgumentInstances = List.copyOf(typeArgumentInstances);
		typeAnnotations = List.copyOf(typeAnnotations);
	}

	@Override
	public ClassInformation<?> declaration() {
		return genericClass.declaration();
	}

	@Override
	public java.lang.reflect.ParameterizedType asJavaType() {
		return new ParameterizedTypeImpl(genericClass.asJavaType(), null,
				typeArgumentInstances.stream().map(TypeInstance::asJavaType).toList());
	}


	@Override
	public List<Type> typeArguments() {
		return List.copyOf(typeArgumentInstances);
	}

	@Override
	public ParameterizedTypeInstance asParameterizedType() {
		return this;
	}

	public List<TypeInstance> parentTypes() {
		Map<TypeVariableInstance, TypeInstance> typeVariableMapping = IntStream.range(0, typeArgumentInstances.size())
		                                                                       .boxed()
		                                                                       .collect(toMap(
				                                                                       declaration().typeParameterInstances()::get,
				                                                                       typeArgumentInstances::get));

		return Stream.concat(Optional.ofNullable(declaration().superClass()).stream(),
				declaration().superInterfaceTypes().stream()).map(superType -> switch (superType) {
			case ParameterizedTypeInstance parameterizedType -> parameterizedType.replaceTypeVariables(typeVariableMapping);
			case TypeInstance typeInstance -> typeInstance;
		}).toList();
	}

	public ParameterizedTypeInstance replaceTypeVariables(Map<TypeVariableInstance, TypeInstance> typeVariableMapping) {
		List<TypeInstance> updatedTypeArguments = typeArgumentInstances.stream().map(typeArgument -> switch (typeArgument) {
			case TypeVariableInstance typeVariable when typeVariableMapping.containsKey(typeVariable) ->
					typeVariableMapping.get(typeVariable);
			case TypeInstance typeInstance -> typeInstance;
		}).toList();

		return new ParameterizedTypeInstance(genericClass, updatedTypeArguments, List.copyOf(typeAnnotations));
	}

	@Override
	public boolean isAssignableFrom(TypeInstance other) {
		return switch (other) {
			case ParameterizedTypeInstance parameterizedType when this.equals(parameterizedType) -> true;
			case ParameterizedTypeInstance(var otherGenericClass, var otherTypeArguments, _) when genericClass.equals(otherGenericClass) -> {
				for (int i = 0; i < typeArgumentInstances.size(); i++) {
					if (otherTypeArguments.get(i) instanceof TypeVariableInstance typeVariable) {
						if (!typeArgumentInstances.get(i).isAssignableFrom(typeVariable)) {
							yield false;
						}
					}
					else if (!otherTypeArguments.get(i).equals(typeArgumentInstances.get(i))) {
						yield false;
					}
				}

				yield true;
			}
			case ParameterizedTypeInstance parameterizedTypeInstance ->
					parameterizedTypeInstance.parentTypes().stream().anyMatch(this::isAssignableFrom);
			case ClassTypeInstance classType when genericClass.equals(classType) -> true;
			case ClassTypeInstance classType -> classType.parentTypes().stream().anyMatch(this::isAssignableFrom);
			case ArrayTypeInstance _, VoidTypeInstance _, PrimitiveTypeInstance _ -> false;
			case TypeVariableInstance _, WildcardTypeInstance _ -> {
				warnNotYetImplemented();
				yield false;
			}
		};
	}

	@Override
	public ParameterizedTypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return new ParameterizedTypeInstance(genericClass, typeArgumentInstances, annotations);
	}
}
