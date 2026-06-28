package eu.jbeernink.hypospray.core.invoke;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.lang.System.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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

	private record GeneratedInvoker<T>(String methodIdentifier, Invoker<T, ? extends Object> invoker) {}

	private final Map<String, InvokerFactory<?>> invokerFactories = new ConcurrentHashMap<>();

	@Override
	public InvokerFactory<?> getInvokerFactory(String className) {
		return invokerFactories.computeIfAbsent(className,
				name -> createReflectiveInvokerFactory(ClassInformationSource.getInstance().getClassInformation(name)));
	}


	private <T> InvokerFactory<T> createReflectiveInvokerFactory(ClassInformation<T> classInformation) {
		// TODO use code generation instead of reflection to create invoker factories.
		Map<String, Invoker<T, ? extends Object>> invokers =
				Stream.concat(createExecutableInvokers(classInformation), createSyntheticFieldInvokers(classInformation))
				      .distinct()
				      .collect(toUnmodifiableMap(GeneratedInvoker::methodIdentifier, GeneratedInvoker::invoker));

		return new WrappedInvokerFactory<>(methodIdentifier -> {
			if (invokers.containsKey(methodIdentifier)) {
				@SuppressWarnings("unchecked") Invoker<T, Object> invoker = (Invoker<T, Object>) invokers.get(methodIdentifier);
				return invoker;
			}

			throw new IllegalArgumentException(
					"No such method on class %s: %s".formatted(classInformation.name(), methodIdentifier));
		});
	}

	private <T> Stream<GeneratedInvoker<T>> createExecutableInvokers(ClassInformation<T> classInformation) {
		return Stream.concat(classInformation.constructorInformation().stream(), classInformation.allMethods().stream())
		             .map(executableInformation -> new GeneratedInvoker<>(executableInformation.methodIdentifier(),
				             createInvoker(executableInformation)));
	}

	private <T> Stream<GeneratedInvoker<T>> createSyntheticFieldInvokers(ClassInformation<T> classInformation) {
		return classInformation.fieldInformation()
		                       .stream()
		                       .map(field -> new GeneratedInvoker<>(field.syntheticSetterMethodIdentifier(),
				                       new ReflectiveFieldSetterInvoker<>(field.fieldInstance())));
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

	private record ReflectiveMethodInvoker<T>(Method method) implements Invoker<T, Object> {

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

	private record ReflectiveConstructorInvoker<T>(Constructor<T> constructor) implements Invoker<T, Object> {
		@Override
		public Object invoke(T unused, Object[] arguments) throws Exception {
			if (!constructor.canAccess(null)) {
				Module callingModule = GeneratedInvokerFactoryManager.class.getModule();
				Module calledModule = constructor.getDeclaringClass().getModule();

				if (!callingModule.canRead(calledModule)) {
					callingModule.addReads(calledModule);
				}

				constructor.setAccessible(true);
			}

			return constructor.newInstance(arguments);
		}
	}

	private record ReflectiveFieldSetterInvoker<T>(Field field) implements Invoker<T, Void> {
		@Override
		public Void invoke(T instance, Object[] arguments) throws Exception {
			if (arguments.length != 1) {
				throw new IllegalArgumentException(
						"Field setter invokers must have exactly one argument: %s".formatted(Arrays.toString(arguments)));
			}

			if (!field.canAccess(instance)) {
				Module callingModule = GeneratedInvokerFactoryManager.class.getModule();
				Module calledModule = field.getDeclaringClass().getModule();

				if (!callingModule.canRead(calledModule)) {
					callingModule.addReads(calledModule);
				}

				field.setAccessible(true);
			}

			field.set(instance, arguments[0]);
			return null;
		}
	}
}
