package eu.jbeernink.hypospray.core.invoke;

import jakarta.enterprise.invoke.Invoker;

/// Invoker which always delegates invocation to a given instance.
///
/// This invoker takes an instance that the method needs to be called on an an invoker that calls that method on any
/// instance. When [#invoke(Void,Object[])] is called, the [#instance] is taken and passed to the
/// [#invoke(Void,Object[])] method of [#invoker].
public record InstanceInvoker<T, R>(T instance, Invoker<T, R> invoker) implements Invoker<Void, R> {
	@Override
	public R invoke(Void ignored, Object[] arguments) throws Exception {
		return invoker.invoke(instance, arguments);
	}
}
