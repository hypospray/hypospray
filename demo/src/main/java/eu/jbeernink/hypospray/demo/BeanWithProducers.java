package eu.jbeernink.hypospray.demo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import eu.jbeernink.hypospray.demo.annotation.GreetingMessage;

@ApplicationScoped
public class BeanWithProducers {


	@Produces
	@GreetingMessage
	public static String staticProducer() {
		return "Hello World!";
	}
}
