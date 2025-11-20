package eu.jbeernink.hypospray.core.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/// Annotation bound to dependencies injected into a proxy at instance creation time.
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, PARAMETER})
public @interface ProxyCreationContext {

	/// A literal instance of [ProxyCreationContext].
	@SuppressWarnings("ClassExplicitlyAnnotation")
	final class Literal extends AnnotationLiteral<ProxyCreationContext> implements ProxyCreationContext {
		public static final Literal INSTANCE = new Literal();
	}
}
