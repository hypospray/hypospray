package eu.jbeernink.hypospray.core.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.enterprise.invoke.Invoker;

/// Invoker which invokes a given method using reflection.
@Deprecated
public record ReflectiveMethodInvoker<T, R>(Method method) implements Invoker<T, R> {
	@Override
	public R invoke(T instance, Object[] arguments) throws Exception {
		try {
			if (!method.canAccess(instance)) {
				method.setAccessible(true);
			}

			@SuppressWarnings("unchecked") R result = (R) method.invoke(instance, arguments);

			return result;
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				throw (Exception) e.getCause();
			}

			throw e;
		}
	}
}
