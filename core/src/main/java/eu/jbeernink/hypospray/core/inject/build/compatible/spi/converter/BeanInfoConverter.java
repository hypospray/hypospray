package eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.BeanInformation;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ScopeInformation;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.model.information.ClassInformation;

public class BeanInfoConverter implements BiFunction<Bean<?>, BeanContainer, BeanInformation<?>> {

	private final InjectionPointInfoConverter injectionPointInfoConverter;
	private final ScopeInfoConverter scopeInfoConverter;

	public BeanInfoConverter() {
		this.injectionPointInfoConverter = new InjectionPointInfoConverter();
		this.scopeInfoConverter = new ScopeInfoConverter();
	}

	// TODO this method should accept ManagedBean instead of Bean.
	@Override
	public BeanInformation<?> apply(Bean<?> bean, BeanContainer beanContainer) {
		return convert(bean, beanContainer);
	}

	private <T> BeanInformation<T> convert(Bean<T> bean, BeanContainer beanContainer) {
		return new BeanInformation<>(getBeanScope(bean, beanContainer), List.of(), Set.of(),
				getBeanClassInformation(bean), false, null, null, null, List.of());
	}

	private <T> ScopeInformation getBeanScope(Bean<T> bean, BeanContainer beanContainer) {
		return scopeInfoConverter.apply(bean.getScope(), beanContainer);
	}

	private <T> ClassInformation<T> getBeanClassInformation(Bean<T> bean) {
		return switch (bean) {
			case DiscoveredBean<T> discoveredBean -> (ClassInformation<T>)discoveredBean.beanClass();
			case Bean<T> _ -> unimplemented();
		};
	}
}
