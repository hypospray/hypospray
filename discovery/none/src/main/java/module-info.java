import eu.jbeernink.hypospray.discovery.none.NothingBeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;

module eu.jbeernink.hypospray.discovery.none {
	requires jakarta.cdi;
	requires eu.jbeernink.hypospray.discovery.scanner;

	provides BeanDiscoveryFilter with NothingBeanDiscoveryFilter;
}