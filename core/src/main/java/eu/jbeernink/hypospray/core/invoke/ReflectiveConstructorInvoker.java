package eu.jbeernink.hypospray.core.invoke;

import java.lang.reflect.Constructor;

import jakarta.enterprise.invoke.Invoker;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Invoker which invokes a constructor using reflection.
///
///  The constructor passed to this invoker should be accessible by this invoker.
///
/// @param constructor the constructor to invoker.
/// @param <T> the type of the object which defines the constructor.
@NullMarked
@Deprecated
public record ReflectiveConstructorInvoker<T>(Constructor<T> constructor) implements Invoker<Void, T> {

	public ReflectiveConstructorInvoker {
		if (!constructor.canAccess(null)) {
			constructor.setAccessible(true);
		}
	}

	@Override
	public T invoke(@Nullable Void instance, @Nullable Object[] arguments) throws Exception {
		return constructor.newInstance(arguments);
	}
}
