package eu.jbeernink.hypospray.core.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import jakarta.interceptor.InvocationContext;

public sealed abstract class InvocationContextImpl implements InvocationContext permits
		AroundConstructInvocationContext, AroundInvokeInvocationContext, PostConstructInvocationContext,
		PreDestroyInvocationContext {

	private final Map<String, Object> contextData;
	private Object[] parameters;

	protected InvocationContextImpl(Object[] parameters, Map<String, Object> contextData) {
		this.parameters = parameters;
		this.contextData = contextData;
	}

	@Override
	public Object getTarget() {
		return null;
	}

	@Override
	public Object getTimer() {
		return null;
	}

	@Override
	public Method getMethod() {
		return null;
	}

	@Override
	public Constructor<?> getConstructor() {
		return null;
	}

	@Override
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(Object[] params) {
		parameters = params;
	}

	@Override
	public Map<String, Object> getContextData() {
		return contextData;
	}
}
