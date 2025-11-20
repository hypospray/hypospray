package eu.jbeernink.hypospray.scope.request;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;


public class RequestContextManager implements RequestContextController {

	private static final ThreadLocal<AtomicReference<RequestId>> activeRequestId =
			ThreadLocal.withInitial(AtomicReference::new);

	private final AtomicReference<RequestId> owningRequestId = new AtomicReference<>();

	private final BeanContainer beanContainer;

	private final Event<Object> requestScopeInitializedEvent;
	private final Event<Object> requestScopeBeforeDestructionEvent;
	private final Event<Object> requestScopeDestroyedEvent;

	@Inject
	RequestContextManager(BeanContainer beanContainer,
	                      @Initialized(RequestScoped.class) Event<Object> requestScopeInitializedEvent,
	                      @BeforeDestroyed(RequestScoped.class) Event<Object> requestScopeBeforeDestructionEvent,
	                      @Destroyed(RequestScoped.class) Event<Object> requestScopeDestroyedEvent) {
		this.beanContainer = beanContainer;
		this.requestScopeInitializedEvent = requestScopeInitializedEvent;
		this.requestScopeBeforeDestructionEvent = requestScopeBeforeDestructionEvent;
		this.requestScopeDestroyedEvent = requestScopeDestroyedEvent;
	}

	@Override
	public boolean activate() {
		AtomicReference<RequestId> activeRequestId = RequestContextManager.activeRequestId.get();
		if (activeRequestId.get() == null) {
			var newRequestId = RequestId.randomId();

			activeRequestId.set(newRequestId);
			owningRequestId.set(newRequestId);

			requestScopeInitializedEvent.fire(newRequestId);

			return true;
		}

		return false;
	}

	@Override
	public void deactivate() throws ContextNotActiveException {
		AtomicReference<RequestId> activeRequestId = RequestContextManager.activeRequestId.get();
		if (activeRequestId.get() == null) {
			throw new ContextNotActiveException("Context not active");
		}

		if (activeRequestId.get().equals(owningRequestId.get())) {
			RequestContext context = (RequestContext) beanContainer.getContext(RequestScoped.class);

			var requestId = owningRequestId.getAndSet(null);

			requestScopeBeforeDestructionEvent.fire(requestId);

			activeRequestId.set(null);
			context.destroyScope(requestId);

			requestScopeDestroyedEvent.fire(requestId);
		}
	}

	public static Optional<RequestId> getCurrentRequestId() {
		return Optional.ofNullable(activeRequestId.get().get());
	}
}
