package eu.jbeernink.hypospray.integration.tests.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.integration.tests.util.testing.beans.scopes.ApplicationScopedBean;
import eu.jbeernink.hypospray.testing.extension.InContainer;
import jdk.jfr.Name;

@InContainer
@Name("An @ApplicationScoped bean")
public class ApplicationScopedBeanTest {

	@Inject
	private ApplicationScopedBean bean;

	@Test
	@Named("should dispatch method calls to the actual bean.")
	public void shouldDispatchMethodCallsToActualBean() {
		int result = bean.multiply(5, 6);

		assertEquals(30, result);
	}

	@Test
	@Named("should dispatch method calls always to the same instance.")
	public void shouldDispatchMethodCallsToSameInstance() {
		long identityHash = bean.identityHash();

		long identityHash2 = bean.identityHash();

		assertEquals(identityHash, identityHash2);
	}

}
