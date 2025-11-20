package eu.jbeernink.hypospray.discovery.scanner;

/// Filter for filtering out beans from a bean archive based on the archive's bean discovery mode.
public interface BeanDiscoveryFilter {

	/// Returns the [BeanDiscoveryMode] implemented by this filter.
	BeanDiscoveryMode discoveryMode();

	/// Returns if the class with the given name is a valid bean candidate according to the [#discoveryMode()].
	///
	/// @param className The name of the class to check.
	boolean isCandidateClass(String className);
}
