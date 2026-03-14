import javax.annotation.processing.Processor;

import eu.jbeernink.hypospray.compile.servicedescriptor.ServiceDescriptorGenerator;

module eu.jbeernink.hypospray.compile.servicedescriptor {
	requires java.compiler;

	provides Processor with ServiceDescriptorGenerator;
}