package eu.jbeernink.hypospray.core.invoke;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.invoke.Invoker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactory;

@DisplayName("GeneratedInvokerFactoryManager")
class GeneratedInvokerFactoryManagerTest {

	private final GeneratedInvokerFactoryManager manager = new GeneratedInvokerFactoryManager();

	@Test
	@DisplayName(
			"getInvokerFactory(String className) with a known class name, returns an invoker factory that can generate invokers for a method on that class.")
	void getInvokerFactory_withKnownClassName_returnsInvokerFactoryWhichCanCallMethodsForClass() throws Exception {
		@SuppressWarnings("unchecked") InvokerFactory<String> invokerFactory =
				(InvokerFactory<String>) manager.getInvokerFactory("java.lang.String");

		Invoker<String, Object> invoker = invokerFactory.create("equals[(Ljava/lang/Object;)Z]");
		Object invoke = invoker.invoke("foo", new Object[]{"bar"});
		assertEquals(Boolean.FALSE, invoke);
	}

	@Test
	@DisplayName(
			"getInvokerFactory(String className) with a known class name, returns an invoker factory that can generate invokers for a constructor on that class.")
	void getInvokerFactory_withKnownClassName_returnsInvokerFactoryWhichCanCallConstructorsForClass() throws Exception {
		@SuppressWarnings("unchecked") InvokerFactory<String> invokerFactory =
				(InvokerFactory<String>) manager.getInvokerFactory("java.lang.String");

		Invoker<?, Object> invoker = invokerFactory.create("new[([C)Ljava/lang/String;]");
		Object invoke = invoker.invoke(null, new Object[]{new char[]{'d', 'i'}});
		assertEquals("di", invoke);
	}

	@Test
	@DisplayName("getInvokerFactory(String className) with a known class name, returns an invoker factory that can generate invokers for setting a field on that class.")
	void getInvokerFactory_withKnownClassName_returnsInvokerFactoryWhichCanSetFieldsForClass() throws Exception {
		ClassWithField classWithField = new ClassWithField();

		@SuppressWarnings("unchecked") InvokerFactory<ClassWithField> invokerFactory =
				(InvokerFactory<ClassWithField>) manager.getInvokerFactory(ClassWithField.class.getName());

		Invoker<ClassWithField, Object> invoker = invokerFactory.create("field$$synthetic$$setter[(Ljava/lang/String;)V]");
		var _ = invoker.invoke(classWithField, new Object[]{"test"});
		assertEquals("test", classWithField.field);
	}

	@Test
	@DisplayName("getInvokerFactory(String className) with an unknown class name, throws DeploymentException.")
	void getInvokerFactory_withUnknownClassName_throwsDeploymentException() throws Exception {
		assertThrows(DeploymentException.class, () -> manager.getInvokerFactory("fake.java.lang.String"));
	}
}