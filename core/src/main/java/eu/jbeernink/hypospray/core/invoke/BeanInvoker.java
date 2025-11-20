package eu.jbeernink.hypospray.core.invoke;

import java.lang.reflect.Type;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.invoke.Invoker;

/// Invoker which performs a lookup for a bean using a [BeanContainer].
///
/// All invocations will pass through a wrapped invoker, which will perform the actual invocation on the bean instance
/// that has been retrieved from the container. If the bean has any interceptors defined for the invoked method, these
/// will be invoked as well.
///
/// If the bean to invoke is a dependent scoped bean, an instance will be created only for this invocation. Once the
/// invocation completes, the instance will be destroyed.
///
/// @param beanContainer the bean container to retrieve the bean through.
/// @param bean          the bean to invoke the method on.
/// @param type          the type the method is defined on.
/// @param invoker       a wrapped invoker which will invoke the method on the bean instance.
/// @param <T>           the type of the bean.
/// @param <R>           the type of the result.
public record BeanInvoker<T, R>(BeanContainer beanContainer, Bean<T> bean, Type type, Invoker<T, R> invoker) implements
		Invoker<Void, R> {

	@Override
	public R invoke(Void unused, Object[] arguments) throws Exception {
		CreationalContext<T> creationalContext = beanContainer.createCreationalContext(bean);
		try {
			@SuppressWarnings("unchecked")
			T instance = (T) beanContainer.getReference(bean, type, creationalContext);
			return invoker.invoke(instance, arguments);
		} finally {
			creationalContext.release();
		}
	}
}
