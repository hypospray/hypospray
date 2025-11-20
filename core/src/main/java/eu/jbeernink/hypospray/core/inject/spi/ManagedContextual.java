package eu.jbeernink.hypospray.core.inject.spi;

import jakarta.enterprise.context.spi.Contextual;

public sealed interface ManagedContextual<T> extends Contextual<T> permits ManagedBean, ManagedInterceptor {}
