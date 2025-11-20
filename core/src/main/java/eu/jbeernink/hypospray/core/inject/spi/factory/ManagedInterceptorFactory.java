package eu.jbeernink.hypospray.core.inject.spi.factory;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.invoke.Invoker;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.MethodConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedInterceptor;
import eu.jbeernink.hypospray.core.inject.spi.producer.ClassBeanProducer;
import eu.jbeernink.hypospray.core.invoke.NoopInvoker;
import eu.jbeernink.hypospray.core.invoke.ReflectiveMethodInvoker;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.reflection.ParameterizedTypeImpl;

@Priority(1)
public class ManagedInterceptorFactory implements ContextualFactory<ManagedInterceptor<?>> {

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	@Override
	public boolean isCandidate(ClassConfiguration<?> classConfiguration) {
		return classConfiguration.hasAnnotation(Interceptor.class);
	}

	@Override
	public Set<ManagedInterceptor<?>> processCandidate(ClassConfiguration<?> classConfiguration,
	                                                   Supplier<InjectableBeanContainer> beanContainerSupplier) {
		if (!isCandidate(classConfiguration)) {
			throw new IllegalArgumentException("Class is not a valid interceptor candidate.");
		}

		return createManagedInterceptor(classConfiguration, beanContainerSupplier.get()).map(Set::<ManagedInterceptor<?>>of)
		                                                                                .orElseGet(Set::of);
	}

	private <T> Optional<ManagedInterceptor<T>> createManagedInterceptor(ClassConfiguration<T> classConfiguration,
	                                                                     BeanContainer beanContainer) {
		return getPriority(classConfiguration).map(
				priority -> new ManagedInterceptor<>(classConfiguration.info(),
						getTypeClosure(classConfiguration.info().classInstance()),
						getInterceptorBindings(classConfiguration, beanContainer),
						getInterceptionTypes(classConfiguration.methodConfigurations()),
						createClassBeanProducer(classConfiguration), priority,
						getAroundConstructMethod(classConfiguration.methodConfigurations()),
						getPostConstructMethod(classConfiguration.methodConfigurations()),
						getAroundInvokeMethod(classConfiguration.methodConfigurations()),
						getAroundTimeoutMethod(classConfiguration.methodConfigurations()),
						getPreDestroyMethod(classConfiguration.methodConfigurations())));
	}

	private static Optional<Integer> getPriority(ClassConfiguration<?> classConfiguration) {
		return classConfiguration.findAnnotation(Priority.class)
		                         .map(annotationInformation -> annotationInformation.value().asInt());
	}

	private <T> Invoker<T, Object> getAroundConstructMethod(List<MethodConfiguration> methods) {
		return findInterceptionMethod(methods, AroundConstruct.class,
				Set.of(typeFactory.ofVoid(), typeFactory.fromJavaType(Object.class))).map(
						                                                                     method -> (Invoker<T, Object>) new ReflectiveMethodInvoker<T, Object>(method.info().methodInstance()))
		                                                                         .orElseGet(NoopInvoker::new);

	}

	private <T> Invoker<T, Object> getPostConstructMethod(List<MethodConfiguration> methods) {
		return findInterceptionMethod(methods, PostConstruct.class,
				Set.of(typeFactory.ofVoid(), typeFactory.of(Object.class))).map(
						                                                           method -> (Invoker<T, Object>) new ReflectiveMethodInvoker<T, Object>(method.info().methodInstance()))
		                                                               .orElseGet(NoopInvoker::new);
	}

	private <T> Invoker<T, Object> getAroundInvokeMethod(List<MethodConfiguration> methods) {
		return findInterceptionMethod(methods, AroundInvoke.class,
				Set.of(typeFactory.ofVoid(), typeFactory.ofObject())).map(
						                                                     method -> (Invoker<T, Object>) new ReflectiveMethodInvoker<T, Object>(method.info().methodInstance()))
		                                                         .orElseGet(NoopInvoker::new);
	}

	private <T> Invoker<T, Object> getAroundTimeoutMethod(List<MethodConfiguration> methods) {
		return findInterceptionMethod(methods, AroundTimeout.class, Set.of(typeFactory.ofObject())).map(
				                                                                                           method -> (Invoker<T, Object>) new ReflectiveMethodInvoker<T, Object>(method.info().methodInstance()))
		                                                                                           .orElseGet(
				                                                                                           NoopInvoker::new);
	}

	private <T> Invoker<T, Object> getPreDestroyMethod(List<MethodConfiguration> methods) {
		return findInterceptionMethod(methods, PreDestroy.class, Set.of(typeFactory.ofObject())).map(
				                                                                                        method -> (Invoker<T, Object>) new ReflectiveMethodInvoker<T, Object>(method.info().methodInstance()))
		                                                                                        .orElseGet(
				                                                                                        NoopInvoker::new);
	}

	private <T> Optional<MethodConfiguration> findInterceptionMethod(List<MethodConfiguration> methods,
	                                                                 Class<? extends Annotation> interceptionAnnotation,
	                                                                 Set<TypeInstance> expectedReturnType) {
		return methods.stream()
		              .filter(method -> method.hasAnnotation(interceptionAnnotation))
		              .filter(method -> isInterceptorMethod(method, expectedReturnType))
		              .findFirst();
	}

	private <T> boolean isInterceptorMethod(MethodConfiguration methodConfiguration,
	                                        Set<TypeInstance> expectedReturnTypes) {
		TypeInstance returnType = methodConfiguration.info().returnType();

		return expectedReturnTypes.contains(returnType) && methodConfiguration.parameters().size() == 1 &&
		       methodConfiguration.parameterConfigurations()
		                          .getFirst()
		                          .info()
		                          .type()
		                          .equals(TypeFactory.getInstance().fromJavaType(InvocationContext.class));

	}

	private <T> ClassBeanProducer<T> createClassBeanProducer(ClassConfiguration<T> classConfiguration) {
		return null;
	}

	private <T> Set<InterceptionType> getInterceptionTypes(List<MethodConfiguration> methods) {
		return Set.of();
	}

	private static Set<Annotation> getInterceptorBindings(ClassConfiguration<?> classConfiguration,
	                                                      BeanContainer beanContainer) {
		return classConfiguration.annotations()
		                         .stream()
		                         .filter(annotationInformation -> beanContainer.isInterceptorBinding(
				                         annotationInformation.declaration().classInstance()))
		                         .map(AnnotationInformation::annotationInstance)
		                         .collect(toUnmodifiableSet());
	}

	private static Set<Type> getTypeClosure(Type type) {
		var typeClosure = new HashSet<Type>();
		var typeVariableMapping = new HashMap<TypeVariable<?>, Type>();

		var typesToExplore = new ArrayList<Type>();
		typesToExplore.add(type);

		while (!typesToExplore.isEmpty()) {
			Type newType = typesToExplore.removeFirst();
			switch (newType) {
				case Class<?> clazz -> {
					for (TypeVariable<?> typeVariable : clazz.getTypeParameters()) {
						typeVariableMapping.putIfAbsent(typeVariable, typeVariable);
					}
					typeClosure.add(clazz);

					typesToExplore.addAll(getParentTypes(clazz));
				}
				case ParameterizedType parameterizedType -> {
					if (parameterizedType.getRawType() instanceof Class<?> clazz) {
						for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
							typeVariableMapping.putIfAbsent(clazz.getTypeParameters()[i],
									parameterizedType.getActualTypeArguments()[i]);
						}


						List<Type> resolvedTypes = Arrays.stream(parameterizedType.getActualTypeArguments()).map(typeArgument -> {
							if (typeArgument instanceof TypeVariable<?> typeVariable) {
								return typeVariableMapping.get(typeVariable);
							}

							return typeArgument;
						}).toList();

						typeClosure.add(new ParameterizedTypeImpl(clazz, null, resolvedTypes));

						typesToExplore.addAll(getParentTypes(clazz));
					}
				}
				default -> throw new IllegalArgumentException("Unsupported type: " + type);
			}
		}

		return typeClosure;
	}

	private static Set<Type> getParentTypes(Class<?> clazz) {
		Set<Type> parentTypes = new HashSet<>();
		if (clazz.getGenericSuperclass() != null) {
			parentTypes.add(clazz.getGenericSuperclass());
		}

		parentTypes.addAll(Arrays.asList(clazz.getGenericInterfaces()));
		return parentTypes;
	}
}
