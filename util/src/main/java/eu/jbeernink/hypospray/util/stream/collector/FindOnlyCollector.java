package eu.jbeernink.hypospray.util.stream.collector;

import static java.util.stream.Collector.Characteristics.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/// Collector that returns an [Optional] containing a single element of a stream, if present, or throws an exception if
/// more than one element is present.
///
/// @param <T> the type of the elements in the stream.
public class FindOnlyCollector<T> implements Collector<T, List<T>, Optional<T>> {

	private final Supplier<RuntimeException> exceptionSupplier;

	public FindOnlyCollector(Supplier<RuntimeException> exceptionSupplier) {
		this.exceptionSupplier = exceptionSupplier;
	}

	@Override
	public Supplier<List<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<List<T>, T> accumulator() {
		return (state, item) -> {
			if (!state.isEmpty()) {
				throw exceptionSupplier.get();
			}
			state.add(item);
		};
	}

	@Override
	public BinaryOperator<List<T>> combiner() {
		return (list1, list2) -> {
			list2.forEach(item -> accumulator().accept(list1, item));

			return list1;
		};
	}

	@Override
	public Function<List<T>, Optional<T>> finisher() {
		return list -> list.stream().findFirst();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Set.of(CONCURRENT, UNORDERED);
	}
}
