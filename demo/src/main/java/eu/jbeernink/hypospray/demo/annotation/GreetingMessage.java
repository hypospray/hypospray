package eu.jbeernink.hypospray.demo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface GreetingMessage {

	class Literal extends AnnotationLiteral<GreetingMessage> implements GreetingMessage {

		public static final Literal INSTANCE = new Literal();

		private Literal(){}

	}
}
