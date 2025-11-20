package eu.jbeernink.hypospray.invoker.factory;

import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;

/// Factory for creating invoker instances for a specific class.
///
/// @param <T> The type to produce invoker factories for.
@FunctionalInterface
public interface InvokerFactory<T> extends Function<String, Invoker<T, Object>> {

	Invoker<T, Object> create(String methodIdentifier);

	@Override
	default Invoker<T, Object> apply(String methodIdentifier) {
		return create(methodIdentifier);
	}

	static InvokerFactoryManager getInvokerFactoryRegistry() {
		return InvokerFactoryManagerLoader.getInvokerFactoryManager();
	}

	default InvokerFactory<Void> withInstance(T instance) {
		return methodIdentifier -> {
			Invoker<T, Object> invoker = apply(methodIdentifier);

			return (_, arguments) -> invoker.invoke(instance, arguments);
		};
	}
}
