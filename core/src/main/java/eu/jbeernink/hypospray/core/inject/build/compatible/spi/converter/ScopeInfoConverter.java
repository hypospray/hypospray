package eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter;

import java.lang.annotation.Annotation;
import java.util.function.BiFunction;

import jakarta.enterprise.inject.spi.BeanContainer;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ScopeInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;

/// Converter from a CDI scope annotation class to [ScopeInformation].
public class ScopeInfoConverter implements BiFunction<Class<? extends Annotation>, BeanContainer, ScopeInformation> {

	/// Convert from a CDI scope annotation class to [ScopeInformation].
	@Override
	public ScopeInformation apply(Class<? extends Annotation> scopeClass, BeanContainer beanContainer) {
		boolean normalScope = beanContainer.isNormalScope(scopeClass);
		// TODO use ClassInformation directly to create a ScopeInformation instance and only fallback to reflection when needed.

		return new ScopeInformation(ClassInformationSource.getInstance().getClassInformation(scopeClass), normalScope);
	}
}
