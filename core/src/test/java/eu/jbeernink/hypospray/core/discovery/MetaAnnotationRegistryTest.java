package eu.jbeernink.hypospray.core.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.inject.Scope;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.ScopeInstance;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;

@DisplayName("MetaAnnotationRegistry")
class MetaAnnotationRegistryTest {

	private ContainerRegistry containerRegistry;
	private MetaAnnotationRegistry metaAnnotationRegistry;

	@BeforeEach
	void setup() {
		containerRegistry = new ContainerRegistry();
		metaAnnotationRegistry = new MetaAnnotationRegistry(containerRegistry);
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, Class<? extends AlterableContext>) with valid context class, registers context instance.")
	void addContext_withValidContextInstance_registersContextInstance() {
		metaAnnotationRegistry.addContext(NormalScopeAnnotation.class, NormalScopeContext.class);

		ScopeInstance scopeInstance = containerRegistry.getContext(NormalScopeAnnotation.class).orElseThrow();
		assertInstanceOf(NormalScopeContext.class, scopeInstance.contexts().getFirst());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, Class<? extends AlterableContext>) with normal scoped annotation class, registers context instance as normal scoped.")
	void addContext_withNormalScopedAnnotation_registersContextInstanceAsNormalScoped() {
		metaAnnotationRegistry.addContext(NormalScopeAnnotation.class, NormalScopeContext.class);

		ScopeInstance scopeInstance = containerRegistry.getContext(NormalScopeAnnotation.class).orElseThrow();
		assertTrue(scopeInstance.isNormalScope());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, Class<? extends AlterableContext>) with non-normal scoped annotation class, registers context instance as non-normal scoped.")
	void addContext_withNonNormalScopedAnnotation_registersContextInstanceAsNonNormalScoped() {
		metaAnnotationRegistry.addContext(ScopeAnnotation.class, ScopeContext.class);

		ScopeInstance scopeInstance = containerRegistry.getContext(ScopeAnnotation.class).orElseThrow();

		assertFalse(scopeInstance.isNormalScope());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, Class<? extends AlterableContext>) with non-scoped annotation class, throws IllegalArgumentException.")
	void addContext_withNonScopedAnnotationClass_throwsIllegalArgumentException() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> metaAnnotationRegistry.addContext(NonScopedAnnotation.class, NonScopedContext.class));

		assertEquals("Implicit scope annotations must be annotated with either @NormalScope or @Scope.",
				exception.getMessage());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, Class<? extends AlterableContext>) with mismatch between annotation and context, throws IllegalArgumentException.")
	void addContext_withMismatchedAnnotation_throwsIllegalArgumentException() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> metaAnnotationRegistry.addContext(ScopeAnnotation.class, NormalScopeContext.class));

		assertEquals(
				"Attempt to register scope annotation @ScopeAnnotation for context which expects to be bound to @NormalScopeAnnotation.",
				exception.getMessage());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, boolean, Class<? extends AlterableContext>) registers context class.")
	void addContext_withNonScopedContextAnnotationClass_registersContext() {
		metaAnnotationRegistry.addContext(NonScopedAnnotation.class, false, NonScopedContext.class);

		ScopeInstance scopeInstance = containerRegistry.getContext(NonScopedAnnotation.class).orElseThrow();

		assertInstanceOf(NonScopedContext.class, scopeInstance.contexts().getFirst());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, boolean, Class<? extends AlterableContext>) with false registers non-normal scope context.")
	void addContext_withNormalScopeSetToFalse_registersContextAsNonNormalScope() {
		metaAnnotationRegistry.addContext(NonScopedAnnotation.class, false, NonScopedContext.class);

		ScopeInstance scopeInstance = containerRegistry.getContext(NonScopedAnnotation.class).orElseThrow();

		assertFalse(scopeInstance.isNormalScope());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, boolean, Class<? extends AlterableContext>) with true registers normal scope context.")
	void addContext_withNormalScopeSetToTrue_registersContextAsNormalScope() {
		metaAnnotationRegistry.addContext(NonScopedAnnotation.class, true, NonScopedContext.class);

		ScopeInstance scopeInstance = containerRegistry.getContext(NonScopedAnnotation.class).orElseThrow();

		assertTrue(scopeInstance.isNormalScope());
	}

	@Test
	@DisplayName(
			"addContext(Class<? extends Annotation>, boolean, Class<? extends AlterableContext>) with mismatch between annotation and context, throws IllegalArgumentException.")
	void addContext_withBooleanAndMismatchedAnnotation_throwsIllegalArgumentException() {
		var exception = assertThrows(IllegalArgumentException.class,
				() -> metaAnnotationRegistry.addContext(NonScopedAnnotation.class, false, NormalScopeContext.class));

		assertEquals(
				"Attempt to register scope annotation @NonScopedAnnotation for context which expects to be bound to @NormalScopeAnnotation.",
				exception.getMessage());
	}

	@NormalScope
	@interface NormalScopeAnnotation {}

	@Scope
	@interface ScopeAnnotation {}

	public static final class ScopeContext implements AlterableContext {

		@Override
		public void destroy(Contextual<?> contextual) {

		}

		@Override
		public Class<? extends Annotation> getScope() {
			return ScopeAnnotation.class;
		}

		@Override
		public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
			return null;
		}

		@Override
		public <T> T get(Contextual<T> contextual) {
			return null;
		}

		@Override
		public boolean isActive() {
			return false;
		}
	}

	public static final class NormalScopeContext implements AlterableContext {

		@Override
		public void destroy(Contextual<?> contextual) {

		}

		@Override
		public Class<? extends Annotation> getScope() {
			return NormalScopeAnnotation.class;
		}

		@Override
		public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
			return null;
		}

		@Override
		public <T> T get(Contextual<T> contextual) {
			return null;
		}

		@Override
		public boolean isActive() {
			return false;
		}
	}

	@interface NonScopedAnnotation {}

	public static final class NonScopedContext implements AlterableContext {

		@Override
		public void destroy(Contextual<?> contextual) {

		}

		@Override
		public Class<? extends Annotation> getScope() {
			return NonScopedAnnotation.class;
		}

		@Override
		public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
			return null;
		}

		@Override
		public <T> T get(Contextual<T> contextual) {
			return null;
		}

		@Override
		public boolean isActive() {
			return false;
		}
	}

}