package eu.jbeernink.hypospray.core.interceptor;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.function.Supplier;

import eu.jbeernink.hypospray.core.interceptor.chain.ConstructorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InstanceInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InterceptorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.NoOpInvocationChain;

public final class AroundConstructInvocationContext extends InvocationContextImpl {

	private final Supplier<Constructor<?>> constructorSupplier;
	private final InvocationChain invocationChain;

	public AroundConstructInvocationContext(Supplier<Constructor<?>> constructorSupplier, Object[] parameters,
	                                        Map<String, Object> contextData, InvocationChain invocationChain) {
		super(parameters, contextData);
		this.constructorSupplier = constructorSupplier;
		this.invocationChain = invocationChain;
	}

	@Override
	public Constructor<?> getConstructor() {
		return constructorSupplier.get();
	}

	@Override
	public Object proceed() throws Exception {
		return switch (invocationChain) {
			case InterceptorInvocationChain interceptor -> interceptor.aroundConstruct(
					new AroundConstructInvocationContext(constructorSupplier, getParameters(), getContextData(), interceptor.next()));
			case ConstructorInvocationChain constructorInvocationChain ->
					constructorInvocationChain.newInstance(getParameters());
			case NoOpInvocationChain chain ->
					throw new UnsupportedOperationException("Unsupported invocation chain entry: " + chain);
			case InstanceInvocationChain chain ->
					throw new UnsupportedOperationException("Unsupported invocation chain entry: " + chain);
		};
	}
}
