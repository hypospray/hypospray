package eu.jbeernink.hypospray.core.inject.spi.producer;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionTarget;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;

/// Producer to create new bean instances.
///
/// @param <T> the type of the produced bean.
public sealed interface BeanProducer<T> extends InjectionTarget<T> permits ClassBeanProducer, FactoryBeanProducer,
		InterceptedClassBeanProducer, SyntheticBeanProducer {

	@Override
	default T produce(CreationalContext<T> ctx) {
		return switch (ctx) {
			case HyposprayCreationalContext<T> context -> produce(context);
			case CreationalContext<T> _ -> throw new IllegalArgumentException("Unsupported creational context: " + ctx);
		};
	}

	/// Produce a new instance of the bean.
	///
	/// Once this method completes, the bean instance has been created, but depending on the type of bean, not all dependencies may have been
	/// injected yet. To guarantee all dependencies have been injected correctly, [#inject(Object, HyposprayCreationalContext)] should be called
	/// with the newly created bean instance.
	///
	/// @param context the creational context to use to produce the bean.
	/// @return the new bean instance.
	T produce(HyposprayCreationalContext<T> context);

	@Override
	default void inject(T instance, CreationalContext<T> ctx) {
		switch (ctx) {
			case HyposprayCreationalContext<T> context -> inject(instance, context);
			case CreationalContext<T> _ -> throw new IllegalArgumentException("Unsupported creational context: " + ctx);
		}
	}

	/// Injects dependencies in the given bean instance.
	///
	/// @param instance the bean instance to inject into.
	/// @param context  the creational context to use when injecting dependencies.
	void inject(T instance, HyposprayCreationalContext<T> context);
}
