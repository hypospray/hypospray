package eu.jbeernink.hypospray.core.test.invoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import jakarta.enterprise.invoke.Invoker;

import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactory;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.model.information.ExecutableInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveConstructorInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveMethodInformation;

/// Fake [InvokerFactoryManager] that returns [FakeInvoker] instances for equality checking in unit tests.
///
/// This class offers various convenience methods on top of [InvokerFactoryManager] in order simplify obtaining
/// [FakeInvoker] instances in unit tests.
@NullMarked
public class FakeInvokerFactoryManager implements InvokerFactoryManager {

	@Override
	public InvokerFactory<?> getInvokerFactory(String className) {
		return (methodIdentifier) -> new FakeInvoker<>(className, methodIdentifier);
	}

	/// Get an [FakeInvoker] for the given [Constructor].
	@SuppressWarnings("unchecked")
	public <T> FakeInvoker<Void, T> getInvoker(Constructor<T> constructor) {
		return (FakeInvoker<Void, T>) getInvoker(new ReflectiveConstructorInformation<>(constructor));
	}

	/// Get an [Invoker] for the given [Method].
	public FakeInvoker<?, ?> getInvoker(Method method) {
		return getInvoker(new ReflectiveMethodInformation(method));
	}

	/// Get a [FakeInvoker] for the given constructor or method.
	public FakeInvoker<?, ?> getInvoker(ExecutableInformation executable) {
		return new FakeInvoker<>(executable.declaringClass().name(), executable.methodIdentifier());
	}
}
