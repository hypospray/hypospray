package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.util.Map;

import jakarta.enterprise.inject.build.compatible.spi.Parameters;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Immutable parameters to be passed to [jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator] and [jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer].
@NullMarked
public record ImmutableParameters(Map<String, Object> parameters) implements Parameters {
	@Override
	public <T> @Nullable T get(String key, Class<T> type) {
		if (!parameters.containsKey(key)) {
			return null;
		}

		return type.cast(parameters.get(key));
	}

	@Override
	public <T> T get(String key, Class<T> type, T defaultValue) {
		if (!parameters.containsKey(key)) {
			return defaultValue;
		}

		return type.cast(parameters.get(key));
	}
}
