package eu.jbeernink.hypospray.core.inject.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.InjectionPoint;

import eu.jbeernink.hypospray.core.annotation.ProxyCreationContext;
import eu.jbeernink.hypospray.core.annotation.QualifierAnnotation;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveFieldInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveParameterInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Injection point defined by a constructor, method or field.
///
/// This class is the internal representation of an injection point, with some accessors that aren't publicly available.
public sealed interface InternalInjectionPoint extends InjectionPoint permits ConstructorInjectionPoint,
		FieldInjectionPoint, MethodInjectionPoint {

	/// {@return the type of the injection point.}
	TypeInstance type();

	@Override
	default Type getType() {
		return type().asJavaType();
	}

	/// {@return an immutable [Set] containing the qualifier annotations as defined on this injection point.}
	Set<AnnotationInformation> qualifierAnnotations();

	/// {@return an immutable [Set] containing the [QualifierAnnotation] instances for this injection point.}
	default Set<QualifierAnnotation> qualifiers() {
		return qualifierAnnotations().stream().map(QualifierAnnotation::of).collect(toUnmodifiableSet());
	}

	@Override
	default Set<Annotation> getQualifiers() {
		return qualifierAnnotations().stream().map(annotation -> switch (annotation) {
			case ReflectiveAnnotationInformation<?>(Annotation a) -> a;
			case SyntheticAnnotationInformation(ClassInformation<? extends Annotation> declaration, _) when declaration.name().equals(
					Default.class.getName()) -> Default.Literal.INSTANCE; // TODO move logic to a helper class to create annotation constructors.
			case SyntheticAnnotationInformation(ClassInformation<? extends Annotation> declaration, _) when declaration.name().equals(
					ProxyCreationContext.class.getName()) -> ProxyCreationContext.Literal.INSTANCE;
			case AnnotationInformation _ -> unimplemented();
		}).collect(toUnmodifiableSet());
	}

	@Override
	default Member getMember() {
		return switch (this) {
			case MethodInjectionPoint(_, _, ReflectiveParameterInformation(Parameter parameter), _, _) ->
					parameter.getDeclaringExecutable();
			case FieldInjectionPoint(_, _, ReflectiveFieldInformation(Field field), _) -> field;
			case ConstructorInjectionPoint(_, _, _, ReflectiveParameterInformation(Parameter parameter), _) ->
					parameter.getDeclaringExecutable();
		};
	}

	@Override
	default Annotated getAnnotated() {
		return unimplemented();
	}
}
