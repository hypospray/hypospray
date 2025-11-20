package eu.jbeernink.hypospray.core.inject.spi.factory;

import java.util.Set;
import java.util.function.Supplier;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedContextual;

public interface ContextualFactory<E extends ManagedContextual<?>> {

	boolean isCandidate(ClassConfiguration<?> classConfiguration);

	Set<E> processCandidate(ClassConfiguration<?> classConfiguration, Supplier<InjectableBeanContainer> beanContainerSupplier);

}
