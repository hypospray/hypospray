package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ConstructorConfiguration;

public record ConstructorBeanProducerInformation<T>(ConstructorConfiguration<T> methodConfiguration,
                                                    List<ConstructorInjectionPointInformation> constructorInjectionPoints,
                                                    Set<FieldInjectionPointInformation> fieldInjectionPoints,
                                                    Set<InitializationMethodInjectionPointInformation> initializationMethodInjectionPoints) implements
		BeanProducerInformation<T> {

	public ConstructorBeanProducerInformation {
		constructorInjectionPoints = List.copyOf(constructorInjectionPoints);
		fieldInjectionPoints = Set.copyOf(fieldInjectionPoints);
		initializationMethodInjectionPoints = Set.copyOf(initializationMethodInjectionPoints);
	}

	@Override
	public Set<InjectionPointInformation> injectionPoints() {
		return Stream.concat(
				Stream.<InjectionPointInformation>concat(constructorInjectionPoints.stream(), fieldInjectionPoints.stream()),
				initializationMethodInjectionPoints.stream()).collect(toUnmodifiableSet());
	}
}
