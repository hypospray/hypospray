package eu.jbeernink.hypospray.integration.tests.util.testing.beans.lifecycle;

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CallbackLogger {

	private final List<Class<?>> postConstructCalled = synchronizedList(new ArrayList<>());

	private final List<Class<?>> preDestroyCalled = synchronizedList(new ArrayList<>());

	public void onPostConstruct(Object bean) {
		postConstructCalled.add(bean.getClass());
	}

	public void onPreDestroy(Object bean) {
		preDestroyCalled.add(bean.getClass());
	}

	public void reset() {
		postConstructCalled.clear();
		preDestroyCalled.clear();
	}

	public long getPostConstructCalledCount(Class<?> clazz) {
		return postConstructCalled.stream().filter(c -> c.equals(clazz)).count();
	}


	public long getPreDestroyCalledCount(Class<?> clazz) {
		return preDestroyCalled.stream().filter(c -> c.equals(clazz)).count();
	}
}
