package eu.jbeernink.hypospray.integration.tests.jakarta.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.enterprise.inject.spi.CDI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.integration.tests.util.testing.beans.MySingleton;
import eu.jbeernink.hypospray.testing.extension.InContainer;

@InContainer
@DisplayName("@Singleton:")
public class SingletonTest {

	private CDI<Object> cdi;

	@BeforeEach
	void setup() {
		cdi = CDI.current();
	}

	@Test
	@DisplayName("a singleton bean must not be proxied.")
	void singletonBean_mustNotBeProxied() {
		MySingleton mySingleton = cdi.select(MySingleton.class).get();

		assertEquals(MySingleton.class, mySingleton.getClass());
	}

	@Test
	@DisplayName("the container should always return the same instance for a singleton bean.")
	void container_mustReturnSameInstanceForSingleton() {
		var expectedInstance = cdi.select(MySingleton.class).get();

		var actualInstance = cdi.select(MySingleton.class).get();

		assertSame(expectedInstance, actualInstance);
	}

}
