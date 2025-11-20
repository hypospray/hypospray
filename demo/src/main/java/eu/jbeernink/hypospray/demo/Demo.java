package eu.jbeernink.hypospray.demo;

import static java.util.logging.Level.OFF;

import java.util.logging.Logger;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.BeanContainer;

import eu.jbeernink.hypospray.demo.annotation.GreetingMessage;
import eu.jbeernink.hypospray.demo.qualifier.Qualified;
import eu.jbeernink.hypospray.demo.synthetic.SyntheticBean;

public class Demo {

	public static void main(String[] args) {
		// Disable todo logging for testing purposes.
		Logger.getLogger("eu.jbeernink.hypospray.core.todo.Todo").setLevel(OFF);

		try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
			System.out.println(container);

			System.out.println(container.getBeanContainer().getBeans(UnscopedBean.class));
			var unscopedBean = container.select(UnscopedBean.class).get();
			System.out.println(unscopedBean);

			var eventSource = container.select(EventSource.class).get();
			System.out.println(eventSource);

			var qualifiedBean = container.select(Qualified.Literal.INSTANCE).get();
			System.out.println(qualifiedBean);

			var beanWithDependencies = container.select(BeanWithDependencies.class).get();
			System.out.println(beanWithDependencies);

			var scopedBean = container.select(ScopedBean.class).get();
			System.out.println(scopedBean);
			scopedBean.run();

			System.out.println(container.select(BeanContainer.class).get());

			System.out.println(container.select(String.class, SyntheticBean.Literal.INSTANCE).get());

			System.out.println(container.select(String.class, GreetingMessage.Literal.INSTANCE).get());
		}
	}

}
