package eu.jbeernink.hypospray.core.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProducerFactory;
import jakarta.inject.Inject;

/// [BeanManager] that only supports CDI Lite operations.
///
/// This [BeanManager] wraps the [InjectableBeanContainer] and throws an [UnsupportedOperationException]
/// for any operation it doesn't support.
public class LiteBeanManager implements BeanManager {
	private final InjectableBeanContainer beanContainer;

	@Inject
	public LiteBeanManager(InjectableBeanContainer beanContainer) {
		this.beanContainer = beanContainer;
	}

	@Override
	public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
		return beanContainer.getReference(bean, beanType, ctx);
	}

	@Override
	public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
		return beanContainer.createCreationalContext(contextual);
	}

	@Override
	public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
		return beanContainer.getBeans(beanType, qualifiers);
	}

	@Override
	public Set<Bean<?>> getBeans(String name) {
		return beanContainer.getBeans(name);
	}

	@Override
	public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
		return beanContainer.resolve(beans);
	}

	@Override
	public Bean<?> getPassivationCapableBean(String id) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public void validate(InjectionPoint injectionPoint) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
		return beanContainer.resolveObserverMethods(event, qualifiers);
	}

	@Override
	public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public int getQualifierHashCode(Annotation qualifier) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	@Deprecated(forRemoval = true)
	public ELResolver getELResolver() {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	@Deprecated(forRemoval = true)
	public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass,
	                              InjectionTargetFactory<T> injectionTargetFactory) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass,
	                                 ProducerFactory<X> producerFactory) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public InjectionPoint createInjectionPoint(AnnotatedField<?> field) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T extends Extension> T getExtension(Class<T> extensionClass) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> ctx, Class<T> clazz) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
		throw new UnsupportedOperationException("Not supported in CDI Lite");
	}

	@Override
	public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
		return beanContainer.resolveInterceptors(type, interceptorBindings);
	}

	@Override
	public boolean isScope(Class<? extends Annotation> annotationType) {
		return beanContainer.isScope(annotationType);
	}

	@Override
	public boolean isNormalScope(Class<? extends Annotation> annotationType) {
		return beanContainer.isNormalScope(annotationType);
	}

	@Override
	public boolean isQualifier(Class<? extends Annotation> annotationType) {
		return beanContainer.isQualifier(annotationType);
	}

	@Override
	public boolean isStereotype(Class<? extends Annotation> annotationType) {
		return beanContainer.isStereotype(annotationType);
	}

	@Override
	public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
		return beanContainer.isInterceptorBinding(annotationType);
	}

	@Override
	public Context getContext(Class<? extends Annotation> scopeType) {
		return beanContainer.getContext(scopeType);
	}

	@Override
	public Collection<Context> getContexts(Class<? extends Annotation> scopeType) {
		return beanContainer.getContexts(scopeType);
	}

	@Override
	public Event<Object> getEvent() {
		return beanContainer.getEvent();
	}

	@Override
	public Instance<Object> createInstance() {
		return beanContainer.createInstance();
	}

	@Override
	public boolean isMatchingBean(Set<Type> beanTypes, Set<Annotation> beanQualifiers, Type requiredType,
	                              Set<Annotation> requiredQualifiers) {
		return beanContainer.isMatchingBean(beanTypes, beanQualifiers, requiredType, requiredQualifiers);
	}

	@Override
	public boolean isMatchingEvent(Type specifiedType, Set<Annotation> specifiedQualifiers, Type observedEventType,
	                               Set<Annotation> observedEventQualifiers) {
		return beanContainer.isMatchingEvent(specifiedType, specifiedQualifiers, observedEventType,
				observedEventQualifiers);
	}
}
