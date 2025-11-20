package eu.jbeernink.hypospray.event.type;

import jakarta.enterprise.util.TypeLiteral;

public record TypeLiteralEventType<T>(TypeLiteral<T> typeLiteral) implements EventType<T> {}
