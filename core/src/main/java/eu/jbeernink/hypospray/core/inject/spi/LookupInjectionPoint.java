package eu.jbeernink.hypospray.core.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Injection point for beans that are created via a lookup.
@NullMarked
public record LookupInjectionPoint(TypeInstance type, Set<AnnotationInformation> qualifiers) implements InjectionPoint {

	public LookupInjectionPoint {
		qualifiers = Set.copyOf(qualifiers);
	}

	@Override
	public Type getType() {
		return type.asJavaType();
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return qualifiers.stream()
				.map(AnnotationInformation::annotationInstance)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public @Nullable Bean<?> getBean() {
		return null;
	}

	@Override
	public @Nullable Member getMember() {
		return null;
	}

	@Override
	public @Nullable Annotated getAnnotated() {
		return null;
	}

	@Override
	public boolean isDelegate() {
		return false;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
}
