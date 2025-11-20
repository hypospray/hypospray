package eu.jbeernink.hypospray.core.priority;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;
import static jakarta.interceptor.Interceptor.Priority.PLATFORM_AFTER;
import static jakarta.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

/// Collection of constants to be used to determine the ordering of given container extensions.
public interface ExtensionPriority {

	int APPLICATION_SCOPE_EXTENSION = PLATFORM_BEFORE + 10;
	int REQUEST_SCOPE_EXTENSION = PLATFORM_BEFORE + 20;
	int EVENT_EXTENSION = PLATFORM_BEFORE + 50;

	/// The default priority for extensions that do not explicitly define a priority.
	int DEFAULT_EXTENSION_PRIORITY = APPLICATION + 500;

	int BEAN_DISCOVERY_EXTENSION = PLATFORM_AFTER + 5;
}
