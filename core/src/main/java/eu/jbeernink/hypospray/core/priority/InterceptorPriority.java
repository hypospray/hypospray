package eu.jbeernink.hypospray.core.priority;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_AFTER;

/// Static constants for determining the priority of container owned interceptors.
public interface InterceptorPriority {

	/// Starting priority for interceptors enabled for a bean archive.
	int ARCHIVE_ENABLED_INTERCEPTOR_PRIORITY = PLATFORM_AFTER + 1_000_000;
	int DECORATOR_INTERCEPTOR_PRIORITY = Integer.MAX_VALUE;
}
