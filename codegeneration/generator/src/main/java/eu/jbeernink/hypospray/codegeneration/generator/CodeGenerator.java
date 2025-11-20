package eu.jbeernink.hypospray.codegeneration.generator;

import java.util.ServiceLoader;

import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;

public class CodeGenerator {

	private final ClientProxyGenerator clientProxyGenerator;

	private CodeGenerator(ClientProxyGenerator clientProxyGenerator) {
		this.clientProxyGenerator = clientProxyGenerator;
	}

	public ClientProxyBuilder newClientProxy(String className) {
		return new ClientProxyBuilder(clientProxyGenerator, className);
	}

	public static CodeGenerator getInstance() {
		return new CodeGenerator(loadInstance(ClientProxyGenerator.class));
	}

	private static <T> T loadInstance(Class<T> factoryClass) {
		return ServiceLoader.load(factoryClass).stream().findFirst().map(ServiceLoader.Provider::get).orElseThrow();
	}

}
