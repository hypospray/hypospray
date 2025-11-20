package eu.jbeernink.hypospray.scope.common;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

public record ScopedInstance<T>(T instance, Contextual<T> contextual, CreationalContext<T> creationalContext) {

	public void destroy() {
		contextual.destroy(instance, creationalContext);
	}

	public static <T> ScopedInstance<T> create(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		return new ScopedInstance<>(contextual.create(creationalContext), contextual, creationalContext);
	}
}
