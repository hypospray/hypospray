package eu.jbeernink.hypospray.core.inject.spi.producer;

import static jakarta.enterprise.inject.spi.InterceptionType.AROUND_CONSTRUCT;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.interceptor.AroundConstructInvocationContext;
import eu.jbeernink.hypospray.core.interceptor.InterceptorContext;
import eu.jbeernink.hypospray.core.interceptor.InterceptorInstance;
import eu.jbeernink.hypospray.core.interceptor.chain.ConstructorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InterceptorInvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.InvocationChain;
import eu.jbeernink.hypospray.core.interceptor.chain.NoOpInvocationChain;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

public record InterceptedClassBeanProducer<T>(ContainerRegistry containerRegistry, ClassBeanProducer<T> producer,
                                              BoundInterceptors boundInterceptors) implements BeanProducer<T> {

	@Override
	public T produce(HyposprayCreationalContext<T> context) {
		Map<Interceptor<?>, InterceptorInstance<?>> interceptors = boundInterceptors.createInterceptors(context);

		try {
			InvocationChain aroundConstructInvocationChain = createAroundConstructInvocationChain(interceptors);

			Object[] constructorParameters = producer.beanConstructor().resolveParameters(producer.beanContainer(), context);

			InvocationContext invocationContext =
					new AroundConstructInvocationContext(producer.beanConstructor()::constructor, constructorParameters,
							new HashMap<>(), aroundConstructInvocationChain);


			@SuppressWarnings("unchecked") T instance = (T) invocationContext.proceed();

			InterceptorContext interceptorContext = createInterceptorContext(instance, interceptors);

			containerRegistry.registerInterceptorContext(instance, interceptorContext);

			return instance;
		} catch (RuntimeException e) {
			context.release();
			throw e;
		} catch (Exception e) {
			context.release();
			// TODO throw better exception.
			throw new RuntimeException(e);
		} finally {
			interceptors.values()
			            .stream()
			            .filter(InterceptedClassBeanProducer::isAroundConstructOnly)
			            .forEach(InterceptorInstance::destroyInstance);
		}
	}

	private InterceptorContext createInterceptorContext(T instance,
	                                                    Map<Interceptor<?>, InterceptorInstance<?>> interceptors) {
		// TODO create appropriate interceptor chains.


		return new InterceptorContext(instance, new NoOpInvocationChain(), new NoOpInvocationChain(), Map.of());
	}

	private static boolean isAroundConstructOnly(InterceptorInstance<?> interceptor) {
		Set<InterceptionType> interceptionTypes = interceptor.interceptionTypes();

		if (interceptionTypes.size() == 1 && interceptionTypes.contains(AROUND_CONSTRUCT)) {
			return true;
		}

		return false;
	}

	@Override
	public void inject(T instance, HyposprayCreationalContext<T> context) {
		// TODO should the initializer methods be intercepted?
		producer.inject(instance, context);
	}

	@Override
	public void postConstruct(T instance) {
		InterceptorContext interceptorContext = containerRegistry.getInterceptorContext(instance).orElseThrow();

		// TODO invoke postConstruct invocation chain
	}

	@Override
	public void preDestroy(T instance) {
		InterceptorContext interceptorContext = containerRegistry.getInterceptorContext(instance).orElseThrow();

		// TODO invoke preDestroy invocation chain.
	}

	@Override
	public void dispose(T instance) {
		containerRegistry.clearInterceptorContext(instance);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return producer.getInjectionPoints();
	}

	private InvocationChain createAroundConstructInvocationChain(
			Map<Interceptor<?>, InterceptorInstance<?>> interceptors) {
		var aroundConstructInterceptors =
				boundInterceptors.aroundConstructInterceptors().stream().map(interceptors::get).toList();

		InvocationChain invocationChain = new ConstructorInvocationChain(producer.beanConstructor().constructorInvoker());

		for (InterceptorInstance<?> interceptorInstance : aroundConstructInterceptors.reversed()) {
			invocationChain = new InterceptorInvocationChain(interceptorInstance, invocationChain);
		}

		return invocationChain;
	}
}
