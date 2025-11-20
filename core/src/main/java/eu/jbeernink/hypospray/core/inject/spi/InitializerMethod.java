package eu.jbeernink.hypospray.core.inject.spi;

import static java.util.Comparator.comparingInt;

import java.util.List;

import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.model.information.MethodInformation;

/// An initialization method in a [jakarta.enterprise.inject.spi.Bean].
///
/// An initialization method is any method that is annotated with [jakarta.inject.Inject] that is used to initialize a
/// bean after the constructor is called. Initialization methods will be called after the constructor, but before any
/// [jakarta.annotation.PostConstruct] method.
public record InitializerMethod(MethodInformation methodInformation, Invoker<?, ?> invoker,
                                List<MethodInjectionPoint> injectionPoints) {

	public InitializerMethod {
		injectionPoints = injectionPoints.stream().sorted(comparingInt(MethodInjectionPoint::index)).toList();
	}
}
