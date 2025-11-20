package eu.jbeernink.hypospray.util.stream;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Gatherer;

/// Collection of gatherers.
public final class Gatherers {

	/// Create a [Gatherer] that filters out elements based on a value derived from an element using a given function.
	///
	/// The derived values are compared for equality using their `equals` function.
	///
	/// @param <T>                  The type of the elements.
	/// @param <F>                  The type of the derived values.
	/// @param derivedValueFunction A function to derive a value from an element.
	/// @return A gatherer that filters out elements based on a derived value.
	public static <T, F> Gatherer<T, ?, T> distinctBy(Function<? super T, ? extends F> derivedValueFunction) {
		return Gatherer.ofSequential(() -> new HashSet<F>(), (previousElements, element, downstream) -> {
			if (previousElements.add(derivedValueFunction.apply(element))) {
				return downstream.push(element);
			}

			return true;
		});
	}

	private Gatherers() {
	}
}
