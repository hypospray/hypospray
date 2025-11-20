import eu.jbeernink.hypospray.codegeneration.classfile.spi.ClassFileApiClientProxyGenerator;
import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;

module eu.jbeernink.hypospray.codegeneration.classfile {
	requires jakarta.cdi;
	requires eu.jbeernink.hypospray.codegeneration.generator;

	provides ClientProxyGenerator with ClassFileApiClientProxyGenerator;
}