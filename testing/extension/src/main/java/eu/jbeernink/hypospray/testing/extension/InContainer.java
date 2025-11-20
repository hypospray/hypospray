package eu.jbeernink.hypospray.testing.extension;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Stereotype;

import org.junit.jupiter.api.extension.ExtendWith;

@Stereotype
@Dependent
@ExtendWith(InContainerExtension.class)
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface InContainer {}
