package eu.jbeernink.hypospray.core;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.inject.BeanInstance;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

/// A Hypospray container.
public class Container extends CDI<Object> implements SeContainer {
	private final AtomicBoolean running = new AtomicBoolean(true);

	private final CreationalContextManager creationalContextManager;
	private final ContainerRegistry containerRegistry;

	private final BeanInstance<Object> instance;

	/// Create a new container with the given [CreationalContextManager] and [ContainerRegistry].
	///
	/// @param creationalContextManager the creational context manager for this container.
	/// @param containerRegistry the container registry for this container.
	public Container(CreationalContextManager creationalContextManager, ContainerRegistry containerRegistry) {
		this.creationalContextManager = creationalContextManager;
		this.containerRegistry = containerRegistry;
		instance =
				new BeanInstance<>(new InjectableBeanContainer(creationalContextManager, containerRegistry), creationalContextManager, Object.class,
						Set.of());
	}

	@Override
	public void close() {
		if (running.compareAndSet(true, false)) {
			getBeanContainer().getEvent().select(Shutdown.class).fire(new Shutdown());

			creationalContextManager.destroyAll();

			ContainerProvider.containerClosed(this);
		} else {
			throw new IllegalStateException("Container already closed.");
		}
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public BeanContainer getBeanContainer() {
		return new InjectableBeanContainer(creationalContextManager, containerRegistry);
	}

	@Override
	public BeanManager getBeanManager() {
		return select(BeanManager.class).get();
	}

	@Override
	public Instance<Object> select(Annotation... qualifiers) {
		return instance.select(qualifiers);
	}

	@Override
	public <U> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
		return instance.select(subtype, qualifiers);
	}

	@Override
	public <U> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		return instance.select(subtype, qualifiers);
	}

	@Override
	public boolean isUnsatisfied() {
		return instance.isUnsatisfied();
	}

	@Override
	public boolean isAmbiguous() {
		return instance.isAmbiguous();
	}

	@Override
	public void destroy(Object instance) {
		this.instance.destroy(instance);
	}

	@Override
	public Handle<Object> getHandle() {
		return instance.getHandle();
	}

	@Override
	public Iterable<? extends Handle<Object>> handles() {
		return instance.handles();
	}

	@Override
	public Object get() {
		return instance.get();
	}

	@Override
	public Iterator<Object> iterator() {
		return instance.iterator();
	}
}
