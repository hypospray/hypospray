package eu.jbeernink.hypospray.core.inject.spi.producer;

import java.util.Set;

import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;

/// Bean producer that creates a bean through a factory.
public record FactoryBeanProducer<T>(BeanContainer beanContainer, BeanFactory<T> beanFactory) implements BeanProducer<T> {

	@Override
	public T produce(HyposprayCreationalContext<T> context) {
		T instance = beanFactory().create(beanContainer, context);
		context.push(instance);
		return instance;
	}

	@Override
	public void inject(T instance, HyposprayCreationalContext<T> context) {
		// No-op on a bean factory.
	}

	@Override
	public void postConstruct(T instance) {
		// No-op on a bean factory.
	}

	@Override
	public void preDestroy(T instance) {
		// No-op on a bean factory.
	}

	@Override
	public void dispose(T instance) {
		beanFactory.dispose(instance);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return beanFactory.injectionPoints();
	}
}
