import eu.jbeernink.hypospray.annotation.service.GenerateServiceDescriptors;
import eu.jbeernink.hypospray.discovery.annotated.AnnotatedBeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;

@GenerateServiceDescriptors
module eu.jbeernink.hypospray.discovery.annotated {
	requires jakarta.cdi;

	requires eu.jbeernink.hypospray.annotation;
	requires eu.jbeernink.hypospray.core;
	requires eu.jbeernink.hypospray.discovery.scanner;

	exports eu.jbeernink.hypospray.discovery.annotated to eu.jbeernink.hypospray.core;

	provides BeanDiscoveryFilter with AnnotatedBeanDiscoveryFilter;
}