import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;

module eu.jbeernink.hypospray.codegeneration.generator {
	requires transitive eu.jbeernink.hypospray.model;

	exports eu.jbeernink.hypospray.codegeneration.generator to eu.jbeernink.hypospray.codegeneration.classfile, eu.jbeernink.hypospray.codegeneration.testing, eu.jbeernink.hypospray.core;
	exports eu.jbeernink.hypospray.codegeneration.generator.proxy to eu.jbeernink.hypospray.codegeneration.classfile, eu.jbeernink.hypospray.codegeneration.testing, eu.jbeernink.hypospray.core;
	exports eu.jbeernink.hypospray.codegeneration.generator.spi to eu.jbeernink.hypospray.codegeneration.classfile, eu.jbeernink.hypospray.codegeneration.testing;

	uses ClientProxyGenerator;
}