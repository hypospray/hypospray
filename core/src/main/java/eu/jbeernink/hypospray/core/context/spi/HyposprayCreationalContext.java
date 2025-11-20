package eu.jbeernink.hypospray.core.context.spi;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

public final class HyposprayCreationalContext<T> implements CreationalContext<T>, CreationalContextOwner {

	private static final Logger logger = System.getLogger(HyposprayCreationalContext.class.getName());

	private final Lock lock = new ReentrantLock();

	private final CreationalContextOwner owner;
	private final Contextual<T> contextual;
	private final Map<T, T> instances = new IdentityHashMap<>();
	private final List<HyposprayCreationalContext<?>> dependentCreationalContexts = new ArrayList<>();

	public HyposprayCreationalContext(CreationalContextOwner owner, Contextual<T> contextual) {
		this.owner = owner;
		this.contextual = contextual;
	}

	public void remove(T instance) {
		lock.lock();
		try {
			instances.remove(instance);

			if (instances.isEmpty()) {
				release();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void push(T incompleteInstance) {
		lock.lock();
		try {
			if (!instances.isEmpty()) {
				logger.log(Logger.Level.WARNING,
						"Creational context has been used to create more than one bean, this may lead to memory leaks.");
			}
			owner.registerDependentCreationalContext(this);
			instances.put(incompleteInstance, incompleteInstance);
		} finally {
			lock.unlock();
		}
	}

	public <X> HyposprayCreationalContext<X> createDependentCreationalContext(Contextual<X> contextual) {
		return newCreationalContext(contextual);
	}

	public void destroyInstances() {
		lock.lock();
		try {
			instances.values().forEach(instance -> contextual.destroy(instance, this));
			instances.clear();
			release();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void release() {
		lock.lock();
		try {
			// Eager copy to avoid concurrent modification exceptions.
			List<HyposprayCreationalContext<?>> contextsToDestroy = List.copyOf(dependentCreationalContexts);

			contextsToDestroy.forEach(HyposprayCreationalContext::destroyInstances);

			owner.unregisterDependentCreationalContext(this);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void registerDependentCreationalContext(HyposprayCreationalContext<?> creationalContext) {
		owner.registerDependentCreationalContext(this);
		dependentCreationalContexts.add(creationalContext);
	}

	@Override
	public void unregisterDependentCreationalContext(HyposprayCreationalContext<?> creationalContext) {
		dependentCreationalContexts.remove(creationalContext);
		if (instances.isEmpty() && dependentCreationalContexts.isEmpty()) {
			owner.unregisterDependentCreationalContext(this);
		}
	}
}
