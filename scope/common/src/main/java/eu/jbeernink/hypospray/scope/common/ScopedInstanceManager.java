package eu.jbeernink.hypospray.scope.common;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

public class ScopedInstanceManager<K extends ScopeInstanceKey> {

	private final Map<K, Map<Contextual<?>, ScopedInstance<?>>> scopeInstances = new ConcurrentHashMap<>();

	public <T> Optional<T> getInstance(K scopeInstanceKey, Contextual<T> contextual) {
		if (scopeInstances.containsKey(scopeInstanceKey)) {
			var beanInstances = scopeInstances.computeIfAbsent(scopeInstanceKey, _ -> new ConcurrentHashMap<>());

			if (beanInstances.containsKey(contextual)) {
				@SuppressWarnings("unchecked") T instance = (T) beanInstances.get(contextual).instance();

				return Optional.of(instance);
			}
		}

		return Optional.empty();
	}

	public <T> T getOrCreateInstance(K scopeKey, Contextual<T> contextual, CreationalContext<T> creationalContext) {
		Map<Contextual<?>, ScopedInstance<?>> beanInstances =
				scopeInstances.computeIfAbsent(scopeKey, _ -> new ConcurrentHashMap<>());

		@SuppressWarnings("unchecked") ScopedInstance<T> scopedInstance =
				(ScopedInstance<T>) beanInstances.computeIfAbsent(contextual,
						_ -> ScopedInstance.create(contextual, creationalContext));

		return scopedInstance.instance();
	}

	public void destroyInstance(K scopeKey, Contextual<?> contextual) {
		if (scopeInstances.containsKey(scopeKey)) {
			Map<Contextual<?>, ScopedInstance<?>> beanInstances = scopeInstances.get(scopeKey);

			ScopedInstance<?> scopedInstance = beanInstances.remove(contextual);

			if (scopedInstance != null) {
				scopedInstance.destroy();
			}
		}
	}

	public void destroyScope(K scopeKey) {
		scopeInstances.computeIfPresent(scopeKey, (_, instancesToDestroy) -> {
			instancesToDestroy.values().forEach(ScopedInstance::destroy);

			return null;
		});
	}

}
