package eu.jbeernink.hypospray.codegeneration.generator.proxy;

import eu.jbeernink.hypospray.model.information.ClassInformation;

public record ClientProxyConfiguration(@Deprecated Class<?> proxiedClass, ClassInformation<?> proxiedClassInformation,
                                       String proxyClassName) {}
