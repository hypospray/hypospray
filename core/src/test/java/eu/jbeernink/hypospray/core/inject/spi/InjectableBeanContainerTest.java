package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Prioritized;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.context.spi.HyposprayCreationalContext;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

@DisplayName("InjectableBeanContainer")
class InjectableBeanContainerTest {

	private CreationalContextManager creationalContextManager;
	private ContainerRegistry containerRegistry;

	private InjectableBeanContainer beanContainer;

	@BeforeEach
	void setup() {
		creationalContextManager = new CreationalContextManager();
		containerRegistry = new ContainerRegistry();
		beanContainer = new InjectableBeanContainer(creationalContextManager, containerRegistry);
	}

	@Test
	@DisplayName("resolve(Set<Beans<?>>) with a null set of beans, returns null.")
	void resolve_withNull_returnsNull() {
		Bean<?> bean = beanContainer.resolve(null);

		assertNull(bean);
	}

	@Test
	@DisplayName("resolve(Set<Beans<?>>) with an empty set, returns null.")
	void resolve_withEmptySet_returnsNull() {
		Bean<?> bean = beanContainer.resolve(Set.of());

		assertNull(bean);
	}

	@Test
	@DisplayName("resolve(Set<Beans<?>>) with a single bean, returns the expected bean.")
	void resolve_withSingleBean_returnsExpectedBean() {
		var bean = new NonPrioritizedBean<>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), false);

		Bean<?> resolvedBean = beanContainer.resolve(Set.of(bean));

		assertEquals(bean, resolvedBean);
	}

	@Test
	@DisplayName("resolve(Set<Beans<?>>) with a single bean and a disabled alternative, returns the expected bean.")
	void resolve_withSingleBeanAndDisabledAlternative_returnsExpectedBean() {
		var bean = new NonPrioritizedBean<>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), false);
		var alternative = new NonPrioritizedBean<>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), true);

		Bean<?> resolvedBean = beanContainer.resolve(Set.of(bean, alternative));

		assertEquals(bean, resolvedBean);
	}

	@Test
	@DisplayName(
			"resolve(Set<Beans<?>>) with a single bean and multiple enabled alternatives, returns the highest priority alternative.")
	void resolve_withSingleBeanAndMultipleEnabledAlterntives_returnsHighestPriorityAlternative() {
		var bean = new NonPrioritizedBean<>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), false);
		var highPriorityAlternative =
				new PrioritizedBean<Object>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), true, 10);
		var lowPriorityAlternative =
				new PrioritizedBean<Object>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), true, 1);

		Bean<?> resolvedBean = beanContainer.resolve(Set.of(bean, highPriorityAlternative, lowPriorityAlternative));

		assertEquals(highPriorityAlternative, resolvedBean);
	}

	@Test
	@DisplayName("resolve(Set<Beans<?>>) with multiple beans, throws AmbiguousResolutionException.")
	void resolve_withMultipleBeans_throwsAmbiguousResolutionException() {
		var bean = new NonPrioritizedBean<>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), false);
		var otherBean = new NonPrioritizedBean<String>(String.class, Set.of(), Set.of(), Dependent.class, Set.of(), false);

		var exception =
				assertThrows(AmbiguousResolutionException.class, () -> beanContainer.resolve(Set.of(bean, otherBean)));

		assertEquals("Unable to resolve bean, multiple matching beans found.", exception.getMessage());
	}

	@Test
	@DisplayName("createCreationalContext(Contextual<?>) creates a new CreationalContext.")
	void createCreationalContext_createsNewCreationalContext() {
		NonPrioritizedBean<Object> contextual =
				new NonPrioritizedBean<>(Object.class, Set.of(), Set.of(), Dependent.class, Set.of(), false);

		HyposprayCreationalContext<Object> creationalContext = beanContainer.createCreationalContext(contextual);

		assertNotNull(creationalContext);
	}


	record NonPrioritizedBean<T>(Class<T> beanClass,

	                             Set<Type> types,

	                             Set<Annotation> qualifiers,

	                             Class<? extends Annotation> scope,

	                             Set<Class<? extends Annotation>> stereotypes, boolean isAlternative) implements
			BeanImpl<T> {}

	record PrioritizedBean<T>(Class<T> beanClass,

	                          Set<Type> types,

	                          Set<Annotation> qualifiers,

	                          Class<? extends Annotation> scope,

	                          Set<Class<? extends Annotation>> stereotypes, boolean isAlternative,
	                          int priority) implements BeanImpl<T>, Prioritized {
		@Override
		public int getPriority() {
			return priority;
		}
	}

	interface BeanImpl<T> extends Bean<T> {

		Class<?> beanClass();

		Set<Type> types();

		Set<Annotation> qualifiers();

		Class<? extends Annotation> scope();

		Set<Class<? extends Annotation>> stereotypes();

		@Override
		default Class<?> getBeanClass() {
			return beanClass();
		}

		@Override
		default Set<InjectionPoint> getInjectionPoints() {
			return Set.of();
		}

		@Override
		default T create(CreationalContext<T> creationalContext) {
			return unimplemented();
		}

		@Override
		default void destroy(T instance, CreationalContext<T> creationalContext) {
			unimplemented();
		}

		@Override
		default Set<Type> getTypes() {
			return types();
		}

		@Override
		default Set<Annotation> getQualifiers() {
			return qualifiers();
		}

		@Override
		default Class<? extends Annotation> getScope() {
			return scope();
		}

		@Override
		default String getName() {
			return null;
		}

		@Override
		default Set<Class<? extends Annotation>> getStereotypes() {
			return stereotypes();
		}

		@Override
		boolean isAlternative();
	}


}