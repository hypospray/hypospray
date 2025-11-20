package eu.jbeernink.hypospray.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;

public record EventInstance<T>(EventBus eventBus, Type eventType, Set<Annotation> qualifiers,
                               InjectionPoint injectionPoint) implements Event<T> {

	public EventInstance {
		qualifiers = Set.copyOf(qualifiers);
	}

	@Override
	public void fire(T event) {
		eventBus.fire(this, event);
	}

	@Override
	public <U extends T> CompletionStage<U> fireAsync(U event) {
		return eventBus.fireAsync(this, event);
	}

	@Override
	public <U extends T> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
		return eventBus.fireAsync(this, event, options);
	}

	private <U extends T> Event<U> select(Class<U> subtype) {
		return new EventInstance<>(eventBus, subtype, qualifiers, injectionPoint);
	}

	private <U extends T> Event<U> select(TypeLiteral<U> typeLiteral) {
		return new EventInstance<>(eventBus, typeLiteral.getType(), qualifiers, injectionPoint);
	}

	@Override
	public Event<T> select(Annotation... qualifiers) {
		if (qualifiers.length == 0) {
			return this;
		}

		HashSet<Annotation> newQualifiers = new HashSet<>(this.qualifiers);
		newQualifiers.addAll(Arrays.asList(qualifiers));

		return new EventInstance<>(eventBus, eventType, newQualifiers, injectionPoint);
	}

	@Override
	public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
		return select(subtype).select(qualifiers);
	}

	@Override
	public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		return select(subtype).select(qualifiers);
	}
}
