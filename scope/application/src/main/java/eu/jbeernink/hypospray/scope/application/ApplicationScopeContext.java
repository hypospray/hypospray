package eu.jbeernink.hypospray.scope.application;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import eu.jbeernink.hypospray.scope.common.ScopedInstanceManager;
import eu.jbeernink.hypospray.scope.common.ScopeInstanceKey;

public class ApplicationScopeContext implements AlterableContext {

	private enum ApplicationScopeKey implements ScopeInstanceKey {INSTANCE}

	private final ScopedInstanceManager<ApplicationScopeKey> scopedInstanceManager = new ScopedInstanceManager<>();

	@Override
	public Class<? extends Annotation> getScope() {
		return ApplicationScoped.class;
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		return scopedInstanceManager.getInstance(ApplicationScopeKey.INSTANCE, contextual).orElse(null);
	}

	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		return scopedInstanceManager.getOrCreateInstance(ApplicationScopeKey.INSTANCE, contextual, creationalContext);
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void destroy(Contextual<?> contextual) {
		scopedInstanceManager.destroyInstance(ApplicationScopeKey.INSTANCE, contextual);
	}

	public void destroyScope() {
		scopedInstanceManager.destroyScope(ApplicationScopeKey.INSTANCE);
	}
}
