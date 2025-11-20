package eu.jbeernink.hypospray.core;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

/// [CDIProvider] to provide the currently active Hypospray [Container] instance.
public class ContainerProvider implements CDIProvider {

	private static final AtomicReference<Container> container = new AtomicReference<>(null);

	/// Set the currently active Hypospray [Container].
	///
	/// @param container the currently active container.
	/// @throws IllegalStateException if there already is an active container.
	public static void setContainer(Container container) {
		if (!ContainerProvider.container.compareAndSet(null, container)) {
			throw new IllegalStateException("Container already running!");
		}
	}

	/// Mark the currently running container as closed.
	///
	/// If the [Container] instance provided isn't equal to the currently active container, this call is a no-op.
	///
	/// @param container the container to close.
	public static void containerClosed(Container container) {
		ContainerProvider.container.compareAndSet(container, null);
	}

	@Override
	public CDI<Object> getCDI() {
		Container cdiContainer = container.get();

		if (cdiContainer == null) {
			throw new IllegalStateException("CDI container must be initialized first.");
		}

		return cdiContainer;
	}
}
