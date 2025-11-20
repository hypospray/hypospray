package eu.jbeernink.hypospray.core.invoke;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.lang.System.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import jakarta.enterprise.invoke.Invoker;

import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactory;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.WrappedInvokerFactory;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.ExecutableInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveConstructorInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveMethodInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;

/// Implementation of [InvokerFactoryManager] that can load predefined [InvokerFactory] instances and generate new
/// [InvokerFactory] instances as necessary.
@NullMarked
public class GeneratedInvokerFactoryManager implements InvokerFactoryManager {

	private static final Logger logger = System.getLogger(GeneratedInvokerFactoryManager.class.getName());

	private final Map<String, InvokerFactory<?>> invokerFactories = new ConcurrentHashMap<>();

	@Override
	public InvokerFactory<?> getInvokerFactory(String className) {
		return invokerFactories.computeIfAbsent(className,
				name -> createReflectiveInvokerFactory(ClassInformationSource.getInstance().getClassInformation(name)));
	}

	private <T> InvokerFactory<T> createReflectiveInvokerFactory(ClassInformation<T> classInformation) {
		// TODO use code generation instead of reflection to create invoker factories.
		Map<String, Invoker<T, Object>> invokers =
				Stream.concat(classInformation.constructorInformation().stream(), classInformation.allMethods().stream())
				      .distinct()
				      .collect(toUnmodifiableMap(ExecutableInformation::methodIdentifier, this::createInvoker));

		return new WrappedInvokerFactory<T>(methodIdentifier -> {
			if (invokers.containsKey(methodIdentifier)) {
				return invokers.get(methodIdentifier);
			}

			throw new IllegalArgumentException(
					"No such method on class %s: %s".formatted(classInformation.name(), methodIdentifier));
		});
	}

	@SuppressWarnings("unchecked") // Needed for generic type safety.
	private <T> Invoker<T, Object> createInvoker(ExecutableInformation executableInformation) {
		return switch (executableInformation) {
			case ReflectiveConstructorInformation<?>(Constructor<?> constructor) ->
					new ReflectiveConstructorInvoker<>((Constructor<T>) constructor);

			case ReflectiveMethodInformation(Method method) -> new ReflectiveMethodInvoker<>(method);

			case ConstructorInformation<?> constructorInformation -> {
				throw new UnsupportedOperationException("Not implemented.");
			}
			case MethodInformation methodInformation -> {
				throw new UnsupportedOperationException("Not implemented.");
			}
		};
	}

	record ReflectiveMethodInvoker<T>(Method method) implements Invoker<T, Object> {

		@Override
		public Object invoke(T instance, Object[] arguments) throws Exception {
			if (!method.canAccess(instance)) {
				Module callingModule = GeneratedInvokerFactoryManager.class.getModule();
				Module calledModule = method.getDeclaringClass().getModule();

				if (!callingModule.canRead(calledModule)) {
					calledModule.addReads(callingModule);
				}

				method.setAccessible(true);
			}

			return method.invoke(instance, arguments);
		}
	}

	record ReflectiveConstructorInvoker<T>(Constructor<T> constructor) implements Invoker<T, Object> {
		@Override
		public Object invoke(T unused, Object[] arguments) throws Exception {
			if (!constructor.canAccess(null)) {
				Module callingModule = GeneratedInvokerFactoryManager.class.getModule();
				Module calledModule = constructor.getDeclaringClass().getModule();

				if (!callingModule.canRead(calledModule)) {
					calledModule.addReads(callingModule);
				}

				constructor.setAccessible(true);
			}

			return constructor.newInstance(arguments);
		}
	}
}
