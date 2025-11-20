package eu.jbeernink.hypospray.core.interceptor.chain;

import java.lang.reflect.InvocationTargetException;

import jakarta.enterprise.invoke.Invoker;

public record InstanceInvocationChain<T>(T instance, Invoker<T, ?> invoker) implements InvocationChain {
	public Object invoke(Object[] parameters) throws InvocationTargetException, IllegalAccessException {
		try {
			return invoker.invoke(instance, parameters);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}
}
