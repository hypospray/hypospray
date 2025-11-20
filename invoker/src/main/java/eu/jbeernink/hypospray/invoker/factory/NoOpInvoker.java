package eu.jbeernink.hypospray.invoker.factory;

import jakarta.enterprise.invoke.Invoker;

import org.jspecify.annotations.Nullable;

/// Invoker that performs no logic or method calls.
///
/// This invoker will always return null.
public record NoOpInvoker<T, R>() implements Invoker<T, R> {

	@Override
	public @Nullable R invoke(T instance, Object[] arguments) throws Exception {
		return null;
	}
}
