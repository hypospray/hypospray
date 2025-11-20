package eu.jbeernink.hypospray.core.inject.spi.producer;

import java.util.Set;

import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;

/// Factory to create instances of a bean.
///
/// @param <T> the type of the bean.
public sealed interface BeanFactory<T> permits StaticInvokerBeanFactory, InstanceInvokerBeanFactory {

	/// Creates a new instance of the bean this is a factory for.
	///
	/// @param container         the bean container to use to create the instance.
	/// @param creationalContext the creational context to use to create the instance.
	/// @return a new instance of a bean.
	T create(BeanContainer container, HyposprayCreationalContext<T> creationalContext);

	/// Disposes of a bean instance.
	///
	/// @param instance the bean instance to dispose.
	void dispose(T instance);

	/// The set of injection points of this bean factory.
	///
	/// @return an immutable set containing the injection points of this bean factory.
	Set<InjectionPoint> injectionPoints();
}
