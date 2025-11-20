package eu.jbeernink.hypospray.model.information.reflection;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;

/// [eu.jbeernink.hypospray.model.information.PackageInformation] wrapping a [Package] using the reflection API.
public record ReflectivePackageInformation(Package packageInstance) implements PackageInformation {
	@Override
	public String name() {
		return packageInstance.getName();
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return Arrays.stream(packageInstance.getAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case PackageInformation other -> Objects.equals(name(), other.name());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(name());
	}
}
