package eu.jbeernink.hypospray.core.inject.spi.producer;

import java.util.Set;
import java.util.function.Supplier;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jspecify.annotations.NullMarked;

/// Injection target instance that calls a specified supplier to get an instance of a given bean.
@NullMarked
public record SupplierInjectionTarget<T>(Supplier<T> supplier) implements InjectionTarget<T> {
	@Override
	public void inject(Object instance, CreationalContext ctx) {
	}

	@Override
	public void postConstruct(Object instance) {
	}

	@Override
	public void preDestroy(Object instance) {
	}

	@Override
	public Object produce(CreationalContext ctx) {
		return supplier.get();
	}

	@Override
	public void dispose(Object instance) {
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Set.of();
	}
}
