package eu.jbeernink.hypospray.core;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.invoke.Invoker;

import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.core.annotation.ProxyCreationContext;

/// Producer that provides part of the execution context of a proxy during proxy creation.
@NullMarked
public final class ProxyContextProducer {

	private record ProxyContext(Function<String, Invoker<Void, Object>> invokerFactory) {}

	private static final ScopedValue<ProxyContext> proxyContext = ScopedValue.newInstance();

	/// Produce a function that returns a factory for creating invokers for the proxy to use.
	///
	/// @return a function to create invokers.
	/// @throws IllegalStateException if no proxy context is active.
	@Produces
	@ProxyCreationContext
	public static Function<String, Invoker<Void, Object>> produceInvokerFactory() {
		try {
			return proxyContext.get().invokerFactory;
		} catch (NoSuchElementException e) {
			throw new IllegalStateException("No proxy context is currently active.", e);
		}
	}

	/// Execute a [Supplier] with the given proxy context for a normal scoped bean proxy.
	///
	/// @param invokerFactory the invoker factory to use for the proxy.
	/// @param supplier       the supplier to execute.
	/// @param <T>            the type of return value.
	/// @return the result of the supplier.
	public static <T> T withProxyContext(Function<String, Invoker<Void, Object>> invokerFactory, Supplier<T> supplier) {
		return withProxyContext(new ProxyContext(invokerFactory), supplier);
	}

	/// Execute a [Supplier] with the given proxy context for a non-normal scoped bean proxy.
	///
	/// @param invokerFactory    the invoker factory to use for the proxy.
	/// @param bean              the bean the proxy is for.
	/// @param dependentInstance the instance to be proxied.
	/// @param supplier          the supplier to execute.
	/// @param <T>               the type of return value.
	/// @return the result of the supplier.
	public static <T> T withNonNormalScopedProxyContext(Function<String, Invoker<Void, Object>> invokerFactory,
	                                                    Bean<?> bean, Object dependentInstance, Supplier<T> supplier) {
		return withProxyContext(new ProxyContext(invokerFactory), supplier);
	}

	private static <T> T withProxyContext(ProxyContext newProxyContext, Supplier<T> supplier) {
		return ScopedValue.where(proxyContext, newProxyContext).call(supplier::get);
	}
}
