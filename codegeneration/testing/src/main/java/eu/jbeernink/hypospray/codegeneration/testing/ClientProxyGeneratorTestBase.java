package eu.jbeernink.hypospray.codegeneration.testing;

import static eu.jbeernink.hypospray.util.stream.Collectors.findOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxy;
import eu.jbeernink.hypospray.codegeneration.generator.proxy.ClientProxyConfiguration;
import eu.jbeernink.hypospray.codegeneration.generator.spi.ClientProxyGenerator;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

public abstract class ClientProxyGeneratorTestBase {

	private ClientProxyGenerator clientProxyGenerator;
	private MethodHandles.Lookup privateLookup;

	protected abstract String clientProxyFactoryName();

	protected ClientProxyGenerator createClientProxy() {
		return ServiceLoader.load(ClientProxyGenerator.class)
		                    .stream()
		                    .filter(clientProxyFactoryProvider -> clientProxyFactoryProvider.type()
		                                                                                    .getName()
		                                                                                    .equals(
				                                                                                    clientProxyFactoryName()))
		                    .collect(findOnly(RuntimeException::new))
		                    .map(ServiceLoader.Provider::get)
		                    .orElseThrow();
	}

	@BeforeEach
	void setup() throws IllegalAccessException {
		clientProxyGenerator = createClientProxy();
		privateLookup = MethodHandles.privateLookupIn(ProxiedClass.class, MethodHandles.lookup());
	}

	@Nested
	@DisplayName("with single proxied type")
	class WithSingleProxiedType {

		private ClientProxyConfiguration clientProxyConfiguration;
		private String classNameSuffix;

		@BeforeEach
		void setup() {
			// Add a unique suffix to the test class name to avoid running into errors because of duplicate class name definitions.
			classNameSuffix = "" + System.nanoTime();
			var proxyName = "eu.jbeernink.hypospray.codegeneration.testing.TestProxyName" + classNameSuffix;
			clientProxyConfiguration =
					new ClientProxyConfiguration(ProxiedClass.class, new ReflectiveClassInformation<>(ProxiedClass.class),
							proxyName);
		}

		@Test
		@DisplayName("createClientProxy(ClientProxyConfiguration) creates a valid proxy class.")
		void createClientProxy_createsValidProxyClass() throws Exception {
			ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

			privateLookup.defineClass(clientProxy.classData());
		}

		@Test
		@DisplayName("createClientProxy(ClientProxyConfiguration) creates class with the expected name.")
		void createClientProxy_createsClassWithExpectedName() throws Exception {
			ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

			Class<?> definedClass = privateLookup.defineClass(clientProxy.classData());
			assertEquals("eu.jbeernink.hypospray.codegeneration.testing.TestProxyName" + classNameSuffix,
					definedClass.getName());
		}

		@Test
		@DisplayName("createClientProxy(ClientProxyConfiguration) creates client proxy with the expected constructor.")
		void createClientProxy_createsClientProxyWithExpectedConstructor() throws Exception {
			ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

			Class<?> definedClass = privateLookup.defineClass(clientProxy.classData());
			try {
				assertNotNull(definedClass.getConstructor(Function.class));
			} catch (NoSuchMethodException e) {
				fail("Proxy class does not have expected constructors, expected (Function), but found: " +
				     Arrays.toString(definedClass.getConstructors()));
			}
		}

		@Test
		@DisplayName("createClientProxy(ClientProxyConfiguration) creates client proxy class with the expected supertype.")
		void createClientProxy_createsClientProxyClassWithExpectedSuperType() throws Exception {
			ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

			Class<?> definedClass = privateLookup.defineClass(clientProxy.classData());
			assertTrue(ProxiedClass.class.isAssignableFrom(definedClass));
		}

		@Nested
		@DisplayName("createClientProxy(ClientProxyConfiguration) creates client proxy class where")
		class CreatesClientProxyClass {

			private ProxiedClass proxy;

			private final Map<String, Invoker<?, ?>> invokers = new HashMap<>();

			@BeforeEach
			void setup() throws Exception {
				Function<String, Invoker<?, ?>> invoker = invokers::get;
				invokers.put("toString[()Ljava/lang/String;]", (_, _) -> "proxy class");
				ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

				Class<?> proxyClass = privateLookup.defineClass(clientProxy.classData());
				proxy = (ProxiedClass) proxyClass.getConstructor(Function.class).newInstance(invoker);
			}

			@Test
			@DisplayName("calling a method returns the result of calling corresponding invoker.")
			void callingProxyMethod_returnsResultsOfCallingTheInvocationContext() {
				var expectedResult = "invocation";
				invokers.put("getResult[()Ljava/lang/String;]", (_, _) -> expectedResult);

				String result = proxy.getResult();

				assertEquals(expectedResult, result);
			}

			@Test
			@DisplayName("calling an inherited method returns the result of calling the invoker.")
			void callingInheritedMethod_returnsResultOfCallingTheInvocationContext() {
				int hashCode = 12345;
				invokers.put("hashCode[()I]", (_, _) -> hashCode);

				int result = proxy.hashCode();

				assertEquals(hashCode, result);
			}

			@Test
			@DisplayName("calling a method that throws a declared exception, rethrows that exception.")
			void callingMethodThrowingDeclaredException_rethrowsException() {
				var exceptionMessage = "This is a test";
				invokers.put("throwingMethod[()V]", (_, _) -> {throw new IOException(exceptionMessage);});

				var exception = assertThrows(IOException.class, () -> proxy.throwingMethod());

				assertEquals(exceptionMessage, exception.getMessage());
			}

			@Test
			@DisplayName("calling a method that throws an undeclared unchecked exception, rethrows that exception.")
			void callingMethodThrowingUndeclaredUncheckedException_rethrowsException() {
				var exceptionMessage = "This is another test";
				invokers.put("throwingMethod[()V]", (_, _) -> {throw new IllegalArgumentException(exceptionMessage);});

				var exception = assertThrows(IllegalArgumentException.class, () -> proxy.throwingMethod());

				assertEquals(exceptionMessage, exception.getMessage());
			}

			@Test
			@DisplayName("calling a method that returns a long, correctly returns the long returned by the invoker.")
			void callingMethodThatReturnsLong_correctlyReturnsInvocationContextValue() {
				long expectedValue = 42L;
				invokers.put("longMethod[()J]", (_, _) -> expectedValue);

				long result = proxy.longMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a boolean, correctly returns the boolean returned by the invoker.")
			void callingMethodThatReturnsBoolean_correctlyReturnsInvocationContextValue() {
				boolean expectedValue = false;
				invokers.put("booleanMethod[()Z]", (_, _) -> expectedValue);

				boolean result = proxy.booleanMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a byte, correctly returns the byte returned by the invoker.")
			void callingMethodThatReturnsByte_correctlyReturnsInvocationContextValue() {
				byte expectedValue = 42;
				invokers.put("byteMethod[()B]", (_, _) -> expectedValue);

				byte result = proxy.byteMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a short, correctly returns the short from the InvocationContext.")
			void callingMethodThatReturnsShort_correctlyReturnsInvocationContextValue() {
				short expectedValue = 256;
				invokers.put("shortMethod[()S]", (_, _) -> expectedValue);

				short result = proxy.shortMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a char, correctly returns the char returned by the invoker.")
			void callingMethodThatReturnsChar_correctlyReturnsInvocationContextValue() {
				char expectedValue = 'Q';
				invokers.put("charMethod[()C]", (_, _) -> expectedValue);

				char result = proxy.charMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a int, correctly returns the int returned by the invoker.")
			void callingMethodThatReturnsInt_correctlyReturnsInvocationContextValue() {
				int expectedValue = 52;
				invokers.put("intMethod[()I]", (_, _) -> expectedValue);

				int result = proxy.intMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a float, correctly returns the float returned by the invoker.")
			void callingMethodThatReturnsFloat_correctlyReturnsInvocationContextValue() {
				float expectedValue = 3.452f;
				invokers.put("floatMethod[()F]", (_, _) -> expectedValue);

				float result = proxy.floatMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method that returns a double, correctly returns the double returned by the invoker.")
			void callingMethodThatReturnsDouble_correctlyReturnsInvocationContextValue() {
				double expectedValue = 42.42;
				invokers.put("doubleMethod[()D]", (_, _) -> expectedValue);

				double result = proxy.doubleMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName(
					"calling a method that returns a reference, correctly returns the reference returned by the invoker.")
			void callingMethodThatReturnsReference_correctlyReturnsInvocationContextValue() {
				String expectedValue = "this is a test";
				invokers.put("referenceMethod[()Ljava/lang/String;]", (_, _) -> expectedValue);

				String result = proxy.referenceMethod();

				assertEquals(expectedValue, result);
			}

			@Test
			@DisplayName("calling a method with an int parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnIntParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("intParameterMethod[(I)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.intParameterMethod(5);

				assertEquals(5, parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an boolean parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnBooleanParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("booleanParameterMethod[(Z)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.booleanParameterMethod(true);

				assertEquals(true, parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an byte parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnByteParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("byteParameterMethod[(B)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.byteParameterMethod((byte) 42);

				assertEquals((byte) 42, parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an short parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnShortParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("shortParameterMethod[(S)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.shortParameterMethod((short) 42);

				assertEquals((short) 42, parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an char parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnCharParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("charParameterMethod[(C)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.charParameterMethod('x');

				assertEquals('x', parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an long parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnLongParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("longParameterMethod[(J)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.longParameterMethod(1_000_000_000_000L);

				assertEquals(1_000_000_000_000L, parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an float parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnFloatParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("floatParameterMethod[(F)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.floatParameterMethod(3.14f);

				assertEquals(3.14f, parameterValue.get());
			}

			@Test
			@DisplayName("calling a method with an double parameter, correctly passes that parameter to the invoker.")
			void callingMethodWithAnDoubleParameter_correctlyPassesParameterToInvoker() {
				AtomicReference<Object> parameterValue = new AtomicReference<>();
				invokers.put("doubleParameterMethod[(D)V]", (_, parameters) -> {
					parameterValue.set(parameters[0]);
					return null;
				});

				proxy.doubleParameterMethod(2.2);

				assertEquals(2.2, parameterValue.get());
			}
		}

		@Nested
		@DisplayName("with default constructor")
		class WithDefaultConstructor {

			public static class ProxiedClassWithDefaultConstructor {

				public ProxiedClassWithDefaultConstructor() {
				}

				public ProxiedClassWithDefaultConstructor(String string) {
					throw new UnsupportedOperationException("Incorrect constructor called");
				}

			}

			@Test
			@DisplayName("calling the proxy constructor calls the correct superclass constructor.")
			void new_callsCorrectSuperclassConstructor() throws Exception {
				// Add a unique suffix to the test class name to avoid running into errors because of duplicate class name definitions.
				classNameSuffix = "" + System.nanoTime();
				var proxyName =
						"eu.jbeernink.hypospray.codegeneration.testing.TestProxyNameWithDefaultConstructor" + classNameSuffix;
				clientProxyConfiguration = new ClientProxyConfiguration(ProxiedClassWithDefaultConstructor.class,
						new ReflectiveClassInformation<>(ProxiedClassWithDefaultConstructor.class), proxyName);

				ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

				Class<?> definedClass = privateLookup.defineClass(clientProxy.classData());
				assertNotNull(
						definedClass.getConstructor(Function.class).newInstance((Function<String, Invoker<?, ?>>) s -> null));
			}
		}

		@Nested
		@DisplayName("without default constructor, but with @Inject constructor")
		class WithoutDefaultConstructor {

			public static class ProxiedClassWithoutDefaultConstructor {
				@Inject
				public ProxiedClassWithoutDefaultConstructor(String string) {
				}

				public ProxiedClassWithoutDefaultConstructor(Object o) {
				}
			}

			@Test
			@DisplayName("calling the proxy constructor calls the correct superclass constructor.")
			void new_callsCorrectSuperclassConstructor() throws Exception {
				// Add a unique suffix to the test class name to avoid running into errors because of duplicate class name definitions.
				classNameSuffix = "" + System.nanoTime();
				var proxyName =
						"eu.jbeernink.hypospray.codegeneration.testing.TestProxyNameWithoutDefaultConstructor" + classNameSuffix;
				clientProxyConfiguration = new ClientProxyConfiguration(ProxiedClassWithoutDefaultConstructor.class,
						new ReflectiveClassInformation<>(ProxiedClassWithoutDefaultConstructor.class), proxyName);

				ClientProxy clientProxy = clientProxyGenerator.createClientProxy(clientProxyConfiguration);

				Class<?> definedClass = privateLookup.defineClass(clientProxy.classData());
				assertNotNull(
						definedClass.getConstructor(Function.class).newInstance((Function<String, Invoker<?, ?>>) s -> null));
			}
		}

		@Nested
		@DisplayName("with a private constructor")
		class WithPrivateConstructor {

			public static class ProxiedClassWithPrivateConstructor {
				private ProxiedClassWithPrivateConstructor() {
				}
			}

			@Test
			@DisplayName("createClientProxy(ClientProxyConfiguration) throw an IllegalArgumentException.")
			void createClientProxy_throwsIllegalArgumentException() throws Exception {
				// Add a unique suffix to the test class name to avoid running into errors because of duplicate class name definitions.
				classNameSuffix = "" + System.nanoTime();
				var proxyName =
						"eu.jbeernink.hypospray.codegeneration.testing.ProxiedClassWithPrivateConstructor" + classNameSuffix;
				clientProxyConfiguration = new ClientProxyConfiguration(WithoutDefaultConstructor.class,
						new ReflectiveClassInformation<>(WithoutDefaultConstructor.class), proxyName);

				var exception = assertThrows(IllegalArgumentException.class,
						() -> clientProxyGenerator.createClientProxy(clientProxyConfiguration));

				assertEquals("Cannot create a proxy for " +
				             "eu.jbeernink.hypospray.codegeneration.testing.ClientProxyGeneratorTestBase$WithSingleProxiedType$WithoutDefaultConstructor. " +
				             "Class does not have a public, protected or package friendly no-argument constructor or constructor marked with @Inject.",
						exception.getMessage());
			}
		}
	}


}