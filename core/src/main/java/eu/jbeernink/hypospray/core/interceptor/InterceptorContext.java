package eu.jbeernink.hypospray.core.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.invoke.Invoker;
import jakarta.interceptor.InvocationContext;

import eu.jbeernink.hypospray.core.interceptor.chain.InstanceInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InvocationChain;
import eu.jbeernink.hypospray.core.invoke.ReflectiveMethodInvoker;

public record InterceptorContext(Object instance, InvocationChain postConstructInvocationChain,
                                 InvocationChain preDestroyInvocationChain,
                                 Map<Method, InvocationChain> aroundInvokeInvocationChains) implements
		Function<String, Invoker<Void, Object>> {

	public InterceptorContext {
		aroundInvokeInvocationChains = Map.copyOf(aroundInvokeInvocationChains);
	}

	public InvocationContext createAroundInvokeInvocationContext(Method method, Object[] parameters) {
		if (!aroundInvokeInvocationChains.containsKey(method)) {
			return new AroundInvokeInvocationContext(instance, method, parameters, new HashMap<>(),
					new InstanceInvocationChain<>(instance, new ReflectiveMethodInvoker<>(method)));
		}

		InvocationChain invocationChain = aroundInvokeInvocationChains.get(method);

		return new AroundInvokeInvocationContext(instance, method, parameters, new HashMap<>(), invocationChain);
	}

	@Override
	public Invoker<Void, Object> apply(String methodIdentifier) {
		// TODO: #119 - Migrate this to fully support invokers.
		return aroundInvokeInvocationChains.entrySet()
		                                   .stream()
		                                   .filter(e -> toMethodIdentifier(e.getKey()).equals(
				                                                            methodIdentifier))
		                                   .findFirst()
		                                   .map(
				                                                            e -> (Invoker<Void, Object>) (_, parameters) -> createAroundInvokeInvocationContext(
						                                                            e.getKey(), parameters))
		                                   .orElseThrow();
	}

	private String toMethodIdentifier(Method method) {
		return "%s[(%s)%s]".formatted(method.getName(),
				Arrays.stream(method.getParameterTypes()).map(Class::descriptorString).collect(Collectors.joining(",")),
				method.getReturnType().descriptorString());
	}
}
