package eu.jbeernink.hypospray.model.todo;

import static java.lang.System.Logger.Level.WARNING;

import java.util.Set;

@Deprecated
public final class Todo {

	private static final System.Logger logger = System.getLogger(Todo.class.getName());

	private Todo(){}

	@Deprecated
	public static <E> E unimplemented() {
		throw new UnsupportedOperationException("Not yet implemented");
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
}
