package eu.jbeernink.hypospray.core.context.spi;

import jakarta.enterprise.context.spi.Contextual;

public sealed interface CreationalContextOwner permits HyposprayCreationalContext, CreationalContextManager {


	/// Register the given creational context as currently owning at least one contextual instance.
	///
	/// @param creationalContext the dependent creational context to register.
	void registerDependentCreationalContext(HyposprayCreationalContext<?> creationalContext);

	/// Unregister the given creational context as owning at least one contextual instance.
	///
	/// @param creationalContext the dependent creational context to unregister.
	void unregisterDependentCreationalContext(HyposprayCreationalContext<?> creationalContext);

	/// Create a new [HyposprayCreationalContext] with the current [CreationalContextOwner] as owner.
	///
	/// @param contextual the contextual to create a [HyposprayCreationalContext] for.
	default <T> HyposprayCreationalContext<T> newCreationalContext(Contextual<T> contextual) {
		return new HyposprayCreationalContext<>(this, contextual);
	}
}
