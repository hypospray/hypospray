package eu.jbeernink.hypospray.event.type;

public record ClassEventType<T>(Class<T> type) implements EventType<T> {}
