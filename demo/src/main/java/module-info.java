import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import eu.jbeernink.hypospray.demo.extension.DemoBuildCompatibleExtension;
import eu.jbeernink.hypospray.demo.synthetic.SyntheticBeanRegistrationExtension;

module eu.jbeernink.hypospray.demo {
	requires jakarta.cdi;
	requires java.logging;
	opens eu.jbeernink.hypospray.demo to eu.jbeernink.hypospray.core;
	opens eu.jbeernink.hypospray.demo.extension to eu.jbeernink.hypospray.core;
	opens eu.jbeernink.hypospray.demo.synthetic to eu.jbeernink.hypospray.core;

	provides BuildCompatibleExtension with DemoBuildCompatibleExtension, SyntheticBeanRegistrationExtension;
}