package eu.jbeernink.hypospray.scope.request;

import static eu.jbeernink.hypospray.scope.request.RequestContextManager.getCurrentRequestId;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import eu.jbeernink.hypospray.scope.common.ScopedInstanceManager;

/// Context for supporting [RequestScoped] beans.
public class RequestContext implements AlterableContext {

	private final ScopedInstanceManager<RequestId> scopedInstanceManager = new ScopedInstanceManager<>();

	@Override
	public void destroy(Contextual<?> contextual) {
		scopedInstanceManager.destroyInstance(getActiveScope(), contextual);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return RequestScoped.class;
	}

	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		return scopedInstanceManager.getOrCreateInstance(getActiveScope(), contextual, creationalContext);
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		return scopedInstanceManager.getInstance(getActiveScope(), contextual).orElse(null);
	}

	@Override
	public boolean isActive() {
		return getCurrentRequestId().isPresent();
	}


	public void destroyScope(RequestId requestId) {
		scopedInstanceManager.destroyScope(requestId);
	}

	private static RequestId getActiveScope() {
		return getCurrentRequestId().orElseThrow(ContextNotActiveException::new);
	}
}
