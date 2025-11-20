package eu.jbeernink.hypospray.util.stream;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collector;

import eu.jbeernink.hypospray.util.stream.collector.FindOnlyCollector;

/// Collection of common collectors.
public final class Collectors {

	/// {@return a collector that returns a an [Optional] containing the only element of a stream, if present, or throws an exception if there's more than one element}
	///
	/// @param exceptionSupplier a supplier that provides the exception to throw if the stream has more than one element.
	public static <T> Collector<T, ?, Optional<T>> findOnly(Supplier<RuntimeException> exceptionSupplier) {
		return new FindOnlyCollector<>(exceptionSupplier);
	}

	private Collectors() {
	}
}
