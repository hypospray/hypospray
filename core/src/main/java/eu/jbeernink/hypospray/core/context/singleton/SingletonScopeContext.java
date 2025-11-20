package eu.jbeernink.hypospray.core.context.singleton;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.inject.Singleton;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Pseudo-scope for contextuals which are a singleton during the lifetime of the container.
@NullMarked
public class SingletonScopeContext implements Context {

	private record ContextualInstance<T>(Contextual<T> contextual, CreationalContext<T> creationalContext, T instance) {}

	private final Map<Contextual<?>, ContextualInstance<?>> beans = new ConcurrentHashMap<>();

	@Override
	public Class<? extends Annotation> getScope() {
		return Singleton.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		return (T) beans.computeIfAbsent(contextual, _ -> {
			T instance = contextual.create(creationalContext);

			return new ContextualInstance<T>(contextual, creationalContext, instance);
		}).instance();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T get(Contextual<T> contextual) {
		if (!beans.containsKey(contextual)) {
			return null;
		}

		return (T) beans.get(contextual).instance;
	}

	@Override
	public boolean isActive() {
		return true;
	}
}
