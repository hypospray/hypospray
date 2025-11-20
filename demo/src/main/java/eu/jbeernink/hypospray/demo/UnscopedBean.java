package eu.jbeernink.hypospray.demo;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

@Dependent
public class UnscopedBean {

	@Inject
	private InjectionPoint injectionPoint;

	@Override
	public String toString() {
		return "UnscopedBean{" + "injectionPoint=" + injectionPoint + '}';
	}
}
