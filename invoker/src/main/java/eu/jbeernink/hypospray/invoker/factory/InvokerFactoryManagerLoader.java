package eu.jbeernink.hypospray.invoker.factory;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;

import java.util.ServiceLoader;

import eu.jbeernink.hypospray.invoker.factory.fallback.FallbackReflectiveInvokerFactoryManager;

/// Loader for [InvokerFactoryManager] instances using [ServiceLoader].
final class InvokerFactoryManagerLoader {

	private static final InvokerFactoryManager INSTANCE;

	static {
		ServiceLoader<InvokerFactoryManager> loader = ServiceLoader.load(InvokerFactoryManager.class);

		INSTANCE = loader.stream()
		                 .collect(findOnly(
				                 () -> new IllegalStateException("Multiple InvokerFactoryRegistry instances found on path.")))
		                 .map(ServiceLoader.Provider::get)
		                 .orElseGet(FallbackReflectiveInvokerFactoryManager::new);
	}

	/// Returns the singleton instance of the current [InvokerFactoryManager].
	static InvokerFactoryManager getInvokerFactoryManager() {
		return INSTANCE;
	}

	private InvokerFactoryManagerLoader() {}
}
