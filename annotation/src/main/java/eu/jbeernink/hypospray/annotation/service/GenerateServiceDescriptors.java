package eu.jbeernink.hypospray.annotation.service;

import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/// Annotation to indicate that service descriptor files should be generated for all services in a specific module.
@Retention(CLASS)
@Target(MODULE)
public @interface GenerateServiceDescriptors {}
