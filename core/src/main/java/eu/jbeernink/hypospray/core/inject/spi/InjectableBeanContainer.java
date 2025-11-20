package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;
import static eu.jbeernink.hypospray.core.todo.Todo.warnNotYetImplemented;
import static eu.jbeernink.hypospray.core.todo.Todo.warnTodo;
import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static java.lang.Integer.MIN_VALUE;
import static java.util.Comparator.comparingInt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.interceptor.InterceptorBinding;

import eu.jbeernink.hypospray.core.ProxyContextProducer;
import eu.jbeernink.hypospray.core.ScopeInstance;
import eu.jbeernink.hypospray.core.annotation.ClientProxy;
import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.event.DummyEventInstance;
import eu.jbeernink.hypospray.core.inject.BeanInstance;
import eu.jbeernink.hypospray.core.interceptor.InterceptorContext;
import eu.jbeernink.hypospray.core.internal.Internal;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;

/// Injectable [BeanContainer] implementation.
public final class InjectableBeanContainer implements BeanContainer {

	/// Comparator that returns the highest priority enabled alternative first.
	private static final Comparator<Object> HIGHEST_PRIORITY_FIRST_COMPARATOR =
			comparingInt(bean -> bean instanceof Prioritized prioritized ? prioritized.getPriority() : MIN_VALUE).reversed();
	private final CreationalContextManager creationalContextManager;
	private final ContainerRegistry containerRegistry;

	@Inject
	public InjectableBeanContainer(@Internal CreationalContextManager creationalContextManager,
	                               @Internal ContainerRegistry containerRegistry) {
		this.creationalContextManager = creationalContextManager;
		this.containerRegistry = containerRegistry;
	}

	@Override
	public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
		warnTodo("Check if instances of bean can be assigned to beanType.");
		warnTodo("Set fall-back injection point if no injection point is active.");

		if (ctx instanceof HyposprayCreationalContext<?> creationalContext) {
			if (shouldBeProxied(bean)) {
				return getProxyInstance(bean, beanType, creationalContext);
			}

			return createPseudoScopedInstance(bean, creationalContext);
		}

		throw new IllegalArgumentException("Unsupported creational context type: " + ctx);
	}

	private Object getProxyInstance(Bean<?> bean, Type beanType, HyposprayCreationalContext<?> ctx) {
		Class<?> beanClass = bean.getBeanClass();
		// TODO use beanType instead of beanClass.
		Bean<?> clientProxyBean = resolve(
				containerRegistry.resolveBeans(TypeFactory.getInstance().of(beanClass), Set.of(ClientProxy.Literal.INSTANCE)));

		if (!isNormalScope(bean.getScope())) {
			Object instance = createPseudoScopedInstance(bean, ctx);
			InterceptorContext interceptorContext = containerRegistry.getInterceptorContext(instance).orElseThrow();

			return ProxyContextProducer.withNonNormalScopedProxyContext(interceptorContext, bean, instance,
					() -> createPseudoScopedInstance(clientProxyBean, ctx));
		}

		return ProxyContextProducer.withProxyContext(new BeanLookupInvokerFactory(bean, this),
				() -> createPseudoScopedInstance(clientProxyBean, ctx));
	}

	private <T> T createPseudoScopedInstance(Contextual<T> contextual, HyposprayCreationalContext<?> creationalContext) {
		return createInstance(contextual, creationalContext.createDependentCreationalContext(contextual));
	}

	private <T> T createInstance(Contextual<T> contextual, CreationalContext<T> creationalContext) {
		if (contextual instanceof Bean<T> bean) {
			Context context = getContext(bean.getScope());

			T instance = context.get(bean);
			if (instance == null) {
				instance = context.get(bean, creationalContext);
			}

			return instance;
		}

		return contextual.create(creationalContext);
	}

	private boolean shouldBeProxied(Bean<?> bean) {
		return isNormalScope(bean.getScope()) || isIntercepted(bean);
	}

	private boolean isIntercepted(Bean<?> bean) {
		warnNotYetImplemented();
		return false;
	}

	@Override
	public <T> HyposprayCreationalContext<T> createCreationalContext(Contextual<T> contextual) {
		return creationalContextManager.newCreationalContext(contextual);
	}

	@Override
	public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
		return containerRegistry.resolveBeans(TypeFactory.getInstance().fromJavaType(beanType), Set.of(qualifiers));
	}

	@Override
	public Set<Bean<?>> getBeans(String name) {
		return unimplemented();
	}

	@Override
	public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
		if (beans == null || beans.isEmpty()) {
			return null;
		}

		return resolveEnabledAlternative(beans).orElseGet(() -> beans.stream()
		                                                             .filter(bean -> !bean.isAlternative())
		                                                             .collect(findOnly(
				                                                             () -> new AmbiguousResolutionException(
						                                                             "Unable to resolve bean, multiple matching beans found.")))
		                                                             .orElseThrow(AmbiguousResolutionException::new));
	}

	private static <X> Optional<Bean<? extends X>> resolveEnabledAlternative(Set<Bean<? extends X>> beans) {
		return beans.stream()
		            .filter(bean -> bean.isAlternative() && bean instanceof Prioritized)
		            .min(HIGHEST_PRIORITY_FIRST_COMPARATOR);
	}

	@Override
	public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
		return unimplemented();
	}

	@Override
	public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
		return containerRegistry.resolveInterceptors(Set.of(interceptorBindings))
		                        .stream()
		                        .filter(interceptor -> interceptor.intercepts(type))
		                        .toList();
	}

	/// Test the given [ClassInformation] to see if it represents a scope annotation.
	///
	/// @param annotationType the annotation to check.
	/// @return true if the given annotation represents a scope and false otherwise.
	public boolean isScope(ClassInformation<? extends Annotation> annotationType) {
		if (annotationType.hasAnnotation(Scope.class) || annotationType.hasAnnotation(NormalScope.class)) {
			return true;
		}

		return containerRegistry.getContext(annotationType.classInstance()).isPresent();
	}

	@Override
	public boolean isScope(Class<? extends Annotation> annotationType) {
		if (annotationType.getAnnotation(Scope.class) != null || annotationType.getAnnotation(NormalScope.class) != null) {
			return true;
		}

		return containerRegistry.getContext(annotationType).isPresent();
	}

	@Override
	public boolean isNormalScope(Class<? extends Annotation> annotationType) {
		if (annotationType.getAnnotation(NormalScope.class) != null) {
			return true;
		}

		return containerRegistry.getContext(annotationType).map(ScopeInstance::isNormalScope).orElse(false);
	}

	/// Tests if the given [ClassInformation] to determine if it is a qualifier annotation.
	///
	/// @param annotationType the annotation type to test.
	/// @return true, if the type is a qualifier annotation and false otherwise.
	public boolean isQualifier(ClassInformation<? extends Annotation> annotationType) {
		// TODO support programmatic qualifiers.
		return annotationType.hasAnnotation(Qualifier.class);
	}

	@Override
	public boolean isQualifier(Class<? extends Annotation> annotationType) {
		return isQualifier(ClassInformationSource.getInstance().getClassInformation(annotationType));
	}

	@Override
	public boolean isStereotype(Class<? extends Annotation> annotationType) {
		// TODO support programmatic stereotypes.
		return annotationType.getAnnotation(Stereotype.class) != null;
	}

	@Override
	public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
		return annotationType.getAnnotation(InterceptorBinding.class) != null;
	}

	@Override
	public Context getContext(Class<? extends Annotation> scopeType) {
		return containerRegistry.getContext(scopeType)
		                        .flatMap(ScopeInstance::activeContext)
		                        .orElseThrow(() -> new ContextNotActiveException(
				                        "No context active for scope " + scopeType.getName()));

	}

	@Override
	public Collection<Context> getContexts(Class<? extends Annotation> scopeType) {
		return containerRegistry.getContext(scopeType)
		                        .stream()
		                        .flatMap(scopeInstance -> scopeInstance.contexts().stream())
		                        .toList();
	}

	@Override
	public Event<Object> getEvent() {
		Instance<Event<Object>> eventInstance = createInstance().select(new TypeLiteral<>() {});
		if (eventInstance.isResolvable()) {
			return eventInstance.get();
		}

		return new DummyEventInstance<>();
	}

	@Override
	public Instance<Object> createInstance() {
		return new BeanInstance<>(this, creationalContextManager, Object.class, Set.of());
	}

	@Override
	public boolean isMatchingBean(Set<Type> beanTypes, Set<Annotation> beanQualifiers, Type requiredType,
	                              Set<Annotation> requiredQualifiers) {
		return unimplemented();
	}

	@Override
	public boolean isMatchingEvent(Type specifiedType, Set<Annotation> specifiedQualifiers, Type observedEventType,
	                               Set<Annotation> observedEventQualifiers) {
		return unimplemented();
	}

	private record BeanLookupInvokerFactory(Bean<?> bean, InjectableBeanContainer container) implements
			Function<String, Invoker<Void, Object>> {

		@Override
		public Invoker<Void, Object> apply(String methodIdentifier) {
			Context context = container.getContext(bean.getScope());
			Object instance = context.get(bean);
			if (instance == null) {
				instance =
						container.createPseudoScopedInstance(bean, container.creationalContextManager.newCreationalContext(bean));
			}

			Optional<InterceptorContext> optionalInterceptorContext =
					container.containerRegistry.getInterceptorContext(instance);

			if (optionalInterceptorContext.isPresent()) {
				return optionalInterceptorContext.get().apply(methodIdentifier);
			}

			return lookupInvoker(methodIdentifier, instance);
		}

		private <T> Invoker<Void, Object> lookupInvoker(String methodIdentifier, T instance) {
			@SuppressWarnings("unchecked") Invoker<T, Object> invoker =
					(Invoker<T, Object>) InvokerFactoryManager.getInstance()
					                                          .getInvokerFactory(bean.getBeanClass().getName())
					                                          .create(methodIdentifier);
			return (_, parameters) -> invoker.invoke(instance, parameters);
		}
	}
}
