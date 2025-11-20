package eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter;

import static eu.jbeernink.hypospray.core.todo.Todo.warnTodo;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.Type;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.InterceptorInformation;
import eu.jbeernink.hypospray.core.inject.spi.ManagedInterceptor;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;

/// Converter from [Interceptor] to [InterceptorInformation].
public class InterceptorInfoConverter implements Function<Interceptor<?>, InterceptorInformation> {

	@Override
	public InterceptorInformation apply(Interceptor<?> interceptor) {
		// TODO support extracting class information directly from ManagedInterceptor.
		return new InterceptorInformation(ClassInformationSource.getInstance().getClassInformation(interceptor.getBeanClass()),
				getInterceptorBindings(interceptor), getInterceptionTypes(interceptor), getTypes(interceptor), Set.of(),
				getInjectionPointInfo(interceptor), getPriority(interceptor));
	}

	private Set<InjectionPointInfo> getInjectionPointInfo(Interceptor<?> interceptor) {
		warnTodo("Extract injection points.");

		return Set.of();
	}

	private Set<AnnotationInfo> getInterceptorBindings(Interceptor<?> interceptor) {
		return interceptor.getInterceptorBindings().stream().map(this::getAnnotationInfo).collect(toUnmodifiableSet());
	}

	private AnnotationInformation getAnnotationInfo(Annotation annotation) {
		// TODO support extracting annotation information directly from ManagedInterceptor.

		warnTodo("Implement extracting parameters.");
		return new ReflectiveAnnotationInformation<>(annotation);
	}

	private static Set<Type> getTypes(Interceptor<?> interceptor) {
		TypeFactory typeFactory = TypeFactory.getInstance();

		return interceptor.getTypes().stream().map(typeFactory::fromJavaType).collect(toUnmodifiableSet());
	}

	private Set<InterceptionType> getInterceptionTypes(Interceptor<?> interceptor) {
		return switch (interceptor) {
			case ManagedInterceptor(
					_, _, _, Set<InterceptionType> interceptionTypes, _, _, _, _, _, _, _
			) -> interceptionTypes;
			case Interceptor<?> i ->
					Arrays.stream(InterceptionType.values()).filter(i::intercepts).collect(toUnmodifiableSet());
		};
	}

	private static Integer getPriority(Interceptor<?> interceptor) {
		return switch (interceptor) {
			case Prioritized prioritized -> prioritized.getPriority();
			case Interceptor<?> _ -> null;
		};
	}
}
