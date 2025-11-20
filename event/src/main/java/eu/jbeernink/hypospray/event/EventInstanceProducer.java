package eu.jbeernink.hypospray.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import eu.jbeernink.hypospray.core.annotation.Wildcard;

@ApplicationScoped
public class EventInstanceProducer {

	@Produces
	@Wildcard
	public <T> Event<T> produceEventInstance(EventBus eventBus, InjectionPoint injectionPoint) {
		Type type = injectionPoint.getType();
		Set<Annotation> qualifiers = injectionPoint.getQualifiers();

		return new EventInstance<>(eventBus, type, qualifiers, injectionPoint);
	}
}
