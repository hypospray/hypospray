package eu.jbeernink.hypospray.demo;

import static java.util.stream.Collectors.joining;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ScopedBean implements Runnable {

	@Inject
	private EventSource eventSource;

	@Override
	public void run() {
		System.out.println("Hello application scope!");
		StackWalker.getInstance().forEach(System.err::println);
	}

	@PostConstruct
	void init() {
		String stack = StackWalker.getInstance()
		                          .walk(stackStream -> stackStream.map(Object::toString)
		                                                          .map(s -> "    " + s)
		                                                          .collect(joining("\n")));
		System.out.printf("PostConstruct called on %s%n%s%n", this, stack);
	}

	@PreDestroy
	void preDestroy() {
		String stack = StackWalker.getInstance()
		                          .walk(stackStream -> stackStream.map(Object::toString)
		                                                          .map(s -> "    " + s)
		                                                          .collect(joining("\n")));
		System.out.printf("PreDestroy called on %s%n%s%n", this, stack);
	}

	@Override
	public String toString() {
		return "ScopedBean{" + "eventSource=" + eventSource + '}';
	}
}
