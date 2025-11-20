package eu.jbeernink.hypospray.docs.invoker;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

public class Application {

	public static void main(String[] args) {
		// tag::enableWarning[]
		try (SeContainer container = SeContainerInitializer.newInstance()
		                                                   .addProperty(
				                                                   "eu.jbeernink.hypospray.invoker.warnOnPrivateMemberInvocation",
				                                                   true)
		                                                   .initialize()) {
			// run application
		}
		// end::enableWarning[]

		// tag::disablePrivateInvoker[]
		try (SeContainer container = SeContainerInitializer.newInstance()
		                                                   .addProperty(
				                                                   "eu.jbeernink.hypospray.invoker.generateForPrivateMembers",
				                                                   false)
		                                                   .initialize()) {
			// run application
		}
		// end::disablePrivateInvoker[]
	}


}
