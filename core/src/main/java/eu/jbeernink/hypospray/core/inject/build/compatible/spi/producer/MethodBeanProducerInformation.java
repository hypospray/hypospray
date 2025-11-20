package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import java.util.Set;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.MethodConfiguration;

public record MethodBeanProducerInformation<T>(MethodConfiguration producerMethod, MethodConfiguration disposerMethod,
                                               Set<InjectionPointInformation> injectionPoints) implements
		BeanProducerInformation<T> {

}
