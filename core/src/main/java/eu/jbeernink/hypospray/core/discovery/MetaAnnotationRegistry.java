package eu.jbeernink.hypospray.core.discovery;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.inject.Scope;

import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

public class MetaAnnotationRegistry implements MetaAnnotations {

	private final ContainerRegistry containerRegistry;

	public MetaAnnotationRegistry(ContainerRegistry containerRegistry) {
		this.containerRegistry = containerRegistry;
	}

	@Override
	public ClassConfig addQualifier(Class<? extends Annotation> annotation) {
		return unimplemented();
	}

	@Override
	public ClassConfig addInterceptorBinding(Class<? extends Annotation> annotation) {
		return unimplemented();
	}

	@Override
	public ClassConfig addStereotype(Class<? extends Annotation> annotation) {
		return unimplemented();
	}

	@Override
	public void addContext(Class<? extends Annotation> scopeAnnotation, Class<? extends AlterableContext> contextClass) {
		if (scopeAnnotation.getAnnotation(NormalScope.class) != null) {
			addContext(scopeAnnotation, true, contextClass);
		} else if (scopeAnnotation.getAnnotation(Scope.class) != null) {
			addContext(scopeAnnotation, false, contextClass);
		} else {
			throw new IllegalArgumentException(
					"Implicit scope annotations must be annotated with either @NormalScope or @Scope.");
		}
	}

	@Override
	public void addContext(Class<? extends Annotation> scopeAnnotation, boolean isNormal,
	                       Class<? extends AlterableContext> contextClass) {
		try {
			AlterableContext alterableContext = contextClass.getConstructor().newInstance();

			if (!alterableContext.getScope().equals(scopeAnnotation)) {
				throw new IllegalArgumentException(
						String.format("Attempt to register scope annotation @%s for context which expects to be bound to @%s.",
								scopeAnnotation.getSimpleName(), alterableContext.getScope().getSimpleName()));
			}

			containerRegistry.registerScope(scopeAnnotation, alterableContext, isNormal);
		} catch (InvocationTargetException e) {
			// TODO improve error messaging.
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
