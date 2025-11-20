package eu.jbeernink.hypospray.core.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
@Target({TYPE})
public @interface ClientProxy {

	@SuppressWarnings("ClassExplicitlyAnnotation")
	class Literal extends AnnotationLiteral<ClientProxy> implements ClientProxy {
		public static final Literal INSTANCE = new Literal();
	}
}
