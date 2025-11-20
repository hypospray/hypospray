package eu.jbeernink.hypospray.discovery.all.test;

import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.ALL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ServiceLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode;

@DisplayName("AllBeanDiscoveryFilter")
public class AllBeanDiscoveryFilterTest {

	private BeanDiscoveryFilter allBeanDiscoveryFilter;

	@BeforeEach
	void setup() {
		allBeanDiscoveryFilter = ServiceLoader.load(BeanDiscoveryFilter.class)
		                                      .stream()
		                                      .filter(provider -> provider.type()
		                                                                  .getName()
		                                                                  .equals(
				                                                                  "eu.jbeernink.hypospray.discovery.all.AllBeanDiscoveryFilter"))
		                                      .map(ServiceLoader.Provider::get)
		                                      .findFirst()
		                                      .orElseThrow();
	}

	@Test
	@DisplayName("discoveryMode() returns ALL.")
	void discoveryMode_returnsAll() {
		BeanDiscoveryMode discoveryMode = allBeanDiscoveryFilter.discoveryMode();

		assertEquals(ALL, discoveryMode);
	}

	@Test
	@DisplayName("isCandidateClass() returns true.")
	void isCandidateClass_returnsTrue() {
		boolean isCandidateClass = allBeanDiscoveryFilter.isCandidateClass(String.class.getName());

		assertTrue(isCandidateClass);
	}
}
