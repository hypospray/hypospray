package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.enterprise.invoke.Invoker;
import jakarta.interceptor.InvocationContext;

import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

public record ManagedInterceptor<T>(ClassInformation<T> classInformation, Set<Type> types,
                                    Set<Annotation> interceptorBindings, Set<InterceptionType> interceptionTypes,
                                    ClassBeanProducer<T> classBeanProducer, int priority,
                                    Invoker<T, Object> aroundConstructMethod, Invoker<T, Object> postConstructMethod,
                                    Invoker<T, Object> aroundInvokeMethod, Invoker<T, Object> aroundTimeOutMethod,
                                    Invoker<T, Object> preDestroyMethod) implements Interceptor<T>,
		ManagedContextual<T>, Prioritized {

	public ManagedInterceptor {
		types = Set.copyOf(types);
		interceptorBindings = Set.copyOf(interceptorBindings);
		interceptionTypes = Set.copyOf(interceptionTypes);
	}

	@Override
	public Set<Annotation> getInterceptorBindings() {
		return interceptorBindings;
	}

	@Override
	public boolean intercepts(InterceptionType type) {
		return interceptionTypes.contains(type);
	}

	@Override
	public Object intercept(InterceptionType type, T instance, InvocationContext ctx) throws Exception {
		return switch (type) {
			case AROUND_INVOKE -> aroundInvokeMethod.invoke(instance, new Object[]{ctx});
			case AROUND_TIMEOUT -> aroundTimeOutMethod.invoke(instance, new Object[]{ctx});
			case AROUND_CONSTRUCT -> aroundConstructMethod.invoke(instance, new Object[]{ctx});
			case POST_CONSTRUCT -> postConstructMethod.invoke(instance, new Object[]{ctx});
			case PRE_DESTROY -> preDestroyMethod.invoke(instance, new Object[]{ctx});
			case PRE_PASSIVATE -> unimplemented();
			case POST_ACTIVATE -> unimplemented();
		};
	}

	@Override
	public Class<?> getBeanClass() {
		return switch (classInformation) {
			case ReflectiveClassInformation<T>(Class<T> classInstance) -> classInstance;
			case ClassInformation<T> _ -> {
				try {
					yield Class.forName(classInformation.name());
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Unable to find class named \"%s\".".formatted(classInformation.name()), e);
				}
			}
		};
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return classBeanProducer.getInjectionPoints();
	}

	@Override
	public T create(CreationalContext<T> creationalContext) {
		T instance = classBeanProducer.produce(creationalContext);

		classBeanProducer.inject(instance, creationalContext);
		classBeanProducer.postConstruct(instance);

		return instance;
	}

	@Override
	public void destroy(T instance, CreationalContext<T> creationalContext) {
		classBeanProducer.preDestroy(instance);

		creationalContext.release();
	}

	@Override
	public Set<Type> getTypes() {
		return types;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Set.of();
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return Dependent.class;
	}

	@Override
	public String getName() {
		// TODO support names?
		return null;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		// TODO support stereo types?
		return null;
	}

	@Override
	public boolean isAlternative() {
		// TODO support @Alternative?
		return false;
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
