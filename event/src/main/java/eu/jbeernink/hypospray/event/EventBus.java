package eu.jbeernink.hypospray.event;

import static java.lang.System.Logger.Level.WARNING;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

import java.lang.System.Logger;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventBus {

	private static final Logger logger = System.getLogger(EventBus.class.getName());

	@FunctionalInterface
	private interface RequestScopeManager extends AutoCloseable {
		@Override
		void close();
	}

	private final BeanContainer beanContainer;
	private final Instance<RequestContextController> requestContextControllerInstance;

	EventBus() {
		// Constructor only for proxy.
		this(null, null);
	}

	@Inject
	public EventBus(BeanContainer beanContainer, Instance<RequestContextController> requestContextControllerInstance) {
		this.beanContainer = beanContainer;
		this.requestContextControllerInstance = requestContextControllerInstance;
	}

	public <T> void fire(EventInstance<T> eventInstance, T event) {
		Set<ObserverMethod<? super T>> observerMethods = resolveObserverMethods(eventInstance, event);

		observerMethods.forEach(observerMethod -> observerMethod.notify(event));
	}

	private <T> Set<ObserverMethod<? super T>> resolveObserverMethods(EventInstance<T> eventInstance, T event) {
		return beanContainer.resolveObserverMethods(event, eventInstance.qualifiers().toArray(new Annotation[0]));
	}

	public <E extends T, T> CompletionStage<E> fireAsync(EventInstance<T> eventInstance, E event) {
		return fireAsync(eventInstance, event, NotificationOptions.ofExecutor(newVirtualThreadPerTaskExecutor()));
	}

	public <E extends T, T> CompletionStage<E> fireAsync(EventInstance<T> eventInstance, E event,
	                                                     NotificationOptions options) {
		Set<ObserverMethod<? super T>> observerMethods = resolveObserverMethods(eventInstance, event);

		var notificationTasks = observerMethods.stream()
		                                       .map(observerMethod -> fireAsync(observerMethod, eventInstance, event,
				                                       options))
		                                       .toArray(CompletableFuture[]::new);

		return CompletableFuture.allOf(notificationTasks).thenApply(_ -> event);
	}

	private <T> CompletableFuture<Void> fireAsync(ObserverMethod<? super T> observerMethod,
	                                              EventInstance<T> eventInstance, T event, NotificationOptions options) {
		var eventMetadata =
				new EventMetadataImpl(eventInstance.eventType(), eventInstance.qualifiers(), eventInstance.injectionPoint());

		if (observerMethod.isAsync()) {
			return CompletableFuture.runAsync(() -> {
				try (var _ = startRequestScope()) {
					observerMethod.notify(new EventContextImpl<>(event, eventMetadata));
				}
			}, options.getExecutor());
		}
		try {
			observerMethod.notify(new EventContextImpl<>(event, eventMetadata));
		} catch (Exception e) {
			return failedFuture(e);
		}

		return completedFuture(null);
	}

	private RequestScopeManager startRequestScope() {
		if (requestContextControllerInstance.isResolvable()) {
			RequestContextController requestContextController = requestContextControllerInstance.get();

			if (requestContextController.activate()) {
				return () -> {
					requestContextController.deactivate();
					requestContextControllerInstance.destroy(requestContextController);
				};
			}
		} else {
			logger.log(WARNING, "RequestContextController not available for asynchronous events.");
		}

		return () -> {};
	}
}
