package eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter;

import static eu.jbeernink.hypospray.core.todo.Todo.warnNotYetImplemented;

import java.util.function.Function;

import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.inject.spi.InjectionPoint;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer.InjectionPointInformation;

public class InjectionPointInfoConverter implements Function<InjectionPoint, InjectionPointInfo> {

	@Override
	public InjectionPointInformation apply(InjectionPoint injectionPoint) {
		warnNotYetImplemented();
		return null;
	}
}
