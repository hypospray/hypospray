module eu.jbeernink.hypospray.lite {
	requires transitive jakarta.cdi;
	requires eu.jbeernink.hypospray.core;
	requires eu.jbeernink.hypospray.discovery.annotated;
	requires eu.jbeernink.hypospray.discovery.none;
	requires eu.jbeernink.hypospray.scope.application;
	requires eu.jbeernink.hypospray.scope.request;
}