module eu.jbeernink.hypospray.scope.common {
	requires jakarta.cdi;

	exports eu.jbeernink.hypospray.scope.common to eu.jbeernink.hypospray.scope.application, eu.jbeernink.hypospray.scope.request;
}