package eu.jbeernink.hypospray.core.context.spi;

import static java.util.Collections.synchronizedSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

public final class CreationalContextManager implements CreationalContextOwner {

	private final Set<HyposprayCreationalContext<?>> creationalContexts = synchronizedSet(new HashSet<>());

	@Override
	public void registerDependentCreationalContext(HyposprayCreationalContext<?> creationalContext) {
		creationalContexts.add(creationalContext);
	}

	@Override
	public void unregisterDependentCreationalContext(HyposprayCreationalContext<?> creationalContext) {
		creationalContexts.remove(creationalContext);
	}

	/// Destroy all [CreationalContext] instances registered with this manager.
	///
	/// This also destroys all [Contextual] instances associated with each [CreationalContext].
	public void destroyAll() {
		// Eager copy to avoid concurrent modification.
		List<HyposprayCreationalContext<?>> contextsToDestroy = List.copyOf(creationalContexts);

		contextsToDestroy.forEach(HyposprayCreationalContext::destroyInstances);
	}
}
