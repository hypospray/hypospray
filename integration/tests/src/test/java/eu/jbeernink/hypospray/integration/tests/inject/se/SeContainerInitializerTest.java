package eu.jbeernink.hypospray.integration.tests.inject.se;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.integration.tests.util.testing.beans.UnqualifiedBean;

@DisplayName("SeContainerInitializer")
public class SeContainerInitializerTest {

	@Test
	@DisplayName("initialize() by default enables bean discovery.")
	void initialize_enablesBeanDiscoveryByDefault() {
		try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
			assertTrue(container.select(UnqualifiedBean.class).isResolvable());
		}
	}

	@Test
	@DisplayName("disableDiscovery() disables bean discovery.")
	void disableDiscovery_disablesBeanDiscovery() {
		try (SeContainer container = SeContainerInitializer.newInstance().disableDiscovery().initialize()) {
			assertFalse(container.select(UnqualifiedBean.class).isResolvable());
		}
	}
}
