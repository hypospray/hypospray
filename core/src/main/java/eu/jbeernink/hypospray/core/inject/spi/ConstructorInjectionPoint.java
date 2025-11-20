package eu.jbeernink.hypospray.core.inject.spi;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record ConstructorInjectionPoint(TypeInstance type, Set<AnnotationInformation> qualifierAnnotations, int index, ParameterInformation parameter,
                                        LateReference<Bean<?>> beanReference) implements InternalInjectionPoint {

	public ConstructorInjectionPoint {
		qualifierAnnotations = Set.copyOf(qualifierAnnotations);
	}

	@Override
	public Bean<?> getBean() {
		return beanReference.get();
	}

	@Override
	public boolean isDelegate() {
		return false;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
}
