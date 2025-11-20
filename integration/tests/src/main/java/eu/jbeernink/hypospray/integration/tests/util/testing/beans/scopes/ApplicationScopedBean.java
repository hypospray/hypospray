package eu.jbeernink.hypospray.integration.tests.util.testing.beans.scopes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;

@ApplicationScoped
public class ApplicationScopedBean {

	private final BeanContainer beanContainer;

	@Inject
	ApplicationScopedBean(BeanContainer beanContainer) {
		this.beanContainer = beanContainer;
	}

	public int multiply(Integer a, Integer b) {
		return a * b;
	}

	public long identityHash() {
		return System.identityHashCode(this);
	}
}
