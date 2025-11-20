package eu.jbeernink.hypospray.model.information.source;

import static java.lang.Integer.MIN_VALUE;
import static java.util.Comparator.comparingInt;

import java.util.ServiceLoader;

import jakarta.annotation.Priority;

/// Manager class for ensuring the [eu.jbeernink.hypospray.model.information.source.ClassInformationSource] is always a singleton.
final class ClassInformationSourceManager {

	private static final ClassInformationSource INSTANCE;

	static {
		synchronized (ClassInformationSourceManager.class) {
			INSTANCE = ServiceLoader.load(ClassInformationSource.class)
			                                      .stream()
			                                      .max(comparingInt(ClassInformationSourceManager::getPriority))
			                                      .map(ServiceLoader.Provider::get)
			                                      .orElseThrow();
		}
	}

	static ClassInformationSource getInstance() {
		return INSTANCE;
	}

	private static int getPriority(ServiceLoader.Provider<ClassInformationSource> provider) {
		if (provider.type().isAnnotationPresent(Priority.class)) {
			return provider.type().getAnnotation(Priority.class).value();
		}

		// Priority not specified, move it to the lowest possible priority.
		return MIN_VALUE;
	}

}
