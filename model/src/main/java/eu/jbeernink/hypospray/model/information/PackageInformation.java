package eu.jbeernink.hypospray.model.information;

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.PackageInfo;

import eu.jbeernink.hypospray.model.information.reflection.ReflectivePackageInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticPackageInformation;

/// Information about a Java package.
///
/// ## Equality
/// To different instances of `PackageInformation` are considered equal if they refer to a package of the same name.
///
/// ### Hash code
/// The hash code of a `PackageInformation` instance must be calculated as follows: {@snippet :
///  Objects.hash(packageInformation.name());
/// }
public sealed interface PackageInformation extends PackageInfo, AnnotatedDeclaration permits
		SyntheticPackageInformation, ReflectivePackageInformation {

	@Override
	default Collection<AnnotationInfo> annotations() {
		return List.copyOf(annotationInformation());
	}

	List<AnnotationInformation> annotationInformation();
}
