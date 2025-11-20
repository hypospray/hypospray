package eu.jbeernink.hypospray.model.information.synthetic;

import java.util.List;
import java.util.Objects;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;

public record SyntheticPackageInformation(@Override String name,
                                          @Override List<AnnotationInformation> annotationInformation) implements
		PackageInformation {

	public SyntheticPackageInformation {
		annotationInformation = List.copyOf(annotationInformation);
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case PackageInformation other -> Objects.equals(name, other.name());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
