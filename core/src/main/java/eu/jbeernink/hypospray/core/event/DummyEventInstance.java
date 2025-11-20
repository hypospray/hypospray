package eu.jbeernink.hypospray.core.event;

import static java.util.concurrent.CompletableFuture.completedStage;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

import jakarta.annotation.Priority;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.util.TypeLiteral;

import eu.jbeernink.hypospray.core.annotation.Wildcard;

/// Event instances that does not handle firing events.
@Wildcard
@Alternative
@Typed(Event.class)
@Priority(0)
public class DummyEventInstance<T> implements Event<T> {
	@Override
	public void fire(T event) {
	}

	@Override
	public <U extends T> CompletionStage<U> fireAsync(U event) {
		return completedStage(event);
	}

	@Override
	public <U extends T> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
		return completedStage(event);
	}

	@Override
	public Event<T> select(Annotation... qualifiers) {
		return this;
	}

	@Override
	public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
		return new DummyEventInstance<>();
	}

	@Override
	public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		return new DummyEventInstance<>();
	}
}
