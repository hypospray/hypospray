package eu.jbeernink.hypospray.integration.tests.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.integration.tests.util.testing.beans.lifecycle.BeanWithCallbackMethods;
import eu.jbeernink.hypospray.integration.tests.util.testing.beans.lifecycle.CallbackLogger;
import eu.jbeernink.hypospray.testing.extension.InContainer;

@InContainer
@DisplayName("Life cycle callbacks: ")
public class CallbackTest {

	@Inject
	private CallbackLogger callbackLogger;

	@Inject
	private BeanContainer container;

	@BeforeEach
	void init() {
		callbackLogger.reset();
	}

	@Test
	@DisplayName("when creating a bean with a post-construct method, the post-construct method is called once.")
	void beanWithPostConstruct_postConstructMethodCalledOnce() {
		Bean<?> callbackBean = container.getBeans(BeanWithCallbackMethods.class).stream().findFirst().orElseThrow();
		var _ = createInstance(callbackBean);

		assertEquals(1L, callbackLogger.getPostConstructCalledCount(BeanWithCallbackMethods.class));
	}

	@Test
	@DisplayName("when creating a bean with a pre-destroy method, the pre-destroy method is not called.")
	void beanWithPreDestroy_preDestroyMethodNotCalled() {
		Bean<?> callbackBean = container.getBeans(BeanWithCallbackMethods.class).stream().findFirst().orElseThrow();
		var _ = createInstance(callbackBean);

		assertEquals(0L, callbackLogger.getPreDestroyCalledCount(BeanWithCallbackMethods.class));
	}

	@Test
	@DisplayName("when destroying a bean with a pre-destroy method, that pre-destroy method is called once.")
	void destroyingBeanWithPreDestroy_preDestroyMethodCalledOnce() {
		@SuppressWarnings("unchecked") Bean<BeanWithCallbackMethods> callbackBean =
				(Bean<BeanWithCallbackMethods>) container.getBeans(BeanWithCallbackMethods.class)
				                                         .stream()
				                                         .findFirst()
				                                         .orElseThrow();
		CreationalContext<BeanWithCallbackMethods> creationalContext = container.createCreationalContext(callbackBean);
		BeanWithCallbackMethods instance = callbackBean.create(creationalContext);

		callbackBean.destroy(instance, creationalContext);

		assertEquals(1L, callbackLogger.getPreDestroyCalledCount(BeanWithCallbackMethods.class));
	}

	private <T> T createInstance(Bean<T> bean) {
		CreationalContext<T> creationalContext = container.createCreationalContext(bean);
		return bean.create(creationalContext);
	}
}
