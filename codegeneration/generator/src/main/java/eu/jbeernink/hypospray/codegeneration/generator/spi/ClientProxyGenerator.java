package eu.jbeernink.hypospray.codegeneration.generator.spi;

import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxy;
import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxyConfiguration;

public interface ClientProxyGenerator {

	ClientProxy createClientProxy(ClientProxyConfiguration clientProxyConfiguration);
}
