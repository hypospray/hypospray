package eu.jbeernink.hypospray.core.registry;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.Prioritized;

import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.core.ScopeInstance;
import eu.jbeernink.hypospray.core.annotation.Wildcard;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.interceptor.InterceptorContext;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Registry for tracking discovered beans and scopes within the container.
@NullMarked
public class ContainerRegistry {

	private static final System.Logger logger = System.getLogger(ContainerRegistry.class.getName());

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Map<Class<? extends Annotation>, ScopeInstance> scopes = new HashMap<>();

	// TODO figure out more efficient way to lookup beans and interceptors.
	private final Set<Bean<?>> beans = new HashSet<>();

	private final Map<Annotation, Interceptor<?>> interceptors = new HashMap<>();

	private final Map<Object, InterceptorContext> interceptorContexts = new HashMap<>();

	public void registerBean(Bean<?> managedBean) {
		withWriteLock(() -> {
			logger.log(DEBUG, "Registering bean: " + managedBean);

			beans.add(managedBean);
		});
	}

	public Set<Bean<?>> resolveBeans(TypeInstance typeInstance, Set<Annotation> qualifiers) {
		return withReadLock(() -> beans.stream().filter(bean -> matchesQualifiers(bean, qualifiers))).filter(
				bean -> matchesType(bean, typeInstance)).collect(toUnmodifiableSet());
	}

	public void registerInterceptor(Interceptor<?> interceptor) {
		// TODO throw exception if a binding already exists.
		withWriteLock(
				() -> interceptor.getInterceptorBindings().forEach(binding -> interceptors.put(binding, interceptor)));
	}

	public List<Interceptor<?>> resolveInterceptors(Set<Annotation> interceptorBindings) {
		return withReadLock(() -> interceptorBindings.stream()
		                                             .filter(interceptors::containsKey)
		                                             .<Interceptor<?>>map(interceptors::get)
		                                             .sorted(comparingInt(this::getInterceptorPriority))
		                                             .toList());
	}

	private int getInterceptorPriority(Interceptor<?> interceptor) {
		return switch (interceptor) {
			case Prioritized prioritized -> prioritized.getPriority();
			case Interceptor<?> _ -> throw new IllegalStateException("Interceptor is not enabled: " + interceptor);
		};
	}

	public void registerScope(Class<? extends Annotation> scopeAnnotation, Context context, boolean isNormalScope) {
		withWriteLock(() -> {
			if (scopes.containsKey(scopeAnnotation)) {
				throw new IllegalStateException(
						"A scope has already been registered for the " + scopeAnnotation + " annotation.");
			}

			scopes.compute(scopeAnnotation, (_, v) -> {
				if (v == null) {
					return new ScopeInstance(List.of(context), scopeAnnotation, isNormalScope);
				}

				return v.appendContextInstance(context);
			});
		});
	}

	public Optional<ScopeInstance> getContext(Class<? extends Annotation> scopeAnnotation) {
		return withReadLock(() -> {
			if (scopes.containsKey(scopeAnnotation)) {
				return Optional.of(scopes.get(scopeAnnotation));
			}

			return Optional.empty();
		});
	}

	public Optional<InterceptorContext> getInterceptorContext(Object instance) {
		return withReadLock(() -> {
			if (interceptorContexts.containsKey(instance)) {
				return Optional.of(interceptorContexts.get(instance));
			}

			return Optional.empty();
		});
	}

	public void registerInterceptorContext(Object instance, InterceptorContext interceptorContext) {
		withWriteLock(() -> interceptorContexts.put(instance, interceptorContext));
	}

	public void clearInterceptorContext(Object instance) {
		withWriteLock(() -> interceptorContexts.remove(instance));
	}

	private boolean matchesType(Bean<?> bean, TypeInstance typeInstance) {
		return switch (bean) {
			case DiscoveredBean<?>(_, Set<TypeInstance> types, _, _, _, _, _, _) ->
					types.stream().anyMatch(typeInstance::isAssignableFrom);
			case Bean<?> _ ->
					bean.getTypes().stream().map(typeFactory::fromJavaType).anyMatch(typeInstance::isAssignableFrom);
		};
	}

	private boolean matchesQualifiers(Bean<?> bean, Set<Annotation> qualifiers) {
		// TODO check member equality.
		if (bean.getQualifiers().contains(Wildcard.Literal.INSTANCE)) {
			return true;
		}

		return bean.getQualifiers().containsAll(qualifiers);
	}

	private <T> T withReadLock(Supplier<T> callable) {
		lock.readLock().lock();
		try {
			return callable.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	private void withWriteLock(Runnable runnable) {
		lock.writeLock().lock();
		try {
			runnable.run();
		} finally {
			lock.writeLock().unlock();
		}
	}

}
