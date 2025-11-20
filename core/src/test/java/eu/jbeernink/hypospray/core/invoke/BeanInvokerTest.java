package eu.jbeernink.hypospray.core.invoke;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.dependent.DependentScopeContext;
import eu.jbeernink.hypospray.core.context.singleton.SingletonScopeContext;
import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.producer.SupplierInjectionTarget;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

class BeanInvokerTest {

	private BeanInvoker<TestBean, String> beanInvoker;

	private TestBean testBean;

	@BeforeEach
	void setup() {
		testBean = new TestBean();
		DiscoveredBean<TestBean> bean = DiscoveredBean.<TestBean>newBuilder(TestBean.class)
		                                              .setInjectionTarget(new SupplierInjectionTarget<>(() -> testBean))
		                                              .build();

		var containerRegistry = new ContainerRegistry();
		containerRegistry.registerScope(Dependent.class, new DependentScopeContext(), false);
		containerRegistry.registerScope(Singleton.class, new SingletonScopeContext(), false);
		Invoker<TestBean, String> invoker =
				(instance, args) -> instance.method((String) args[0], (String) args[1], (String) args[2]);

		BeanContainer beanContainer = new InjectableBeanContainer(new CreationalContextManager(), containerRegistry);
		beanInvoker = new BeanInvoker<>(beanContainer, bean, TestBean.class, invoker);
	}

	@Test
	@DisplayName("invoke(Void, Object[]) calls the method on the looked up instance.")
	void invoke_callsMethodOnGivenInstance() throws Exception {
		var _ = beanInvoker.invoke(null, new Object[]{"a", "b", "c"});

		assertEquals(List.of(new Invocation("a", "b", "c")), testBean.invocations);
	}

	@Test
	@DisplayName("invoke(T, Object[]) returns the result of the method call.")
	void invoke_returnsResultOfMethodCall() throws Exception {
		String result = beanInvoker.invoke(null, new Object[]{"foo", "+", "bar"});

		assertEquals("foo+bar", result);
	}

	@Test
	@DisplayName("invoke(T, Object[]) when the invoked method throws an exception, rethrows that exception.")
	void invoke_withMethodThatThrows_rethrowsExceptionFromMethod() {
		var _ = assertThrows(NullPointerException.class, () -> beanInvoker.invoke(null, new Object[]{null, null, null}));
	}

	private record Invocation(String a, String b, String c) {}

	public static class TestBean {
		final List<Invocation> invocations = new ArrayList<>();

		public String method(String a, String b, String c) {
			requireNonNull(a);
			invocations.add(new Invocation(a, b, c));
			return String.format("%s%s%s", a, b, c);
		}
	}
}