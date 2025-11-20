package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.warnNotYetImplemented;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Injection point defined by a method parameter.
///
/// @param type                 the type of the injection point.
/// @param qualifierAnnotations the qualifier annotations defined on the injection point.
/// @param parameter            the parameter defining this injection point.
/// @param beanReference        a late-binding reference to the bean defining this injection point, may be null if the injection point is not defined by a bean.
@NullMarked
public record MethodInjectionPoint(TypeInstance type, Set<AnnotationInformation> qualifierAnnotations,
                                   ParameterInformation parameter,
								   int index,
                                   @Nullable LateReference<Bean<?>> beanReference) implements
		InternalInjectionPoint {

	@Override
	public @Nullable Bean<?> getBean() {
		return beanReference == null ? null : beanReference.get();
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
