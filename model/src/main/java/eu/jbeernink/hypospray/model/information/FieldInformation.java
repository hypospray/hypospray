package eu.jbeernink.hypospray.model.information;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveFieldInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Information about a field in a class.
///
/// ## Equality
/// Two instances of `FieldInformation` are considered equal if they have the same [#declaringClass()] and [#name()].
///
/// ### Hash-code
///
/// The [#hashCode()] of a `FieldInformation` instance should be calculated as follows: {@snippet :
///  Objects.hashCode(fieldInformation.declaringClass(), fieldInformation.name())
///  }
public sealed interface FieldInformation extends FieldInfo, AnnotatedDeclaration, ClassMember permits ReflectiveFieldInformation {

	@Deprecated
		// TODO: #53 - Remove methods directly depending on reflection.
	Field fieldInstance();

	@Override
	default boolean isStatic() {
		return Modifier.isStatic(modifiers());
	}

	@Override
	default boolean isFinal() {
		return Modifier.isFinal(modifiers());
	}

	@Override
	default List<AnnotationInfo> annotations() {
		return List.copyOf(annotationInformation());
	}

	ClassInformation<?> declaringClass();

	TypeInstance type();

	List<AnnotationInformation> annotationInformation();
}
