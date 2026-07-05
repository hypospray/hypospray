import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryExtension;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;
import eu.jbeernink.hypospray.annotation.service.GenerateServiceDescriptors;

@GenerateServiceDescriptors
module eu.jbeernink.hypospray.discovery.scanner {
	requires jakarta.cdi;
	requires eu.jbeernink.hypospray.annotation;
	requires eu.jbeernink.hypospray.core;
	requires eu.jbeernink.hypospray.xml;
	requires org.jspecify;

	exports eu.jbeernink.hypospray.discovery.scanner;

	uses BeanDiscoveryFilter;

	provides BuildCompatibleExtension with BeanDiscoveryExtension;
}