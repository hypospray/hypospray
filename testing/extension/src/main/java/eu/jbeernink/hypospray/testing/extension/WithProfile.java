package eu.jbeernink.hypospray.testing.extension;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/// Indicate that a given test method or class should be run against the CDI Lite profile.
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@ExtendWith(InContainerExtension.class)
public @interface WithProfile {
	enum Profile {LITE, FULL_SE, FULL_EE}

	Profile[] value();
}
