import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

import eu.jbeernink.hypospray.scope.request.RequestScopeExtension;

module eu.jbeernink.hypospray.scope.request {
	requires jakarta.cdi;

	requires eu.jbeernink.hypospray.core;
	requires eu.jbeernink.hypospray.scope.common;

	opens eu.jbeernink.hypospray.scope.request to eu.jbeernink.hypospray.core;

	provides BuildCompatibleExtension with RequestScopeExtension;
}