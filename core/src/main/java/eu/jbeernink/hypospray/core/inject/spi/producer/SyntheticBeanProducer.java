package eu.jbeernink.hypospray.core.inject.spi.producer;

import java.util.Set;

import jakarta.enterprise.inject.CreationException;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.exception.DisposalException;
import eu.jbeernink.hypospray.core.inject.BeanInstance;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ImmutableParameters;

public record SyntheticBeanProducer<T>(BeanContainer beanContainer, ImmutableParameters constructionParameters,
                                       Invoker<Void, ? extends SyntheticBeanCreator<T>> creatorConstructorInvoker,
                                       Invoker<Void, ? extends SyntheticBeanDisposer<T>> disposerConstructorInvoker) implements
		BeanProducer<T> {


	@Override
	public T produce(HyposprayCreationalContext<T> context) {
		try {
			SyntheticBeanCreator<T> beanCreator = creatorConstructorInvoker.invoke(null, null);

			T instance = beanCreator.create(new BeanInstance<>(beanContainer, context, Object.class, Set.of()),
					constructionParameters);

			context.push(instance);

			return instance;
		} catch (Exception e) {
			throw new CreationException("Unable to produce bean instance.", e);
		}
	}

	@Override
	public void inject(T instance, HyposprayCreationalContext<T> context) {
		// Do nothing, injection is handled during construction.
	}

	@Override
	public void postConstruct(T instance) {
		// Do nothing.
	}

	@Override
	public void preDestroy(T instance) {
		// Do nothing.
	}

	@Override
	public void dispose(T instance) {
		// We need a temporary creational context for lookup during disposal, but it doesn't matter for which bean.
		Bean<?> bean = beanContainer.getBeans(BeanContainer.class).stream().findFirst().orElseThrow();
		HyposprayCreationalContext<?> creationalContext =
				(HyposprayCreationalContext<?>) beanContainer.createCreationalContext(bean);

		try {
			SyntheticBeanDisposer<T> disposer = disposerConstructorInvoker.invoke(null, null);

			disposer.dispose(instance, new BeanInstance<>(beanContainer, creationalContext, Object.class, Set.of()),
					constructionParameters);

		} catch (Exception e) {
			throw new DisposalException("Unable to dispose bean %s".formatted(instance), e);
		} finally {
			creationalContext.release();
		}
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Set.of();
	}
}
