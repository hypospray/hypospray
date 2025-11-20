package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;

import java.util.Collection;
import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.DisposerInfo;
import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.inject.build.compatible.spi.InterceptorInfo;
import jakarta.enterprise.inject.build.compatible.spi.ScopeInfo;
import jakarta.enterprise.inject.build.compatible.spi.StereotypeInfo;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.types.Type;

public record InterceptorInformation(ClassInfo declaringClass, Set<AnnotationInfo> interceptorBindings,
                                     Set<InterceptionType> interceptionTypes, Set<Type> types,
                                     Set<AnnotationInfo> qualifiers, Set<InjectionPointInfo> injectionPoints,
                                     Integer priority) implements InterceptorInfo {

	@Override
	public boolean intercepts(InterceptionType interceptionType) {
		return interceptionTypes.contains(interceptionType);
	}

	@Override
	public ScopeInfo scope() {
		return unimplemented();
	}

	@Override
	public boolean isClassBean() {
		return true;
	}

	@Override
	public boolean isProducerMethod() {
		return false;
	}

	@Override
	public boolean isProducerField() {
		return false;
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	@Override
	public MethodInfo producerMethod() {
		return null;
	}

	@Override
	public FieldInfo producerField() {
		return null;
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public DisposerInfo disposer() {
		return null;
	}

	@Override
	public Collection<StereotypeInfo> stereotypes() {
		return unimplemented();
	}
}
