package eu.jbeernink.hypospray.model.types.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ParameterizedTypeImpl(Class<?> rawType, @Nullable Type ownerType,
                                    List<Type> actualTypeArguments) implements ParameterizedType {

	public ParameterizedTypeImpl {
		actualTypeArguments = List.copyOf(actualTypeArguments);
		if (rawType.getTypeParameters().length != actualTypeArguments.size()) {
			throw new IllegalArgumentException(String.format(
					"Specified type arguments do not match the number of expected type arguments: Expected: %d, Actual: %d.",
					rawType.getTypeParameters().length, actualTypeArguments.size()));
		}
	}

	@Override
	public Type[] getActualTypeArguments() {
		return actualTypeArguments.toArray(Type[]::new);
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParameterizedType other) {
			return Objects.equals(rawType, other.getRawType()) && Objects.equals(ownerType, other.getOwnerType()) &&
			       Objects.equals(actualTypeArguments, Arrays.asList(other.getActualTypeArguments()));
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getActualTypeArguments()) ^ Objects.hashCode(ownerType) ^ Objects.hashCode(rawType);
	}
}
