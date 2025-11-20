package eu.jbeernink.hypospray.core.inject.spi;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// A bean which has been registered through the bean discovery process.
@NullMarked
public record DiscoveredBean<T>(ClassInformation<?> beanClass, Set<TypeInstance> types,
                                Set<AnnotationInformation> qualifiers, ClassInformation<? extends Annotation> scope,
                                Set<ClassInformation<? extends Annotation>> stereotypes,
                                InjectionTarget<T> injectionTarget, @Nullable String elName,
                                boolean isAlternative) implements ManagedBean<T> {


	public DiscoveredBean {
		types = Set.copyOf(types);
		qualifiers = Set.copyOf(qualifiers);
		stereotypes = Set.copyOf(stereotypes);
	}

	@Override
	public T create(CreationalContext<T> creationalContext) {
		T instance = injectionTarget.produce(creationalContext);

		injectionTarget.inject(instance, creationalContext);
		injectionTarget.postConstruct(instance);

		return instance;
	}

	@Override
	public void destroy(T instance, CreationalContext<T> creationalContext) {
		if (creationalContext instanceof HyposprayCreationalContext<T> hyposprayCreationalContext) {
			injectionTarget.preDestroy(instance);

			injectionTarget.dispose(instance);

			hyposprayCreationalContext.remove(instance);
		}
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return injectionTarget.getInjectionPoints();
	}

	@Override
	public @Nullable String getName() {
		return elName;
	}

	@Override
	public boolean isAlternative() {
		return isAlternative;
	}

	public static <T> Builder<T> newBuilder(Class<?> beanClass) {
		return new Builder<>(ClassInformationSource.getInstance().getClassInformation(requireNonNull(beanClass)));
	}

	public static <T> Builder<T> newBuilder(ClassInformation<?> beanClass) {
		return new Builder<>(requireNonNull(beanClass));
	}

	public static final class Builder<T> {


		private final ClassInformation<?> beanClass;
		private final Set<TypeInstance> types = new HashSet<>();
		private final Set<AnnotationInformation> qualifiers = new HashSet<>();
		private ClassInformation<? extends Annotation> scope = ClassInformationSource.getInstance()
		                                                                             .getClassInformation(Dependent.class);
		private final Set<ClassInformation<? extends Annotation>> stereoTypes = new HashSet<>();
		private @Nullable InjectionTarget<T> injectionTarget;
		private @Nullable String elName;
		private boolean isAlternative = false;

		private Builder(ClassInformation<?> beanClass) {
			this.beanClass = requireNonNull(beanClass);
		}

		public Builder<T> addType(Type type) {
			types.add(TypeFactory.getInstance().fromJavaType(type));

			return this;
		}

		public Builder<T> addType(TypeInstance typeInstance) {
			types.add(typeInstance);

			return this;
		}

		public Builder<T> addAllTypes(Collection<Type> types) {
			types.forEach(this::addType);

			return this;
		}

		public Builder<T> addAllTypeInstances(Collection<TypeInstance> types) {
			types.forEach(this::addType);

			return this;
		}


		public Builder<T> addQualifier(AnnotationInformation annotationInformation) {
			qualifiers.add(annotationInformation);

			return this;
		}

		public Builder<T> addAllQualifiers(Collection<AnnotationInformation> qualifiers) {
			this.qualifiers.addAll(qualifiers);

			return this;
		}

		public Builder<T> addQualifier(Annotation qualifier) {
			return addQualifier(new ReflectiveAnnotationInformation<>(qualifier));
		}

		@Deprecated
		public Builder<T> deprecatedAddAllQualifiers(Collection<Annotation> qualifiers) {
			qualifiers.forEach(this::addQualifier);

			return this;
		}

		public Builder<T> setScope(ClassInformation<? extends Annotation> scope) {
			this.scope = scope;

			return this;
		}

		public Builder<T> setScope(Class<? extends Annotation> scope) {
			return setScope(ClassInformationSource.getInstance().getClassInformation(scope));
		}

		public Builder<T> setInjectionTarget(InjectionTarget<T> injectionTarget) {
			this.injectionTarget = injectionTarget;
			return this;
		}

		public Builder<T> setElName(String elName) {
			this.elName = elName;
			return this;
		}

		public Builder<T> setAlternative(boolean alternative) {
			isAlternative = alternative;
			return this;
		}

		public DiscoveredBean<T> build() {
			return new DiscoveredBean<>(beanClass, types, qualifiers, scope, stereoTypes,
					requireNonNull(injectionTarget, () -> "No injection target specified for managed bean: " + beanClass.name()),
					elName, isAlternative);
		}
	}
}
