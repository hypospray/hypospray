package eu.jbeernink.hypospray.core.inject;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Producer that provides the current [InjectionPoint] for injection.
@NullMarked
public final class InjectionPointProducer {

	private static final ScopedValue<InjectionPoint> currentInjectionPoint = ScopedValue.newInstance();

	/// Produce the current [InjectionPoint].
	///
	/// @return the current injection point.
	/// @throws IllegalStateException if no injection point is currently active.
	@Produces
	public static InjectionPoint produceInjectionPoint() {
		try {
			return currentInjectionPoint.get();
		} catch (NoSuchElementException e) {
			throw new IllegalStateException("No current injection point found.", e);
		}
	}

	/// Run the given [Supplier] with the given [InjectionPoint] available for injection.
	///
	/// @param injectionPoint the injection point.
	/// @param supplier       the supplier to execute.
	/// @param <T>            the type of the result.
	/// @return the result from the supplier.
	public static <T extends @Nullable Object> T withInjectionPoint(InjectionPoint injectionPoint, Supplier<T> supplier) {
		if (injectionPoint.getType().equals(InjectionPoint.class)) {
			// Don't overwrite the current injection point if the injected value itself is an injection point.
			return supplier.get();
		}

		return ScopedValue.where(currentInjectionPoint, injectionPoint).call(supplier::get);
	}
}
