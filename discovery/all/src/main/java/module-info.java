import eu.jbeernink.hypospray.annotation.service.GenerateServiceDescriptors;
import eu.jbeernink.hypospray.discovery.all.AllBeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;

@GenerateServiceDescriptors
module eu.jbeernink.hypospray.discovery.all {
	requires eu.jbeernink.hypospray.annotation;
	requires eu.jbeernink.hypospray.discovery.scanner;
	requires jakarta.cdi;

	exports eu.jbeernink.hypospray.discovery.all to eu.jbeernink.hypospray.core;

	provides BeanDiscoveryFilter with AllBeanDiscoveryFilter;
}