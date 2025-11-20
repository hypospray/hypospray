package eu.jbeernink.hypospray.core.inject.spi.producer;

import static java.util.Comparator.comparingInt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.inject.InjectionPointProducer;
import eu.jbeernink.hypospray.core.inject.spi.ConstructorInjectionPoint;
import eu.jbeernink.hypospray.core.invoke.ReflectiveConstructorInvoker;

public record BeanConstructor<T>(Invoker<Void, T> constructorInvoker, List<ConstructorInjectionPoint> injectionPoints) {

	@Deprecated(forRemoval = true) // TODO #121 - Remove once interceptors are migrated to invokers.
	public Constructor<T> constructor() {
		return switch (constructorInvoker) {
			case ReflectiveConstructorInvoker<T>(Constructor<T> constructor) -> constructor;
			case Supplier<?> supplier when supplier.get() instanceof Constructor<?> constructor -> (Constructor<T>)constructor;
			case Invoker<Void, T> _ -> throw new IllegalStateException("Unable to obtain instance of constructor from invoker.");
		};
	}

	public T invoke(BeanContainer beanContainer, HyposprayCreationalContext<T> creationalContext) {
		try {
			Object[] parameters = resolveParameters(beanContainer, creationalContext);

			return constructorInvoker.invoke(null, parameters);
		} catch (Throwable t) {
			// TODO improve exception.
			throw new RuntimeException(t);
		}
	}

	public Object[] resolveParameters(BeanContainer beanContainer, HyposprayCreationalContext<T> creationalContext) {
		return injectionPoints.stream()
		                      .sorted(comparingInt(ConstructorInjectionPoint::index))
		                      .map(injectionPoint -> resolveParameter(beanContainer, injectionPoint, creationalContext))
		                      .toArray();
	}

	private Object resolveParameter(BeanContainer beanContainer, InjectionPoint injectionPoint,
	                                HyposprayCreationalContext<?> creationalContext) {
		return InjectionPointProducer.withInjectionPoint(injectionPoint, () -> {
			Set<Bean<?>> beans =
					beanContainer.getBeans(injectionPoint.getType(), injectionPoint.getQualifiers().toArray(new Annotation[0]));


			Bean<?> bean = beanContainer.resolve(beans);

			return resolveParameter(beanContainer, bean, injectionPoint.getType(), creationalContext);
		});
	}

	@SuppressWarnings("unchecked")
	private static <T> T resolveParameter(BeanContainer beanContainer, Bean<T> bean, Type type,
	                                      HyposprayCreationalContext<?> creationalContext) {
		HyposprayCreationalContext<T> dependentCreationalContext = creationalContext.createDependentCreationalContext(bean);

		return (T) beanContainer.getReference(bean, type, dependentCreationalContext);
	}
}
