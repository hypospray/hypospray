package eu.jbeernink.hypospray.core.context.dependent;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

public class DependentScopeContext implements Context {

	@Override
	public Class<? extends Annotation> getScope() {
		return Dependent.class;
	}

	@Override
	public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		return contextual.create(creationalContext);
	}

	@Override
	public <T> T get(Contextual<T> contextual) {
		return null;
	}

	@Override
	public boolean isActive() {
		return true;
	}
}
