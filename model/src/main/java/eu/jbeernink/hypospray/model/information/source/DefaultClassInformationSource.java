package eu.jbeernink.hypospray.model.information.source;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.DeploymentException;

import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

/// A [ClassInformationSource] that returns a new [ReflectiveClassInformation].
///
/// @implNote This class has a low priority so it can be overridden by other implementations.
@Priority(0)
public final class DefaultClassInformationSource implements ClassInformationSource {

	@Override
	public ClassInformation<?> getClassInformation(String className) {
		try {
			return getClassInformation(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("No class with name %s can be found.".formatted(className), e);
		}
	}

	@Override
	public <T> ClassInformation<T> getClassInformation(Class<T> clazz) {
		return new ReflectiveClassInformation<>(clazz);
	}
}
