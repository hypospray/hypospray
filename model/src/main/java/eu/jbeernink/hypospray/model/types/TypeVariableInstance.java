package eu.jbeernink.hypospray.model.types;

import static eu.jbeernink.hypospray.model.todo.Todo.unimplemented;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.enterprise.lang.model.types.TypeVariable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.TypeVariableOwner;
import eu.jbeernink.hypospray.model.types.lazy.LazyTypeVariableInstance;

public sealed interface TypeVariableInstance extends TypeInstance, TypeVariable permits DeclaredTypeVariableInstance,
		LazyTypeVariableInstance {
	@Override
	default Type asJavaType() {
		return unimplemented();
	}

	@Override
	default TypeVariableInstance asTypeVariable() {
		return this;
	}

	@Override
	default List<jakarta.enterprise.lang.model.types.Type> bounds() {
		return List.copyOf(upperBounds());
	}

	TypeVariableOwner owner();

	String name();

	List<TypeInstance> upperBounds();

	@Override
	default boolean isAssignableFrom(TypeInstance other) {
		return switch (other) {
			case ClassTypeInstance _, ParameterizedTypeInstance _ when upperBounds().isEmpty() -> true;
			case ClassTypeInstance _, ParameterizedTypeInstance _ ->
					upperBounds().stream().allMatch(bound -> bound.isAssignableFrom(other));
			case ArrayTypeInstance _, PrimitiveTypeInstance _, TypeVariableInstance _, VoidTypeInstance _,
			     WildcardTypeInstance _ -> false;
		};
	}
}
