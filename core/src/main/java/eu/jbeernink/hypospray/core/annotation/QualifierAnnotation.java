package eu.jbeernink.hypospray.core.annotation;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Map;

import org.jspecify.annotations.NullMarked;

import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;

/// A qualifier annotation with the values for all bindings fields.
@NullMarked
public record QualifierAnnotation(ClassInformation<?> annotation, Map<String, AnnotationMemberValue> bindingMembers) {

	public QualifierAnnotation {
		if (!annotation.isAnnotation()) {
			throw new IllegalArgumentException(String.format("%s is not an annotation.", annotation.name()));
		}

		bindingMembers = Map.copyOf(bindingMembers);
	}

	public static QualifierAnnotation of(AnnotationInformation annotation) {
		var annotationClass = annotation.declaration();

		Map<String, AnnotationMemberValue> bindingMembers = annotationClass.methodInformation()
		                                                                   .stream()
		                                                                   .filter(method -> method.declaringClass()
		                                                                                           .equals(annotationClass))
		                                                                   .map(MethodInformation::name)
		                                                                   .filter(annotation::isBindingMember)
		                                                                   .collect(
				                                                                   toUnmodifiableMap(methodName -> methodName,
						                                                                   annotation::member));

		return new QualifierAnnotation(annotationClass, bindingMembers);
	}
}
