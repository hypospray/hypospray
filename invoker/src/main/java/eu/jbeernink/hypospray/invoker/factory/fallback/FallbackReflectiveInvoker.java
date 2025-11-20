package eu.jbeernink.hypospray.invoker.factory.fallback;

import static java.lang.System.Logger.Level.WARNING;

import java.lang.System.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import jakarta.enterprise.invoke.Invoker;

/// Reflective invoker to be used as a fallback when no better invoker is available.
record FallbackReflectiveInvoker<T, R>(Executable executable) implements Invoker<T, R> {

	private static final Logger logger = System.getLogger(FallbackReflectiveInvoker.class.getName());

	@Override
	@SuppressWarnings("unchecked")
	public R invoke(T instance, Object[] arguments) throws Exception {
		logger.log(WARNING,
				"Using the reflection-based fallback invoker, method invocations may fail due to access restrictions.");
		if (!executable.canAccess(instance)) {
			executable.setAccessible(true);
		}

		return switch (executable) {
			case Method method -> (R) method.invoke(instance, arguments);
			case Constructor<?> constructor -> (R) constructor.newInstance(arguments);
		};
	}
}
