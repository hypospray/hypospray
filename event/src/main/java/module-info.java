module eu.jbeernink.hypospray.event {
	requires jakarta.cdi;

	requires eu.jbeernink.hypospray.core;
	requires eu.jbeernink.hypospray.scope.application;

	opens eu.jbeernink.hypospray.event to eu.jbeernink.hypospray.core;
	opens eu.jbeernink.hypospray.event.type to eu.jbeernink.hypospray.core;
}