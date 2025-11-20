package eu.jbeernink.hypospray.integration.tests.util.testing.beans;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Stereotype;

/// Helper annotation for helping discover beans which do not have bean defining annotations themselves. For example,
/// [Singleton][jakarta.inject.Singleton] is not a bean-defining annotation according to the CDI spec.
@Stereotype
@Retention(RUNTIME)
@Target({TYPE})
public @interface DefinedBean {

}
