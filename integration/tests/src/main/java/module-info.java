module eu.jbeernink.hypospray.integration.tests.util {
	requires java.logging;

	requires org.junit.jupiter.api;
	requires jakarta.cdi;

	exports eu.jbeernink.hypospray.integration.tests.util.testing.beans;
	exports eu.jbeernink.hypospray.integration.tests.util.testing.beans.scopes;
	exports eu.jbeernink.hypospray.integration.tests.util.testing.beans.lifecycle;
	opens eu.jbeernink.hypospray.integration.tests.util.testing.beans.lifecycle;
	opens eu.jbeernink.hypospray.integration.tests.util.testing.beans.scopes to eu.jbeernink.hypospray.core;
}