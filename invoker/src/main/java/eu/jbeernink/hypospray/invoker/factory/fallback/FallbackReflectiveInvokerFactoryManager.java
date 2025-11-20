package eu.jbeernink.hypospray.invoker.factory.fallback;

import static java.lang.System.Logger.Level.WARNING;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.lang.System.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactory;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.WrappedInvokerFactory;

/// Fallback [InvokerFactoryManager] that generates invokers using reflection.
public class FallbackReflectiveInvokerFactoryManager implements InvokerFactoryManager {

	private static final Logger logger = System.getLogger(FallbackReflectiveInvokerFactoryManager.class.getName());

	@Override
	public InvokerFactory<?> getInvokerFactory(String className) {
		logger.log(WARNING, "Using fallback invoker factory registry, reflective invocations using invoker may fail.");
		try {
			Class<?> aClass = Class.forName(className);
			return createReflectiveInvokerFactory(className, aClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable find class " + className, e);
		}
	}

	private <T> InvokerFactory<T> createReflectiveInvokerFactory(String className, Class<T> aClass) {
		Map<String, Invoker<T, Object>> invokers = Stream.concat(Arrays.stream(aClass.getConstructors()),
				                                                 Stream.concat(Arrays.stream(aClass.getMethods()), Arrays.stream(aClass.getDeclaredMethods())))
		                                                 .distinct()
		                                                 .collect(toUnmodifiableMap(this::getMethodIdentifier,
				                                                 FallbackReflectiveInvoker::new));
		return new WrappedInvokerFactory<T>(methodIdentifier -> {
			if (invokers.containsKey(methodIdentifier)) {
				return invokers.get(methodIdentifier);
			}

			throw new IllegalArgumentException("No such method on class %s: %s".formatted(className, methodIdentifier));
		});
	}

	private String getMethodIdentifier(Executable executable) {
		return switch (executable) {
			case Constructor<?> constructor -> "new[(%s)%s]".formatted(getParameterDescriptorString(constructor),
					constructor.getDeclaringClass().descriptorString());
			case Method method ->
					"%s[(%s)%s]".formatted(method.getName(), getParameterDescriptorString(method), method.getReturnType().descriptorString());
		};
	}

	private static String getParameterDescriptorString(Executable method) {
		return Arrays.stream(method.getParameterTypes()).map(Class::descriptorString).collect(joining(","));
	}
}
