package eu.jbeernink.hypospray.core.inject;

import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.util.TypeLiteral;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextOwner;
import eu.jbeernink.hypospray.core.inject.spi.LookupInjectionPoint;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.util.value.LazyValue;

public final class BeanInstance<T> implements Instance<T> {

	private final BeanContainer container;
	private final CreationalContextOwner creationalContextOwner;
	private final Type type;
	private final Set<Annotation> qualifiers;

	private final Map<T, BeanHandle> createdHandles = synchronizedMap(new IdentityHashMap<>());

	public BeanInstance(BeanContainer container, CreationalContextOwner creationalContextOwner, Type type,
	                    Set<Annotation> qualifiers) {
		this.container = container;
		this.creationalContextOwner = creationalContextOwner;
		this.type = type;
		this.qualifiers = Set.copyOf(qualifiers);
	}


	public <U extends T> BeanInstance<U> select(Class<U> type) {
		return new BeanInstance<>(container, creationalContextOwner, type, qualifiers);
	}

	public <U extends T> BeanInstance<U> select(TypeLiteral<U> typeLiteral) {
		return new BeanInstance<>(container, creationalContextOwner, typeLiteral.getType(), qualifiers);
	}

	@Override
	public Instance<T> select(Annotation... qualifiers) {
		if (qualifiers.length == 0) {
			return this;
		}

		var newQualifiers = new HashSet<>(this.qualifiers);
		newQualifiers.addAll(Arrays.asList(qualifiers));

		return new BeanInstance<>(container, creationalContextOwner, type, newQualifiers);
	}

	@Override
	public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
		return select(subtype).select(qualifiers);
	}

	@Override
	public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		return select(subtype).select(qualifiers);
	}

	@Override
	public boolean isUnsatisfied() {
		return getAllMatchingBeans().isEmpty();
	}

	@Override
	public boolean isAmbiguous() {
		Set<Bean<?>> beans = container.getBeans(type, getQualifiers());

		try {
			var _ = container.resolve(beans);

			return false;
		} catch (AmbiguousResolutionException e) {
			return true;
		}
	}

	@Override
	public void destroy(T instance) {
		getHandle().destroy();
	}

	@Override
	public Handle<T> getHandle() {
		if (isUnsatisfied()) {
			// TODO improve error message
			throw new UnsatisfiedResolutionException(
					"Unable to resolve bean of type %s with annotations %s.".formatted(type.getTypeName(), qualifiers));
		}
		if (isAmbiguous()) {
			// TODO improve error message.
			throw new AmbiguousResolutionException();
		}

		@SuppressWarnings("unchecked") Bean<T> bean =
				(Bean<T>) container.resolve(container.getBeans(type, getQualifiers()));

		return createHandle(bean);
	}

	private Annotation[] getQualifiers() {
		if (qualifiers.isEmpty()) {
			return new Annotation[]{Default.Literal.INSTANCE};
		}

		return qualifiers.toArray(Annotation[]::new);
	}

	@Override
	public Iterable<? extends Handle<T>> handles() {
		return () -> {
			Set<Bean<T>> beans = getAllMatchingBeans();

			return createHandles(beans).iterator();
		};
	}

	@Override
	public Stream<? extends Handle<T>> handlesStream() {
		return createHandles(getAllMatchingBeans());
	}

	@Override
	public T get() {
		return getHandle().get();
	}

	@Override
	public Iterator<T> iterator() {
		return createHandles(getAllMatchingBeans()).map(Handle::get).iterator();
	}

	private Handle<T> createHandle(Bean<T> bean) {
		Set<AnnotationInformation> qualifierAnnotations =
				qualifiers.stream().map(ReflectiveAnnotationInformation::new).collect(toUnmodifiableSet());
		return new BeanHandle(bean, type, creationalContextOwner.newCreationalContext(bean),
				new LookupInjectionPoint(TypeFactory.getInstance().fromJavaType(type), qualifierAnnotations));
	}

	private Stream<Handle<T>> createHandles(Set<Bean<T>> beans) {
		return beans.stream().map(this::createHandle);
	}

	private Set<Bean<T>> getAllMatchingBeans() {
		return container.getBeans(type, getQualifiers()).stream().map(bean -> {
			@SuppressWarnings("unchecked") var typedBean = (Bean<T>) bean;

			return typedBean;
		}).collect(toUnmodifiableSet());
	}

	private final class BeanHandle implements Handle<T> {
		private final Bean<T> managedBean;
		private final CreationalContext<T> creationalContext;
		private final LookupInjectionPoint injectionPoint;
		private final LazyValue<T> instance;
		private final AtomicBoolean destroyed = new AtomicBoolean(false);

		private BeanHandle(Bean<T> managedBean, Type type, CreationalContext<T> creationalContext,
		                   LookupInjectionPoint injectionPoint) {
			this.managedBean = Objects.requireNonNull(managedBean);
			this.creationalContext = creationalContext;
			this.injectionPoint = injectionPoint;
			this.instance = new LazyValue<>(() -> {
				@SuppressWarnings("unchecked") T value = (T) container.getReference(managedBean, type, creationalContext);

				createdHandles.put(value, BeanHandle.this);

				return value;
			});
		}

		@Override
		public T get() {
			return InjectionPointProducer.withInjectionPoint(injectionPoint, instance::get);
		}

		@Override
		public Bean<T> getBean() {
			return managedBean;
		}

		@Override
		public void destroy() {
			if (instance.isCreated() && !destroyed.getAndSet(true)) {
				createdHandles.remove(instance.get());
				managedBean.destroy(instance.get(), creationalContext);
			}
		}

		@Override
		public void close() {
			destroy();
		}
	}
}
