import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;

@NullMarked
module eu.jbeernink.hypospray.invoker {
	requires jakarta.cdi;
	requires org.jspecify;
	requires eu.jbeernink.hypospray.util;

	exports eu.jbeernink.hypospray.invoker.factory to eu.jbeernink.hypospray.core, eu.jbeernink.hypospray.model;

	uses InvokerFactoryManager;
}