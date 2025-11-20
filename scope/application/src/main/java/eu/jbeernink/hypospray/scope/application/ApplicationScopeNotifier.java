package eu.jbeernink.hypospray.scope.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;

@ApplicationScoped
public class ApplicationScopeNotifier {

	private final Event<Object> applicationScopeInitializedEvent;
	private final Event<Object> applicationScopePreDestructionEvent;

	private final Event<Object> applicationScopeDestroyedEvent;

	@Inject
	public ApplicationScopeNotifier(@Initialized(ApplicationScoped.class) Event<Object> applicationScopeInitializedEvent,
	                                @BeforeDestroyed(ApplicationScoped.class)
	                                Event<Object> applicationScopePreDestructionEvent,
	                                @Destroyed(ApplicationScoped.class) Event<Object> applicationScopeDestroyedEvent) {
		this.applicationScopeInitializedEvent = applicationScopeInitializedEvent;
		this.applicationScopePreDestructionEvent = applicationScopePreDestructionEvent;
		this.applicationScopeDestroyedEvent = applicationScopeDestroyedEvent;
	}

	void observesContainerIntialization(@Observes Startup event) {
		applicationScopeInitializedEvent.fire(null);
	}

	void observesContainerShuttingDown(@Observes Shutdown event, BeanContainer beanContainer) {
		applicationScopePreDestructionEvent.fire(null);

		ApplicationScopeContext applicationScopedContext =
				(ApplicationScopeContext) beanContainer.getContext(ApplicationScoped.class);

		applicationScopedContext.destroyScope();

		applicationScopeDestroyedEvent.fire(null);
	}
}
