package eu.jbeernink.hypospray.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.enterprise.inject.se.SeContainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.test.extension.FakeBuildCompatibleExtension;
import eu.jbeernink.hypospray.core.test.extension.FakeBuildCompatibleExtension.DiscoveryPhase;

@DisplayName("ContainerInitializer")
class ContainerInitializerTest {

	private ContainerInitializer containerInitializer = new ContainerInitializer();

	@BeforeEach
	void setup() {
		FakeBuildCompatibleExtension.reset();
	}

	@Nested
	@DisplayName("with bean discovery disabled")
	class WithBeanDiscoveryDisabled {

		@BeforeEach
		void setup() {
			containerInitializer.disableDiscovery();
		}

		@Test
		@DisplayName("when calling initialize() a Container instance is returned.")
		void initializer_returnsContainer() {
			try (SeContainer initialize = containerInitializer.initialize()) {

				assertTrue(initialize instanceof Container);
			}
		}
	}

	@Nested
	@DisplayName("with bean discovery enabled")
	class WithBeanDiscoveryEnabled {

		@Test
		@DisplayName("when calling initialize() a Container instance is returned.")
		void initialize_returnsContainer() {
			try (SeContainer initialize = containerInitializer.initialize()) {

				assertTrue(initialize instanceof Container);
			}
		}

		@Disabled // TODO: figure out why test does not work.
		@Test
		@DisplayName("when calling initialize() calls the lifecycle methods in order.")
		void initialize_callsDiscoveryExtensionMethods() {
			try (var _ = containerInitializer.initialize()) {
				List<? extends Class<?>> phases = FakeBuildCompatibleExtension.phases().stream().map(Object::getClass).toList();
				assertEquals(List.of(DiscoveryPhase.class), phases);
			}
		}
	}

}