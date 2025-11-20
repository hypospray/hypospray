package eu.jbeernink.hypospray.event.type;

public sealed interface EventType<T> permits ClassEventType, TypeLiteralEventType {}
