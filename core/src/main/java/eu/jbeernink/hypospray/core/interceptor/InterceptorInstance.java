package eu.jbeernink.hypospray.core.interceptor;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Arrays;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;

public record InterceptorInstance<T>(Interceptor<T> interceptor, CreationalContext<T> creationalContext, T instance) {

	public Set<InterceptionType> interceptionTypes() {
		return Arrays.stream(InterceptionType.values()).filter(interceptor::intercepts).collect(toUnmodifiableSet());
	}

	public Object aroundConstruct(InvocationContext invocationContext) throws Exception {
		return interceptor.intercept(InterceptionType.AROUND_CONSTRUCT, instance, invocationContext);
	}

	public Object postConstruct(InvocationContext invocationContext) throws Exception {
		return interceptor.intercept(InterceptionType.POST_CONSTRUCT, instance, invocationContext);
	}

	public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
		return interceptor.intercept(InterceptionType.AROUND_INVOKE, instance, invocationContext);
	}

	public Object preDestroy(InvocationContext invocationContext) throws Exception {
		return interceptor.intercept(InterceptionType.PRE_DESTROY, instance, invocationContext);
	}

	public void destroyInstance() {
		interceptor.destroy(instance, creationalContext);
	}
}
