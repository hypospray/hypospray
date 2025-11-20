package eu.jbeernink.hypospray.scope.request;

import static eu.jbeernink.hypospray.core.priority.ExtensionPriority.REQUEST_SCOPE_EXTENSION;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;

@Priority(REQUEST_SCOPE_EXTENSION)
public class RequestScopeExtension implements BuildCompatibleExtension {

	@Discovery
	public void registerScope(MetaAnnotations metaAnnotations) {
		metaAnnotations.addContext(RequestScoped.class, RequestContext.class);
	}
}
