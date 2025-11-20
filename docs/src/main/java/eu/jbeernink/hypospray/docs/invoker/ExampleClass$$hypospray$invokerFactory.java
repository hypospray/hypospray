package eu.jbeernink.hypospray.docs.invoker;

import static java.lang.System.Logger.Level.WARNING;

import java.lang.System.Logger;
import java.lang.reflect.Method;
import java.util.function.Function;

import jakarta.enterprise.invoke.Invoker;

public class ExampleClass$$hypospray$invokerFactory implements Function<String, Invoker<ExampleClass, ?>> {

	private static final Logger logger = System.getLogger(ExampleClass$$hypospray$invokerFactory.class.getName());

	@Override
	public Invoker<ExampleClass, ?> apply(String s) {
		return switch (s) {
			case "hashCode[()I]" -> (instance, _) -> instance.hashCode();
			case "equals[(Ljava/lang/Object;)Z]" -> (instance, arguments) -> instance.equals(arguments[0]);
			case "toString[()Ljava/lang/String;]" -> (instance, _) -> instance.toString();
			case "methodOne[()V]" -> (instance, _) -> {
				instance.methodOne();
				return null;
			};
			case "methodTwo[(ILjava/lang/Object;)Ljava/util/List;]" ->
					(instance, arguments) -> instance.methodTwo((Integer) arguments[0], arguments[1]);
			case "methodThree([Ljava/lang/String;]Ljava/lang/String;]" -> (instance, _) -> {
				logger.log(WARNING,
						"ExampleClass.methodThree() is private, invocation of private methods will fail if reflection is not available at runtime.");
				Method method = ExampleClass.class.getDeclaredMethod("methodThree");
				method.setAccessible(true);
				return method.invoke(instance);
			};
			case String _ -> throw new IllegalArgumentException(
					"No method named %s defined on class %s".formatted(s, ExampleClass.class.getName()));
		};
	}
}
