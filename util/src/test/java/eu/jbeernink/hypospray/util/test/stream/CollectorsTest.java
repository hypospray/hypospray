package eu.jbeernink.hypospray.util.test.stream;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Collectors")
class CollectorsTest {

	@Nested
	@DisplayName("findOnly(Supplier<RuntimeException>)")
	class FindOnly {

		@Test
		@DisplayName("on a stream with one element, returns an optional containing that element.")
		void onStreamWithOneElement_returnsNonEmptyOptional() {
			var element = "s";

			Optional<String> optionalElement = Stream.of(element).collect(findOnly(RuntimeException::new));

			assertEquals(element, optionalElement.orElseThrow());
		}

		@Test
		@DisplayName("on an empty stream, returns empty optional.")
		void onEmptyStream_throwsNoSuchElementException() {
			Optional<Object> optional = Stream.of().collect(findOnly(RuntimeException::new));

			assertTrue(optional.isEmpty());
		}

		@Test
		@DisplayName("on a stream with multiple elements, throws supplied exception.")
		void onStreamWithMultipleElements_throwsSuppliedException() {
			var errorMessage = "Illegal state";

			var exception = assertThrows(IllegalStateException.class,
					() -> Stream.of("a", "b").collect(findOnly(() -> new IllegalStateException(errorMessage))));

			assertEquals(errorMessage, exception.getMessage());
		}
	}

}