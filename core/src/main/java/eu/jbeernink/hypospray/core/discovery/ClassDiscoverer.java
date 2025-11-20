package eu.jbeernink.hypospray.core.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;

public class ClassDiscoverer implements ScannedClasses {

	private final Map<Class<?>, ClassInformation<?>> discoveredClasses = new HashMap<>();

	private final ClassInformationSource classInformationSource = ClassInformationSource.getInstance();

	public void add(Class<?> clazz) {
		discoveredClasses.computeIfAbsent(clazz, classInformationSource::getClassInformation);
	}

	@Override
	public void add(String className) {
		try {
			add(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class not found: \"" + className + "\"", e);
		}
	}

	public List<ClassInformation<?>> getDiscoveredClasses() {
		return List.copyOf(discoveredClasses.values());
	}
}
