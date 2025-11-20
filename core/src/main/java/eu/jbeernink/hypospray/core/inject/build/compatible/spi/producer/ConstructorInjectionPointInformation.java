package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import java.util.Set;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ParameterInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record ConstructorInjectionPointInformation(ParameterInformation declaration,
                                                   Set<AnnotationInformation> qualifierInformation) implements
		InjectionPointInformation {

	@Override
	public TypeInstance type() {
		return declaration.type();
	}
}
