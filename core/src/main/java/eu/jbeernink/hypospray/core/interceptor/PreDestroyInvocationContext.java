package eu.jbeernink.hypospray.core.interceptor;

import java.util.Map;

import eu.jbeernink.hypospray.core.interceptor.chain.ConstructorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InstanceInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InterceptorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.NoOpInvocationChain;

public final class PreDestroyInvocationContext extends InvocationContextImpl {
	private final InvocationChain invocationChain;

	public PreDestroyInvocationContext(Map<String, Object> contextData, InvocationChain invocationChain) {
		super(new Object[0], contextData);
		this.invocationChain = invocationChain;
	}


	@Override
	public Object proceed() throws Exception {
		return switch (invocationChain) {
			case InterceptorInvocationChain(InterceptorInstance<?> interceptor, InvocationChain next) ->
					interceptor.preDestroy(new PostConstructInvocationContext(getContextData(), next));
			case InstanceInvocationChain instanceInvocationChain -> instanceInvocationChain.invoke(getParameters());
			case NoOpInvocationChain _ -> null;
			case ConstructorInvocationChain constructorInvocationChain ->
					throw new IllegalStateException("Unsupported post construct invocation: " + constructorInvocationChain);
		};
	}
}
