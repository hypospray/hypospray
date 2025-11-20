package eu.jbeernink.hypospray.core.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.System.Logger;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.Predicate;

import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.DisposerInfo;
import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.inject.build.compatible.spi.ScopeInfo;
import jakarta.enterprise.inject.build.compatible.spi.StereotypeInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationTarget;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.DeclarationInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.exception.DeploymentException;

@DisplayName("ContainerInitializationLogger")
class ContainerInitializationLoggerTest {

	private ContainerInitializationLogger containerInitializationLogger;

	@BeforeEach
	void setup() {
		containerInitializationLogger = ContainerInitializationLogger.forClass(ContainerInitializationLoggerTest.class);
	}

	@Test
	@DisplayName("calling error(String) throws a DeploymentException.")
	public void error_throwsDeploymentException() {
		var exception = assertThrows(DeploymentException.class, () -> containerInitializationLogger.error("test"));

		assertEquals("test", exception.getMessage());
	}

	@Test
	@DisplayName("calling error(String, AnnotationTarget) throws a DeploymentException.")
	public void error_withAnnotationTarget_throwsDeploymentException() {
		var exception = assertThrows(DeploymentException.class,
				() -> containerInitializationLogger.error("test", new FakeAnnotationTarget()));

		assertEquals("Annotation target Fake Annotation Target: test", exception.getMessage());
	}

	@Test
	@DisplayName("calling error(String, BeanInfo) throws a DeploymentException.")
	public void error_withBeanInfo_throwsDeploymentException() {
		var exception =
				assertThrows(DeploymentException.class, () -> containerInitializationLogger.error("test", new FakeBeanInfo()));

		assertEquals("Bean Fake bean info: test", exception.getMessage());
	}

	@Test
	@DisplayName("calling error(String, ObserverInfo) throws a DeploymentException.")
	public void error_withObserverInfo_throwsDeploymentException() {
		var exception = assertThrows(DeploymentException.class,
				() -> containerInitializationLogger.error("test", new FakeObserverInfo()));

		assertEquals("Observer Fake Observer Info: test", exception.getMessage());
	}

	@Test
	@DisplayName("calling error(Exception) throws a DeploymentException with the expected message.")
	public void error_withException_throwsDeploymentExceptionWithExpectedMessage() {
		var exception =
				assertThrows(DeploymentException.class, () -> containerInitializationLogger.error(new RuntimeException("Foo")));

		assertEquals("java.lang.RuntimeException: Foo", exception.getMessage());
	}

	@Test
	@DisplayName("calling error(Exception) throws a DeploymentException with the expected cause.")
	public void error_withException_throwsDeploymentExceptionWithExpectedCause() {
		var cause = new RuntimeException("Foo");
		var exception = assertThrows(DeploymentException.class, () -> containerInitializationLogger.error(cause));

		assertEquals(cause, exception.getCause());
	}

	private static class FakeAnnotationTarget implements AnnotationTarget {
		@Override
		public boolean isDeclaration() {
			return false;
		}

		@Override
		public boolean isType() {
			return false;
		}

		@Override
		public DeclarationInfo asDeclaration() {
			return null;
		}

		@Override
		public Type asType() {
			return null;
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
			return false;
		}

		@Override
		public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
			return false;
		}

		@Override
		public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
			return null;
		}

		@Override
		public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
			return null;
		}

		@Override
		public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
			return null;
		}

		@Override
		public Collection<AnnotationInfo> annotations() {
			return null;
		}

		@Override
		public String toString() {
			return "Fake Annotation Target";
		}
	}

	private static class FakeBeanInfo implements BeanInfo {
		@Override
		public ScopeInfo scope() {
			return null;
		}

		@Override
		public Collection<Type> types() {
			return null;
		}

		@Override
		public Collection<AnnotationInfo> qualifiers() {
			return null;
		}

		@Override
		public ClassInfo declaringClass() {
			return null;
		}

		@Override
		public boolean isClassBean() {
			return false;
		}

		@Override
		public boolean isProducerMethod() {
			return false;
		}

		@Override
		public boolean isProducerField() {
			return false;
		}

		@Override
		public boolean isSynthetic() {
			return false;
		}

		@Override
		public MethodInfo producerMethod() {
			return null;
		}

		@Override
		public FieldInfo producerField() {
			return null;
		}

		@Override
		public boolean isAlternative() {
			return false;
		}

		@Override
		public Integer priority() {
			return null;
		}

		@Override
		public String name() {
			return null;
		}

		@Override
		public DisposerInfo disposer() {
			return null;
		}

		@Override
		public Collection<StereotypeInfo> stereotypes() {
			return null;
		}

		@Override
		public Collection<InjectionPointInfo> injectionPoints() {
			return null;
		}

		@Override
		public String toString() {
			return "Fake bean info";
		}
	}

	private static class FakeObserverInfo implements ObserverInfo {
		@Override
		public Type eventType() {
			return null;
		}

		@Override
		public Collection<AnnotationInfo> qualifiers() {
			return null;
		}

		@Override
		public ClassInfo declaringClass() {
			return null;
		}

		@Override
		public MethodInfo observerMethod() {
			return null;
		}

		@Override
		public ParameterInfo eventParameter() {
			return null;
		}

		@Override
		public BeanInfo bean() {
			return null;
		}

		@Override
		public boolean isSynthetic() {
			return false;
		}

		@Override
		public int priority() {
			return 0;
		}

		@Override
		public boolean isAsync() {
			return false;
		}

		@Override
		public Reception reception() {
			return null;
		}

		@Override
		public TransactionPhase transactionPhase() {
			return null;
		}

		@Override
		public String toString() {
			return "Fake Observer Info";
		}
	}
}