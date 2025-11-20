package eu.jbeernink.hypospray.core.inject.spi.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.CreationException;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.exception.DisposalException;
import eu.jbeernink.hypospray.core.inject.BeanInstance;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ImmutableParameters;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.inject.spi.ManagedBean;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

@DisplayName("SyntheticBeanProducer")
class SyntheticBeanProducerTest {
	private record DisposerCall(FakeBean instance, Instance<Object> lookup, Parameters param) {}

	private final List<DisposerCall> disposerCalls = new ArrayList<>();

	private ContainerRegistry containerRegistry;
	private CreationalContextManager creationalContextManager;

	private ManagedBean<FakeBean> fakeBeanBean;

	private ImmutableParameters parameters;

	private SyntheticBeanProducer<FakeBean> syntheticBeanProducer;

	@BeforeEach
	void setup() {
		containerRegistry = new ContainerRegistry();
		creationalContextManager = new CreationalContextManager();

		parameters = new ImmutableParameters(Map.of("String", 5));

		var beanContainerBean = DiscoveredBean.<BeanContainer>newBuilder(InjectableBeanContainer.class)
		                                      .addType(BeanContainer.class)
		                                      .setInjectionTarget(new InjectionTarget<>() {
			                                      @Override
			                                      public void inject(BeanContainer instance,
			                                                         CreationalContext<BeanContainer> ctx) {

			                                      }

			                                      @Override
			                                      public void postConstruct(BeanContainer instance) {

			                                      }

			                                      @Override
			                                      public void preDestroy(BeanContainer instance) {

			                                      }

			                                      @Override
			                                      public BeanContainer produce(CreationalContext<BeanContainer> ctx) {
				                                      return null;
			                                      }

			                                      @Override
			                                      public void dispose(BeanContainer instance) {

			                                      }

			                                      @Override
			                                      public Set<InjectionPoint> getInjectionPoints() {
				                                      return Set.of();
			                                      }
		                                      })
		                                      .build();
		containerRegistry.registerBean(beanContainerBean);
	}

	@Nested
	@DisplayName("with regular creator and disposer")
	class WithRegularCreatorAndDisposer {

		private SyntheticBeanProducer<FakeBean> syntheticBeanProducer;

		@BeforeEach
		void setup() {
			syntheticBeanProducer =
					new SyntheticBeanProducer<>(new InjectableBeanContainer(creationalContextManager, containerRegistry),
							parameters, (_, _) -> new SyntheticBeanProducerTest.FakeBeanCreator(),
							(_, _) -> new SyntheticBeanProducerTest.FakeBeanDisposer());

			fakeBeanBean = DiscoveredBean.<FakeBean>newBuilder(FakeBean.class)
			                             .addType(FakeBean.class)
			                             .setInjectionTarget(syntheticBeanProducer)
			                             .build();
		}

		@Test
		@DisplayName("produce(HyposprayCreationalContext<T>) calls the bean creator to produce a new bean.")
		void produce_callsBeanCreatorToProduceNewBean() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			assertInstanceOf(FakeBean.class, fakeBean);
		}

		@Test
		@DisplayName("produce(HyposprayCreationalCotnext<T>) calls the bean creator with an instance object.")
		void produce_callsBeanCreatorWithInstanceObject() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			assertInstanceOf(BeanInstance.class, fakeBean.instance);
		}

		@Test
		@DisplayName("produce(HyposprayCreationalContext<T>) calls the bean creator with the specified parameters.")
		void produce_callsBeanCreatorWithParameters() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			assertEquals(parameters, fakeBean.parameters);
		}

		@Test
		@DisplayName("dispose(T instance) calls the bean disposer once.")
		void dispose_callsBeanDisposerOnce() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			syntheticBeanProducer.dispose(fakeBean);

			assertEquals(1, disposerCalls.size());
		}

		@Test
		@DisplayName("dispose(T instance) calls the bean disposer with the instance to dispose.")
		void dispose_callsBeanDisposerWithInstanceToDispose() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			syntheticBeanProducer.dispose(fakeBean);

			assertEquals(fakeBean, disposerCalls.removeFirst().instance);
		}

		@Test
		@DisplayName("dispose(T instance) calls the bean disposer with a BeanInstance.")
		void dispose_callsBeanDisposer() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			syntheticBeanProducer.dispose(fakeBean);


			assertInstanceOf(BeanInstance.class, disposerCalls.getFirst().lookup);
		}

		@Test
		@DisplayName("dispose(T instance) calls the bean disposer with the parameters specified.")
		void dispose_callsBeanDisposerWithParameters() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			syntheticBeanProducer.dispose(fakeBean);

			assertEquals(parameters, disposerCalls.getFirst().param);
		}
	}

	@Nested
	@DisplayName("with creator that throws an exception")
	class WithCreatorThatThrowsException {

		@BeforeEach
		void setup() {
			SyntheticBeanCreator<FakeBean> throwingCreator = (_, _) -> {
				throw new IllegalArgumentException();
			};
			syntheticBeanProducer =
					new SyntheticBeanProducer<>(new InjectableBeanContainer(creationalContextManager, containerRegistry),
							parameters, (_, _) -> throwingCreator, (_, _) -> new SyntheticBeanProducerTest.FakeBeanDisposer());

			fakeBeanBean = DiscoveredBean.<FakeBean>newBuilder(FakeBean.class)
			                             .addType(FakeBean.class)
			                             .setInjectionTarget(syntheticBeanProducer)
			                             .build();
		}

		@Test
		@DisplayName("producer(HyposprayCreationalContext) throws CreationException.")
		void producer_throwsCreationException() {
			var exception = assertThrows(CreationException.class,
					() -> syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean)));

			assertEquals("Unable to produce bean instance.", exception.getMessage());
		}
	}

	@Nested
	@DisplayName("with a disposer that throws an exception")
	class WithDisposerThatThrowsException {

		@BeforeEach
		void setup() {
			SyntheticBeanDisposer<FakeBean> throwingDisposer = (_, _, _) -> {
				throw new IllegalArgumentException();
			};
			syntheticBeanProducer =
					new SyntheticBeanProducer<>(new InjectableBeanContainer(creationalContextManager, containerRegistry),
							parameters, (_, _) -> new SyntheticBeanProducerTest.FakeBeanCreator(), (_, _) -> throwingDisposer);

			fakeBeanBean = DiscoveredBean.<FakeBean>newBuilder(FakeBean.class)
			                             .addType(FakeBean.class)
			                             .setInjectionTarget(syntheticBeanProducer)
			                             .build();
		}

		@Test
		@DisplayName("dispose(T) throws InvocationException.")
		void dispose_throwsInvocationException() {
			FakeBean fakeBean = syntheticBeanProducer.produce(creationalContextManager.newCreationalContext(fakeBeanBean));

			var exception = assertThrows(DisposalException.class, () -> syntheticBeanProducer.dispose(fakeBean));

			assertTrue(exception.getMessage().startsWith("Unable to dispose bean "));
		}
	}

	private static final class FakeBeanCreator implements SyntheticBeanCreator<FakeBean> {

		@Override
		public FakeBean create(Instance<Object> lookup, Parameters params) {
			return new FakeBean(lookup, params);
		}
	}

	private class FakeBeanDisposer implements SyntheticBeanDisposer<FakeBean> {

		@Override
		public void dispose(FakeBean instance, Instance<Object> lookup, Parameters params) {
			disposerCalls.add(new DisposerCall(instance, lookup, params));
		}
	}

	private record FakeBean(Instance<Object> instance, Parameters parameters) {

	}
}