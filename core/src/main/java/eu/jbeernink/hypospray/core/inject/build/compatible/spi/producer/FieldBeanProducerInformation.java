package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import java.util.Set;

import eu.jbeernink.hypospray.model.information.FieldInformation;

public record FieldBeanProducerInformation<T>(FieldInformation producerField,
                                              Set<InjectionPointInformation> injectionPoints) implements
		BeanProducerInformation<T> {

	public FieldBeanProducerInformation {
		injectionPoints = Set.copyOf(injectionPoints);
	}

}
