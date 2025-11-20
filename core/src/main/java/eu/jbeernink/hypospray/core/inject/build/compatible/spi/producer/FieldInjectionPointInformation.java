package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import java.util.Set;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record FieldInjectionPointInformation(TypeInstance type, Set<AnnotationInformation> qualifierInformation,
                                             FieldInformation declaration) implements InjectionPointInformation {

	public FieldInjectionPointInformation {
		qualifierInformation = Set.copyOf(qualifierInformation);
	}
}
