package eu.jbeernink.hypospray.demo;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import eu.jbeernink.hypospray.demo.qualifier.Qualified;

@Dependent
public class EventSource {

	@Inject
	@Qualified
	private Event<String> event;

	@Override
	public String toString() {
		return "EventSource{" + "event=" + event + '}';
	}
}
