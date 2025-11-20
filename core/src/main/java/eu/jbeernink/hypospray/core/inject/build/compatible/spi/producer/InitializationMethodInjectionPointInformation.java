package eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer;

import java.util.Set;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record InitializationMethodInjectionPointInformation(TypeInstance type,
                                                            Set<AnnotationInformation> qualifierInformation,
                                                            MethodInformation declaration, int index) implements
		InjectionPointInformation {


}
