package eu.jbeernink.hypospray.scope.request;

import java.util.UUID;

import eu.jbeernink.hypospray.scope.common.ScopeInstanceKey;

public record RequestId(UUID id) implements ScopeInstanceKey {

	public static RequestId randomId() {
		return new RequestId(UUID.randomUUID());
	}
}
