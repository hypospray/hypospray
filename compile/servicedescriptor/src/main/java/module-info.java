import javax.annotation.processing.Processor;

import eu.jbeernink.hypospray.compile.servicedescriptor.ServiceDescriptorGenerator;

module eu.jbeernink.hypospray.compile.servicedescriptor {
	requires java.compiler;
	requires eu.jbeernink.hypospray.compile.annotation;
	provides Processor with ServiceDescriptorGenerator;
}