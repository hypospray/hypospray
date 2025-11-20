package eu.jbeernink.hypospray.core.interceptor.chain;

import jakarta.interceptor.InvocationContext;

import eu.jbeernink.hypospray.core.interceptor.InterceptorInstance;

public record InterceptorInvocationChain(InterceptorInstance<?> interceptorInstance, InvocationChain next) implements
		InvocationChain {

	public Object aroundConstruct(InvocationContext invocationContext) throws Exception {
		return interceptorInstance.aroundConstruct(invocationContext);
	}

	public Object postConstruct(InvocationContext invocationContext) throws Exception {
		return interceptorInstance.postConstruct(invocationContext);
	}

	public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
		return interceptorInstance.aroundInvoke(invocationContext);
	}

	public Object preDestroy(InvocationContext invocationContext) throws Exception {
		return interceptorInstance.preDestroy(invocationContext);
	}
}
