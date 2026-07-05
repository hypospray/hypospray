import eu.jbeernink.hypospray.annotation.service.GenerateServiceDescriptors;
import eu.jbeernink.hypospray.codegeneration.classfile.spi.ClassFileApiClientProxyGenerator;
import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;

@GenerateServiceDescriptors
module eu.jbeernink.hypospray.codegeneration.classfile {
	requires jakarta.cdi;
	requires eu.jbeernink.hypospray.annotation;
	requires eu.jbeernink.hypospray.codegeneration.generator;

	provides ClientProxyGenerator with ClassFileApiClientProxyGenerator;
}