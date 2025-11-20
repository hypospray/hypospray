package eu.jbeernink.hypospray.codegeneration.classfile.test;

import eu.jbeernink.hypospray.codegeneration.testing.ClientProxyGeneratorTestBase;

class ClassFileApiClientProxyGeneratorTest extends ClientProxyGeneratorTestBase {


	@Override
	protected String clientProxyFactoryName() {
		return "eu.jbeernink.hypospray.codegeneration.classfile.spi.ClassFileApiClientProxyGenerator";
	}
}