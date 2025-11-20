package eu.jbeernink.hypospray.core.inject.spi.producer;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.exception.BeanCreationException;
import eu.jbeernink.hypospray.core.inject.spi.MethodInjectionPoint;

/// Bean factory that uses a static method or field to create bean instances.
///
/// @param <T>                    the type of the bean.
/// @param factoryInvoker         an invoker which calls the static method or field which creates the bean.
/// @param factoryInjectionPoints any injection points that may be present on a method that produces the bean.
/// @param disposerInvoker        an invoker which is called on disposal of the bean.
public record StaticInvokerBeanFactory<T>(Invoker<Void, T> factoryInvoker,
                                          List<MethodInjectionPoint> factoryInjectionPoints,
                                          Invoker<Void, Void> disposerInvoker) implements BeanFactory<T> {
	@Override
	public T create(BeanContainer container, HyposprayCreationalContext<T> creationalContext) {
		try {
			return factoryInvoker.invoke(null, new Object[]{});
		} catch (Exception e) {
			throw new BeanCreationException("Unable to create instance of bean.", e);
		}
	}

	@Override
	public void dispose(T instance) {
		unimplemented();
	}

	@Override
	public Set<InjectionPoint> injectionPoints() {
		return Set.copyOf(factoryInjectionPoints);
	}
}
