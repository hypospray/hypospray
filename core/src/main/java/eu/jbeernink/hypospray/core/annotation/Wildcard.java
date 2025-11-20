package eu.jbeernink.hypospray.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/// Wildcard qualifier annotation that matches any set of qualifier annotations at bean resolution time.
///
/// This can be considered the bean/producer method equivalent of annotating an injection point with [Any][jakarta.enterprise.inject.Any].
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Wildcard {

	@SuppressWarnings("ClassExplicitlyAnnotation")
	class Literal extends AnnotationLiteral<Wildcard> implements Wildcard {
		public static final Literal INSTANCE = new Literal();
	}
}
