package eu.jbeernink.hypospray.core.interceptor.chain;

public sealed interface InvocationChain permits ConstructorInvocationChain, InstanceInvocationChain,
		InterceptorInvocationChain, NoOpInvocationChain {

}
