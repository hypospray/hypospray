package eu.jbeernink.hypospray.model.information.source;

import eu.jbeernink.hypospray.model.information.ClassInformation;

/// A pluggable source of obtaining [ClassInformation] instances.
public interface ClassInformationSource {

	/// Get the highest priority instance of [ClassInformationSource].
	static ClassInformationSource getInstance() {
		return ClassInformationSourceManager.getInstance();
	}

	/// Obtain a [ClassInformation] instance for the class with the given name.
	///
	/// @param className the name of the class,
	/// @return the [ClassInformation] for the given class name.
	/// @throws jakarta.enterprise.inject.spi.DeploymentException if no class can be found with the given name.
	ClassInformation<?> getClassInformation(String className);

	/// Obtain a [ClassInformation] instance for the given [Class].
	///
	/// @param clazz the class to obtain [ClassInformation] for.
	/// @param <T>   the type of the class modeled by `clazz`.
	/// @return the [ClassInformation] for the given [Class].
	<T> ClassInformation<T> getClassInformation(Class<T> clazz);
}
