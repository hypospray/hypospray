package eu.jbeernink.hypospray.core.test.invoker;

import jakarta.enterprise.invoke.Invoker;

///  Fake [Invoker] used for checking expected [Invoker] during unit tests.
public record FakeInvoker<T,R>(String className, String methodIdentifier) implements Invoker<T, R> {
	@Override
	public R invoke(T instance, Object[] arguments) throws Exception {
		throw new UnsupportedOperationException("Fake invoker.");
	}
}
