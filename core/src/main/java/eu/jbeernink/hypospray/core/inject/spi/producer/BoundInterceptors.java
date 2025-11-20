package eu.jbeernink.hypospray.core.inject.spi.producer;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.Interceptor;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.interceptor.InterceptorInstance;

public record BoundInterceptors(List<Interceptor<?>> aroundConstructInterceptors,
                                List<Interceptor<?>> postConstructInterceptors,
                                Map<Method, List<Interceptor<?>>> aroundInvokeInterceptors,
                                Map<Method, List<Interceptor<?>>> aroundTimeOutInterceptors,
                                List<Interceptor<?>> preDestroyInterceptors) {

	private static final BoundInterceptors EMPTY_INSTANCE =
			new BoundInterceptors(List.of(), List.of(), Map.of(), Map.of(), List.of());

	public BoundInterceptors {
		aroundConstructInterceptors = List.copyOf(aroundConstructInterceptors);
		postConstructInterceptors = List.copyOf(postConstructInterceptors);
		aroundInvokeInterceptors = Map.copyOf(aroundInvokeInterceptors);
		aroundTimeOutInterceptors = Map.copyOf(aroundTimeOutInterceptors);
		preDestroyInterceptors = List.copyOf(preDestroyInterceptors);
	}

	public Set<Interceptor<?>> allInterceptors() {
		return concat(aroundConstructInterceptors.stream(), concat(postConstructInterceptors.stream(),
				concat(preDestroyInterceptors.stream(),
						concat(aroundInvokeInterceptors.values().stream(), aroundTimeOutInterceptors.values().stream()).flatMap(
								List::stream)))).collect(toUnmodifiableSet());
	}

	public Map<Interceptor<?>, InterceptorInstance<?>> createInterceptors(
			HyposprayCreationalContext<?> creationalContext) {
		return allInterceptors().stream()
		                        .collect(Collectors.toUnmodifiableMap(i -> i,
				                        interceptor -> createInstance(interceptor, creationalContext)));
	}

	private static <T> InterceptorInstance<T> createInstance(Interceptor<T> interceptor,
	                                                         HyposprayCreationalContext<?> creationalContext) {
		HyposprayCreationalContext<T> interceptorCreationalContext =
				creationalContext.createDependentCreationalContext(interceptor);

		T instance = interceptor.create(interceptorCreationalContext);

		return new InterceptorInstance<>(interceptor, interceptorCreationalContext, instance);
	}

	public static BoundInterceptors empty() {
		return EMPTY_INSTANCE;
	}
}
