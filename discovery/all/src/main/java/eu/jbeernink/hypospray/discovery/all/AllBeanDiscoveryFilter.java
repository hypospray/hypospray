package eu.jbeernink.hypospray.discovery.all;

import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.ALL;

import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode;

/// Bean discovery filter for bean archives with bean discovery mode `"all"`.
///
/// This filter will always return `true` for all classes.
public class AllBeanDiscoveryFilter implements BeanDiscoveryFilter {

	@Override
	public BeanDiscoveryMode discoveryMode() {
		return ALL;
	}

	@Override
	public boolean isCandidateClass(String className) {
		return true;
	}
}
