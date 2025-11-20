package eu.jbeernink.hypospray.model.types;

import static eu.jbeernink.hypospray.model.todo.Todo.unimplemented;
import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static jakarta.enterprise.lang.model.types.Type.Kind.WILDCARD_TYPE;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.WildcardType;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;

@NullMarked
public record WildcardTypeInstance(List<TypeInstance> upperBounds, List<TypeInstance> lowerBounds,
                                   List<AnnotationInformation> typeAnnotations) implements WildcardType, TypeInstance {

	public WildcardTypeInstance {
		upperBounds = List.copyOf(upperBounds);
		lowerBounds = List.copyOf(lowerBounds);
		typeAnnotations = List.copyOf(typeAnnotations);
	}

	@Override
	public Type asJavaType() {
		return unimplemented();
	}

	@Override
	public @Nullable TypeInstance upperBound() {
		// Throw an exception as the WildcardType API does not support multiple upper bounds yet.
		return upperBounds.stream()
		                  .collect(findOnly(() -> new IllegalStateException("Wildcard has more than one upper bound.")))
		                  .orElse(null);
	}

	@Override
	public @Nullable TypeInstance lowerBound() {
		// Throw an exception as the WildcardType API does not support multiple lower bounds yet.
		return lowerBounds.stream()
		                  .collect(findOnly(() -> new IllegalStateException("Wildcard has more than one lower bound.")))
		                  .orElse(null);
	}

	@Override
	public Kind kind() {
		return WILDCARD_TYPE;
	}

	@Override
	public boolean isAssignableFrom(TypeInstance other) {
		return switch (other) {
			case ClassTypeInstance _, ParameterizedTypeInstance _ when !lowerBounds.isEmpty() ->
					upperBounds.stream().allMatch(other::isAssignableFrom) &&
					lowerBounds.stream().allMatch(lowerBound -> lowerBound.isAssignableFrom(other));
			case ArrayTypeInstance _, ClassTypeInstance _, ParameterizedTypeInstance _, PrimitiveTypeInstance _,
			     TypeVariableInstance _, VoidTypeInstance _, WildcardTypeInstance _ -> false;
		};
	}

	@Override
	public WildcardTypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return new WildcardTypeInstance(upperBounds, lowerBounds, annotations);
	}
}
