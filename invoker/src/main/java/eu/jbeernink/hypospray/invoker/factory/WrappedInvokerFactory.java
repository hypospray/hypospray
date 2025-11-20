package eu.jbeernink.hypospray.invoker.factory;

import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;

public record WrappedInvokerFactory<T>(Function<String, Invoker<T, Object>> invokerLookupFunction) implements InvokerFactory<T> {

	@Override
	public Invoker<T, Object> create(String methodIdentifier) {
		return invokerLookupFunction.apply(methodIdentifier);
	}
}
