package eu.jbeernink.hypospray.model.types;

import static eu.jbeernink.hypospray.model.todo.Todo.warnNotYetImplemented;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.List;
import java.util.function.Predicate;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.types.PrimitiveType.PrimitiveKind;
import jakarta.enterprise.lang.model.types.Type;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;

/// An instance of a Java type.
public sealed interface TypeInstance extends Type permits ArrayTypeInstance, ClassTypeInstance,
		ParameterizedTypeInstance, PrimitiveTypeInstance, TypeVariableInstance, VoidTypeInstance, WildcardTypeInstance {

	java.lang.reflect.Type asJavaType();

	List<AnnotationInformation> typeAnnotations();

	@Override
	default List<AnnotationInfo> annotations() {
		return List.copyOf(typeAnnotations());
	}

	@Override
	default ClassTypeInstance asClass() {
		throw new IllegalStateException("Not a class.");
	}

	@Override
	default VoidTypeInstance asVoid() {
		throw new IllegalStateException("Not a void");
	}

	@Override
	default PrimitiveTypeInstance asPrimitive() {
		throw new IllegalStateException("Not a primitive");
	}

	@Override
	default ArrayTypeInstance asArray() {
		throw new IllegalStateException("Not an array");
	}

	@Override
	default ParameterizedTypeInstance asParameterizedType() {
		throw new IllegalStateException("Not a parameterized type");
	}

	@Override
	default TypeVariableInstance asTypeVariable() {
		throw new IllegalStateException("Not a type variable");
	}

	@Override
	default boolean hasAnnotation(Class<? extends Annotation> annotationType) {
		return annotations().stream().anyMatch(annotation -> matchesAnnotationType(annotationType, annotation));
	}

	private static boolean matchesAnnotationType(Class<? extends Annotation> annotationType, AnnotationInfo annotation) {
		return annotationType.getName().equals(annotation.name());
	}

	@Override
	default boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
		return annotations().stream().anyMatch(predicate);
	}

	@Override
	default <T extends Annotation> @Nullable AnnotationInfo annotation(Class<T> annotationType) {
		return annotations().stream()
		                    .filter(annotation -> matchesAnnotationType(annotationType, annotation))
		                    .findFirst()
		                    .orElse(null);
	}

	@Override
	default <T extends Annotation> List<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
		if (annotationType.isAnnotationPresent(Repeatable.class)) {
			Class<? extends Annotation> wrapperClass = annotationType.getAnnotation(Repeatable.class).value();

			return typeAnnotations().stream()
			                        .filter(annotation -> matchesAnnotationType(annotationType, annotation) ||
			                                              matchesAnnotationType(wrapperClass, annotation))
			                        .map(annotation -> {
				                        if (matchesAnnotationType(wrapperClass, annotation)) {
					                        return annotation.value()
					                                         .asArray()
					                                         .stream()
					                                         .map(AnnotationMember::asNestedAnnotation)
					                                         .toList();
				                        }
				                        return List.<AnnotationInfo>of(annotation);
			                        })
			                        .findFirst()
			                        .orElseGet(List::of);
		}

		return annotations().stream().filter(annotation -> matchesAnnotationType(annotationType, annotation)).toList();
	}

	@Override
	default List<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
		return annotations().stream().filter(predicate).toList();
	}

	default String descriptorString() {
		return switch (this) {
			case ArrayTypeInstance(var componentType, _) -> "[%s".formatted(componentType.descriptorString());
			case ClassTypeInstance classTypeInstance ->
					"L%s;".formatted(classTypeInstance.declaration().name().replace('.', '/'));
			case ParameterizedTypeInstance(TypeInstance rawTypeInstance, _, _) -> rawTypeInstance.descriptorString();
			case PrimitiveTypeInstance(PrimitiveKind primitiveKind, _) -> switch (primitiveKind) {
				case PrimitiveKind.BYTE -> "B";
				case PrimitiveKind.SHORT -> "S";
				case PrimitiveKind.CHAR -> "C";
				case PrimitiveKind.INT -> "I";
				case PrimitiveKind.LONG -> "J";
				case PrimitiveKind.FLOAT -> "F";
				case PrimitiveKind.DOUBLE -> "D";
				case PrimitiveKind.BOOLEAN -> "Z";
			};
			case TypeVariableInstance _ -> {
				warnNotYetImplemented();
				yield "Ljava/lang/Object";
			}
			case VoidTypeInstance.VOID -> "V";
			case WildcardTypeInstance _ -> {
				warnNotYetImplemented();
				yield "Ljava/lang/Object";
			}
		};
	}

	boolean isAssignableFrom(TypeInstance other);

	/// Create a copy of this [TypeInstance] with the given type annotations.
	///
	/// @param annotations a list of annotations to use in the copy.
	/// @return a copy of this type instance with the given annotations.
	TypeInstance withTypeAnnotations(List<AnnotationInformation> annotations);
}
