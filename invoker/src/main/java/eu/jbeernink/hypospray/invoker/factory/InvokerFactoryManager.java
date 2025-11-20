package eu.jbeernink.hypospray.invoker.factory;

import jakarta.enterprise.invoke.Invoker;

/// Manager for tracking invoker factories.
///
/// As [InvokerFactoryManager] instances are singletons, implementations must be thread-safe.
public interface InvokerFactoryManager {

	/// Gets an instance of an [InvokerFactoryManager].
	///
	/// The returned [InvokerFactoryManager] will be a singleton.
	static InvokerFactoryManager getInstance() {
		return InvokerFactoryManagerLoader.getInvokerFactoryManager();
	}

	/// Gets an [InvokerFactory] with the class with the given name.
	///
	/// Implementations may generate new [InvokerFactory] instances if no existing factory is present.
	///
	/// @param className the name of the class to generate an invoker factory for.
	/// @return an invoker factory for the given class.
	/// @throws IllegalArgumentException if no class with the given name could be found.
	InvokerFactory<?> getInvokerFactory(String className);

	/// Gets an [Invoker] for the given class and method identifier.
	///
	/// Implementations may generate new invokers as needed.
	///
	/// @param className        the class name of the class to return the invoker factory for.
	/// @param methodIdentifier the method identifier for the method to return the invoker for.
	/// @return an invoker for the given class and method.
	default Invoker<?, ?> getInvoker(String className, String methodIdentifier) {
		return getInvokerFactory(className).apply(methodIdentifier);
	}
}
