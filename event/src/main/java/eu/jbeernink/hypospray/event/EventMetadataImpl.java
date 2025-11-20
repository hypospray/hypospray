package eu.jbeernink.hypospray.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.InjectionPoint;

record EventMetadataImpl(Type type, Set<Annotation> qualifiers, InjectionPoint injectionPoint) implements
		EventMetadata {

	public EventMetadataImpl {
		qualifiers = Set.copyOf(qualifiers);
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers;
	}

	@Override
	public InjectionPoint getInjectionPoint() {
		return injectionPoint;
	}
}
