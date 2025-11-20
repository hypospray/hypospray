package eu.jbeernink.hypospray.integration.tests.util.testing.beans.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
public class BeanWithCallbackMethods {

	private final CallbackLogger callbackLogger;

	@Inject
	public BeanWithCallbackMethods(CallbackLogger callbackLogger) {
		this.callbackLogger = callbackLogger;
	}

	@PostConstruct
	public void postConstruct() {
		callbackLogger.onPostConstruct(this);
	}

	@PreDestroy
	public void preDestroy() {
		callbackLogger.onPreDestroy(this);
	}
}
