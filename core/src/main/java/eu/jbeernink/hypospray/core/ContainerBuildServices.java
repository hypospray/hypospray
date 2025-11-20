package eu.jbeernink.hypospray.core;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import jakarta.enterprise.inject.build.compatible.spi.BuildServices;

import eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderFactory;

/// A [BuildServices] that provides services to [jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension] instances.
public final class ContainerBuildServices implements BuildServices {

	@Override
	public AnnotationBuilderFactory annotationBuilderFactory() {
		return new SyntheticAnnotationInformationBuilderFactory();
	}

	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}
}
