package eu.jbeernink.hypospray.core.interceptor.chain;

import jakarta.enterprise.invoke.Invoker;

public record ConstructorInvocationChain(Invoker<Void, ?> constructorInvoker) implements InvocationChain{

	public Object newInstance(Object[] params) throws Exception{
		return constructorInvoker.invoke(null, params);
	}
}
