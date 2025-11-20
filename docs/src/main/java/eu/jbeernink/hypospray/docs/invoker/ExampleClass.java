package eu.jbeernink.hypospray.docs.invoker;

import java.util.List;

import jakarta.enterprise.inject.Produces;

public class ExampleClass {

	public void methodOne() {}

	public List<String> methodTwo(int a, Object b) throws Exception {
		return List.of();
	}

	@Produces
	private String methodThree(String name) {
		return "Hello %s".formatted(name);
	}
}
