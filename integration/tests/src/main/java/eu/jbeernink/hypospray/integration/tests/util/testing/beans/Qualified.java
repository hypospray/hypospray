package eu.jbeernink.hypospray.integration.tests.util.testing.beans;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Target({TYPE, METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface Qualified {

	@SuppressWarnings("ClassExplicitlyAnnotation")
	final class Literal extends AnnotationLiteral<Qualified> implements Qualified {
		public static final Literal INSTANCE = new Literal();
	}
}
