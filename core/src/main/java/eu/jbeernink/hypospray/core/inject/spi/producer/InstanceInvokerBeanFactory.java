package eu.jbeernink.hypospray.core.inject.spi.producer;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;

/// Bean factory which creates beans using an factory bean.
///
/// @param factoryBean the factory bean that creates the required bean.
/// @param factoryInvoker an invoker to call the factory method on an instance of the factory bean.
/// @param disposerInvoker an invoker to be called on the factory bean when the constructed bean is disposed.
/// @param injectionPoints the set of injection points to be passed to the
public record InstanceInvokerBeanFactory<F, T>(Bean<F> factoryBean, Invoker<F, T> factoryInvoker, Invoker<F, T> disposerInvoker,
                                               Set<InjectionPoint> injectionPoints) implements BeanFactory<T> {

	public InstanceInvokerBeanFactory {
		injectionPoints = Set.copyOf(injectionPoints);
	}

	@Override
	public T create(BeanContainer container, HyposprayCreationalContext<T> creationalContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void dispose(T instance) {
		throw new UnsupportedOperationException();
	}
}
