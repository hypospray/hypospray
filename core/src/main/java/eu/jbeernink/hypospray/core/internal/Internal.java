package eu.jbeernink.hypospray.core.internal;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/// Qualifier annotation to mark contextuals that should only be available within the DI container itself.
@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, PARAMETER})
public @interface Internal {

	/// Helper class for providing a literal for the [Internal] annotation.
	@SuppressWarnings("ClassExplicitlyAnnotation")
	class Literal extends AnnotationLiteral<Internal> implements Internal {
		public static final Literal INSTANCE = new Literal();
	}
}
