package eu.jbeernink.hypospray.discovery.none;

import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.NONE;

import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode;

/// Bean discovery filter for bean archives with bean discovery mode `"none"`.
///
/// This filter will always return false for all classes.
public class NothingBeanDiscoveryFilter implements BeanDiscoveryFilter {
	@Override
	public BeanDiscoveryMode discoveryMode() {
		return NONE;
	}

	@Override
	public boolean isCandidateClass(String className) {
		return false;
	}
}
