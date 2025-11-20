package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;


import java.util.Set;

public sealed interface BeanProducerInformation<T> permits ConstructorBeanProducerInformation,
		FieldBeanProducerInformation, MethodBeanProducerInformation {

	Set<InjectionPointInformation> injectionPoints();

}
