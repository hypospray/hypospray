package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import java.util.Collection;
import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;


public sealed interface InjectionPointInformation extends InjectionPointInfo permits
		ConstructorInjectionPointInformation, FieldInjectionPointInformation,
		InitializationMethodInjectionPointInformation {

	Set<AnnotationInformation> qualifierInformation();

	@Override
	default Collection<AnnotationInfo> qualifiers() {
		return Set.copyOf(qualifierInformation());
	}
}
