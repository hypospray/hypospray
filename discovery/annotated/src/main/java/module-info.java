import eu.jbeernink.hypospray.discovery.annotated.AnnotatedBeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;

module eu.jbeernink.hypospray.discovery.annotated {
	requires eu.jbeernink.hypospray.core;
	requires eu.jbeernink.hypospray.discovery.scanner;
	requires jakarta.cdi;

	exports eu.jbeernink.hypospray.discovery.annotated to eu.jbeernink.hypospray.core;

	provides BeanDiscoveryFilter with AnnotatedBeanDiscoveryFilter;
}