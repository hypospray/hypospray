package eu.jbeernink.hypospray.core.todo;

import static java.lang.System.Logger.Level.WARNING;

import java.util.Set;

public final class Todo {

	private static final System.Logger logger = System.getLogger(Todo.class.getName());

	@Deprecated
	public static void todo(String message) {
		throw new UnsupportedOperationException("TODO: " + message);
	}

	@Deprecated
	public static void warnNotYetImplemented() {
		String frame = StackWalker.getInstance(Set.of())
		                          .walk(frames -> frames.skip(1)
		                                                .map(f -> String.format("%s.%s", f.getClassName(),
				                                                f.getMethodName()))
		                                                .findFirst()
		                                                .orElseThrow());


		logger.log(WARNING, "Not yet implemented: " + frame);
	}

	@Deprecated
	public static void warnTodo(String message) {
		String frame = StackWalker.getInstance(Set.of())
		                          .walk(frames -> frames.skip(1)
		                                                .map(f -> String.format("%s.%s", f.getClassName(),
				                                                f.getMethodName()))
		                                                .findFirst()
		                                                .orElseThrow());


		logger.log(WARNING, frame + ": " + message);
	}

	@Deprecated
	public static <E> E unimplemented() {
		todo("Not yet implemented");
		
		return null;
	}

	private Todo() {
	}
}
