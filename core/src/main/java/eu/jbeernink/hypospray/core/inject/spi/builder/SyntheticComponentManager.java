package eu.jbeernink.hypospray.core.inject.spi.builder;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserverBuilder;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.lang.model.types.Type;

import eu.jbeernink.hypospray.core.inject.spi.ManagedContextual;

/// Manager of synthetic beans during container initialization.
public final class SyntheticComponentManager implements SyntheticComponents {

	private final Supplier<BeanContainer> beanContainerSupplier;
	private final List<InternalSyntheticBeanBuilder<?>> syntheticBeanBuilders = new ArrayList<>();

	public SyntheticComponentManager(Supplier<BeanContainer> beanContainerSupplier) {
		this.beanContainerSupplier = beanContainerSupplier;
	}

	@Override
	public <T> SyntheticBeanBuilder<T> addBean(Class<T> implementationClass) {
		InternalSyntheticBeanBuilder<T> syntheticBeanBuilder =
				new InternalSyntheticBeanBuilder<>(implementationClass, beanContainerSupplier);

		syntheticBeanBuilders.add(syntheticBeanBuilder);

		return syntheticBeanBuilder;
	}

	@Override
	public <T> SyntheticObserverBuilder<T> addObserver(Class<T> eventType) {
		return unimplemented();
	}

	@Override
	public <T> SyntheticObserverBuilder<T> addObserver(Type eventType) {
		return unimplemented();
	}

	/// Build all synthetic contextuals that have been created through this synthetic component manager.
	///
	/// @return the list of built synthetic contextuals.
	public List<ManagedContextual<?>> buildSyntheticContextuals() {
		return syntheticBeanBuilders.stream().map(InternalSyntheticBeanBuilder::build).collect(toUnmodifiableList());
	}
}
