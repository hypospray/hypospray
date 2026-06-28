package eu.jbeernink.hypospray.core.inject.spi.producer;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.exception.BeanCreationException;
import eu.jbeernink.hypospray.core.inject.InjectionPointProducer;
import eu.jbeernink.hypospray.core.inject.spi.FieldInjectionPoint;
import eu.jbeernink.hypospray.core.inject.spi.InitializerMethod;

public record ClassBeanProducer<T>(BeanContainer beanContainer, BeanConstructor<T> beanConstructor,
                                   Set<FieldInjectionPoint> fieldInjectionPoints,
                                   Set<InitializerMethod> initializerMethods, Invoker<T, Void> postConstructCallback,
                                   Invoker<T, Void> preDestroyCallback) implements BeanProducer<T> {

	public ClassBeanProducer {
		fieldInjectionPoints = Set.copyOf(fieldInjectionPoints);
		initializerMethods = Set.copyOf(initializerMethods);
	}

	@Override
	public T produce(HyposprayCreationalContext<T> ctx) {
		try {
			T instance = beanConstructor.invoke(beanContainer, ctx);
			ctx.push(instance);

			return instance;
		} catch (Exception e) {
			throw new BeanCreationException("Unable to create instance of bean.", e);
		}
	}

	@Override
	public void dispose(T instance) {
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Set.copyOf(beanConstructor.injectionPoints());
	}

	@Override
	public void inject(T instance, HyposprayCreationalContext<T> ctx) {
		try {
			for (FieldInjectionPoint fieldInjectionPoint : fieldInjectionPoints) {
				Object beanValue = resolveBean(fieldInjectionPoint, ctx);

				@SuppressWarnings("unchecked") var setter = (Invoker<T, Void>) fieldInjectionPoint.setter();

				setter.invoke(instance, new Object[]{beanValue});
			}

			for (InitializerMethod initializerMethod : initializerMethods) {
				injectInitializerMethods(instance, ctx, initializerMethod);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// TODO improve error messages.
			throw new RuntimeException(e);
		}
	}

	private void injectInitializerMethods(T instance, HyposprayCreationalContext<T> ctx,
	                                      InitializerMethod initializerMethod) throws Exception {
		Object[] parameters =
				initializerMethod.injectionPoints().stream().map(injectionPoint -> resolveBean(injectionPoint, ctx)).toArray();

		@SuppressWarnings("unchecked") Invoker<? super T, ?> invoker = (Invoker<? super T, ?>) initializerMethod.invoker();

		invoker.invoke(instance, parameters);
	}

	private Object resolveBean(InjectionPoint injectionPoint, CreationalContext<?> creationalContext) {
		return InjectionPointProducer.withInjectionPoint(injectionPoint, () -> {
			Bean<?> bean = beanContainer.resolve(
					beanContainer.getBeans(injectionPoint.getType(), injectionPoint.getQualifiers().toArray(Annotation[]::new)));

			return beanContainer.getReference(bean, injectionPoint.getType(), creationalContext);
		});
	}

	@Override
	public void postConstruct(T instance) {
		try {
			postConstructCallback.invoke(instance, new Object[0]);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// TODO throw better exception
			throw new RuntimeException(e);
		}
	}

	@Override
	public void preDestroy(T instance) {
		try {
			preDestroyCallback.invoke(instance, new Object[0]);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			// TODO throw better exception.
			throw new RuntimeException(e);
		}
	}
}
