package eu.jbeernink.hypospray.core.invoke;

import jakarta.enterprise.invoke.Invoker;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Invoker that does nothing and always returns null.
///
/// This invoker can be used as a placeholder for when no method can be invoked, for example, when a bean does not
/// have any lifecycle callback methods.
@NullMarked
public final class NoopInvoker<T, R> implements Invoker<T, R> {
	@Override
	public @Nullable R invoke(T instance, Object[] arguments) throws Exception {
		return null;
	}
}
