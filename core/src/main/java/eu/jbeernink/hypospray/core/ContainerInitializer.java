package eu.jbeernink.hypospray.core;

import java.lang.System.Logger;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.Extension;

import eu.jbeernink.hypospray.core.factory.ContainerFactory;
import eu.jbeernink.hypospray.core.factory.ContainerSettings;

/// A container initializer that is used to create [Container] instances.
public class ContainerInitializer extends SeContainerInitializer {

	private static final Logger logger = System.getLogger(ContainerInitializer.class.getName());

	private boolean beanDiscoveryEnabled = true;

	private final Set<Class<?>> beanClassesToAdd = new HashSet<>();

	@Override
	public SeContainerInitializer addBeanClasses(Class<?>... classes) {
		beanClassesToAdd.addAll(List.of(classes));

		return this;
	}

	@Override
	public SeContainerInitializer addPackages(Class<?>... classes) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer addPackages(boolean b, Class<?>... classes) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer addPackages(Package... packages) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer addPackages(boolean b, Package... packages) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer addExtensions(Extension... extensions) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	@SafeVarargs
	public final SeContainerInitializer addExtensions(Class<? extends Extension>... classes) {
		// TODO add optional support for extensions.
		logger.log(Logger.Level.WARNING, "Ignoring extension registration,CDI Lite does not support extensions.");

		return this;
	}

	@Override
	public SeContainerInitializer enableInterceptors(Class<?>... classes) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer enableDecorators(Class<?>... classes) {
		// TODO implement optional support for decorators.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer selectAlternatives(Class<?>... classes) {
		// TODO implement
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@SafeVarargs
	@Override
	public final SeContainerInitializer selectAlternativeStereotypes(Class<? extends Annotation>... classes) {
		// TODO implement
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainerInitializer addProperty(String s, Object o) {
		// TODO implement
		return this;
	}

	@Override
	public SeContainerInitializer setProperties(Map<String, Object> map) {
		// TODO implement
		return this;
	}

	@Override
	public SeContainerInitializer disableDiscovery() {
		beanDiscoveryEnabled = false;

		return this;
	}

	@Override
	public SeContainerInitializer setClassLoader(ClassLoader classLoader) {
		// TODO implement
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public SeContainer initialize() {
		return ContainerFactory.newInstance().createContainer(new ContainerSettings());
	}
}
