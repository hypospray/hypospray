package eu.jbeernink.hypospray.event;

import jakarta.enterprise.inject.spi.EventContext;
import jakarta.enterprise.inject.spi.EventMetadata;

record EventContextImpl<T>(T event, EventMetadataImpl metadata) implements EventContext<T> {
	@Override
	public T getEvent() {
		return event;
	}

	@Override
	public EventMetadata getMetadata() {
		return metadata;
	}
}
