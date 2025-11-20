package eu.jbeernink.hypospray.discovery.none.test;

import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.NONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ServiceLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode;

@DisplayName("NothingBeanDiscoveryFilter")
class NothingBeanDiscoveryFilterTest {

	private BeanDiscoveryFilter nothingBeanDiscoveryFilter;

	@BeforeEach
	void setup() {
		nothingBeanDiscoveryFilter = ServiceLoader.load(BeanDiscoveryFilter.class)
		                                          .stream()
		                                          .filter(provider -> provider.type()
		                                                                      .getName()
		                                                                      .equals(
				                                                                      "eu.jbeernink.hypospray.discovery.none.NothingBeanDiscoveryFilter"))
		                                          .map(ServiceLoader.Provider::get)
		                                          .findFirst()
		                                          .orElseThrow();
	}

	@Test
	@DisplayName("discoveryMode() returns NONE.")
	void discoveryMode_returnsNone() {
		BeanDiscoveryMode discoveryMode = nothingBeanDiscoveryFilter.discoveryMode();

		assertEquals(NONE, discoveryMode);
	}

	@Test
	@DisplayName("isCandidateClass() returns false.")
	void isCandidateClass_returnsFalse() {
		boolean isCandidateClass = nothingBeanDiscoveryFilter.isCandidateClass(String.class.getName());

		assertFalse(isCandidateClass);
	}
}