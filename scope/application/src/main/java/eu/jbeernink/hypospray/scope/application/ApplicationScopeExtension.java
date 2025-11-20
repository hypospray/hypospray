package eu.jbeernink.hypospray.scope.application;

import static eu.jbeernink.hypospray.core.priority.ExtensionPriority.APPLICATION_SCOPE_EXTENSION;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;

/// Extension for registering the context for the [ApplicationScoped] annotation.
@Priority(APPLICATION_SCOPE_EXTENSION)
public class ApplicationScopeExtension implements BuildCompatibleExtension {

	@Discovery
	public void registerContext(MetaAnnotations metaAnnotations) {
		metaAnnotations.addContext(ApplicationScoped.class, ApplicationScopeContext.class);
	}
}
