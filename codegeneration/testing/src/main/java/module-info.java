import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;

module eu.jbeernink.hypospray.codegeneration.testing {
	requires jakarta.cdi;
	requires eu.jbeernink.hypospray.codegeneration.generator;
	requires eu.jbeernink.hypospray.util;
	requires org.junit.jupiter.api;

	exports eu.jbeernink.hypospray.codegeneration.testing;
	opens eu.jbeernink.hypospray.codegeneration.testing;

	uses ClientProxyGenerator;
}