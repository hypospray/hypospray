package eu.jbeernink.hypospray.codegeneration.generator;

import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;
import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxy;
import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxyConfiguration;
import eu.jbeernink.hypospray.model.information.ClassInformation;

public final class ClientProxyBuilder {

	private final ClientProxyGenerator clientProxyGenerator;
	private final String proxyClassName;

	private Class<?> proxiedClass = Object.class;
	private ClassInformation<?> proxiedClassInformation;

	ClientProxyBuilder(ClientProxyGenerator clientProxyGenerator, String proxyClassName) {
		this.clientProxyGenerator = clientProxyGenerator;
		this.proxyClassName = proxyClassName;
	}

	public ClientProxyBuilder withProxiedClass(Class<?> proxiedClass) {
		this.proxiedClass = proxiedClass;

		return this;
	}

	public ClientProxyBuilder withProxiedClass(ClassInformation<?> classInformation) {
		this.proxiedClassInformation = classInformation;

		return this;
	}

	public ClientProxy build() {
		var config = new ClientProxyConfiguration(proxiedClass, proxiedClassInformation, proxyClassName);

		return clientProxyGenerator.createClientProxy(config);
	}

}
