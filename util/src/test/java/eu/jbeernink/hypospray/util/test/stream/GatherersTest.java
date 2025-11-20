package eu.jbeernink.hypospray.util.test.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Gatherer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.util.stream.Gatherers;

@DisplayName("Gatherers")
class GatherersTest {

	@DisplayName("distinctBy(Function<T, F>)")
	@Nested
	class DistinctBy {

		@Test
		@DisplayName("returns a gatherer that filters out duplicate elements using the mapper function.")
		void returnsAGathererThatFiltersOutDuplicateElements() {
			var strings = List.of("a", "aa", "ab", "b", "ba", "c", "ac", "ca");
			Gatherer<String, ?, String> distinctFirstCharacter = Gatherers.distinctBy((String string) -> string.charAt(0));

			List<String> distinctStrings = strings.stream().gather(distinctFirstCharacter).toList();

			assertEquals(List.of("a", "b", "c"), distinctStrings);
		}

	}

}