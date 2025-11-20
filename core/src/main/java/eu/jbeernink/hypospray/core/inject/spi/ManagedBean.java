package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.DeploymentException;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.core.annotation.QualifierAnnotation;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// A bean which is managed by the container.
public sealed interface ManagedBean<T> extends ManagedContextual<T>, Bean<T> permits DiscoveredBean {

	@Override
	default Class<?> getBeanClass() {
		return switch (beanClass()) {
			case ReflectiveClassInformation<?>(Class<?> classInstance) -> classInstance;
			case ClassInformation<?> _ -> unimplemented();
		};
	}

	@Override
	default Set<Type> getTypes() {
		return types().stream().map(TypeInstance::asJavaType).collect(toUnmodifiableSet());
	}

	default Set<QualifierAnnotation> getQualifierAnnotations() {
		return qualifiers().stream().map(QualifierAnnotation::of).collect(toUnmodifiableSet());
	}

	@Override
	default Set<Annotation> getQualifiers() {
		return qualifiers().stream().map(annotationInformation -> switch (annotationInformation) {
			case ReflectiveAnnotationInformation<?>(Annotation annotation) -> annotation;
			case SyntheticAnnotationInformation _ -> unimplemented();
		}).collect(toUnmodifiableSet());
	}

	@Override
	default Class<? extends Annotation> getScope() {
		return toClass(scope());
	}

	@Override
	@Nullable
	default String getName() {
		return elName();
	}

	@Override
	default Set<Class<? extends Annotation>> getStereotypes() {
		return stereotypes().stream().map(ManagedBean::toClass).collect(toUnmodifiableSet());
	}

	/// Returns class information about the bean's class.
	ClassInformation<?> beanClass();

	/// Returns the set of bean types of this bean.
	Set<TypeInstance> types();

	/// Returns the set of qualifying annotations for this bean.
	Set<AnnotationInformation> qualifiers();

	/// Returns class information about the scope of this bean.
	ClassInformation<? extends Annotation> scope();

	/// Returns the set of stereotypes defined on this bean.
	Set<ClassInformation<? extends Annotation>> stereotypes();

	String elName();

	private static <T> Class<T> toClass(ClassInformation<T> classInformation) {
		return switch (classInformation) {
			case ReflectiveClassInformation<T>(var clazz) -> clazz;
			case ClassInformation<?> _ -> {
				try {
					@SuppressWarnings("unchecked") var clazz = (Class<T>) Class.forName(classInformation.name());
					yield clazz;
				} catch (ClassNotFoundException e) {
					throw new DeploymentException(String.format("Unable to load class %s.", classInformation.name()), e);
				}
			}
		};
	}
}
