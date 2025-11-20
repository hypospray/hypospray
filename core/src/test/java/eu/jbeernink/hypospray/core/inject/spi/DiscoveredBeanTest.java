package eu.jbeernink.hypospray.core.inject.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

@DisplayName("ManagedBean")
class DiscoveredBeanTest {

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	private InjectableBeanContainer beanContainer;
	private HyposprayCreationalContext<Integer> creationalContext;

	private DiscoveredBean<Integer> discoveredBean;

	@BeforeEach
	void setup() {
		beanContainer = new InjectableBeanContainer(new CreationalContextManager(), new ContainerRegistry());
		discoveredBean = new DiscoveredBean<>(new ReflectiveClassInformation<>(Integer.class),
				Set.of(typeFactory.of(Integer.class), typeFactory.of(Number.class), typeFactory.of(Object.class)),
				Set.of(new ReflectiveAnnotationInformation<>(Default.Literal.INSTANCE)), new ReflectiveClassInformation<>(Dependent.class), Set.of(), new FakeInjectionTarget(), null, false);
		creationalContext = beanContainer.createCreationalContext(discoveredBean);
	}

	@Test
	@DisplayName("calling create(CreationalContext<T>) creates a new instance.")
	void create_createsNewInstance() {
		Integer instance = discoveredBean.create(creationalContext);

		assertEquals(1, instance);
	}

	private final static class FakeInjectionTarget implements InjectionTarget<Integer> {

		private final AtomicInteger count = new AtomicInteger(0);

		@Override
		public void inject(Integer instance, CreationalContext<Integer> ctx) {

		}

		@Override
		public void postConstruct(Integer instance) {

		}

		@Override
		public void preDestroy(Integer instance) {

		}

		@Override
		public Integer produce(CreationalContext<Integer> ctx) {
			return count.incrementAndGet();
		}

		@Override
		public void dispose(Integer instance) {

		}

		@Override
		public Set<InjectionPoint> getInjectionPoints() {
			return null;
		}
	}

}