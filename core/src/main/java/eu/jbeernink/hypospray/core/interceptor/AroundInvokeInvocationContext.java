package eu.jbeernink.hypospray.core.interceptor;

import java.lang.reflect.Method;
import java.util.Map;

import eu.jbeernink.hypospray.core.interceptor.chain.ConstructorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InstanceInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InterceptorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.NoOpInvocationChain;

public final class AroundInvokeInvocationContext extends InvocationContextImpl {

	private final Object target;
	private final Method method;

	private final InvocationChain invocationChain;

	public AroundInvokeInvocationContext(Object target, Method method, Object[] parameters,
	                                     Map<String, Object> contextData, InvocationChain invocationChain) {
		super(parameters, contextData);
		this.target = target;
		this.method = method;
		this.invocationChain = invocationChain;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public Object proceed() throws Exception {
		return switch (invocationChain) {
			case InterceptorInvocationChain(InterceptorInstance<?> interceptor, InvocationChain next) ->
					interceptor.aroundInvoke(
							new AroundInvokeInvocationContext(target, method, getParameters(), getContextData(), next));
			case InstanceInvocationChain instance -> instance.invoke(getParameters());
			case NoOpInvocationChain noOp ->
					throw new IllegalStateException("Around invoke cannot be called on a no-op: " + noOp);
			case ConstructorInvocationChain constructor ->
					throw new IllegalStateException("Around invoke cannot be called on a constructor: " + constructor);
		};
	}
}
