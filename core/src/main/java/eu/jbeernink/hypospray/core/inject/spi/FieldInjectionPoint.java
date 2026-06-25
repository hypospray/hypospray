package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.warnNotYetImplemented;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record FieldInjectionPoint(TypeInstance type, Set<AnnotationInformation> qualifierAnnotations, FieldInformation field,
                                  LateReference<Bean<?>> managedBeanReference, Invoker<?, Void> setter) implements InternalInjectionPoint {

	public FieldInjectionPoint {
		qualifierAnnotations = Set.copyOf(qualifierAnnotations);
	}

	@Override
	public Bean<?> getBean() {
		return managedBeanReference.get();
	}

	@Override
	public boolean isDelegate() {
		warnNotYetImplemented();
		return false;
	}

	@Override
	public boolean isTransient() {
		warnNotYetImplemented();
		return false;
	}
}
