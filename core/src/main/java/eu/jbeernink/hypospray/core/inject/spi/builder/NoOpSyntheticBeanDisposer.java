package eu.jbeernink.hypospray.core.inject.spi.builder;

import static java.lang.System.Logger.Level.DEBUG;

import java.lang.System.Logger;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;

/// {link SyntheticBeanDisposer} which does not take any action on disposal.
final class NoOpSyntheticBeanDisposer<T> implements SyntheticBeanDisposer<T> {

	private static final Logger logger = System.getLogger(NoOpSyntheticBeanDisposer.class.getName());

	@Override
	public void dispose(T instance, Instance<Object> lookup, Parameters params) {
		logger.log(DEBUG, "No-op disposer called for synthetic bean: %s.".formatted(instance));
	}
}
