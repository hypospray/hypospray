package eu.jbeernink.hypospray.model.information;

import java.lang.annotation.Annotation;
import java.util.Map;

import jakarta.annotation.Nullable;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.util.Nonbinding;

import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;

/// Information about an annotation.
///
/// ## Equality
/// Two instances of [AnnotationInformation] are considered equal if they refer to the same declaring class and
/// have the same member values.
public sealed interface AnnotationInformation extends AnnotationInfo permits ReflectiveAnnotationInformation,
		SyntheticAnnotationInformation {

	@Deprecated
		// TODO: #53 - Remove methods directly depending on reflection.
	Annotation annotationInstance();

	ClassInformation<? extends Annotation> declaration();

	Map<String, AnnotationMemberValue> memberValues();

	@Override
	default boolean hasMember(String name) {
		return memberValues().containsKey(name);
	}

	default boolean isBindingMember(String name) {
		if (!hasMember(name)) {
			throw new IllegalArgumentException("Annotation %s has no member named '%s'".formatted(declaration().name(), name));
		}

		return declaration().methodInformation()
		                    .stream()
		                    .filter(member -> member.name().equals(name))
		                    .findFirst()
		                    .map(member -> !member.hasAnnotation(Nonbinding.class))
		                    .orElseThrow();
	}

	@Override
	default @Nullable AnnotationMemberValue member(String name) {
		return memberValues().get(name);
	}

	@Override
	default Map<String, AnnotationMember> members() {
		return Map.copyOf(memberValues());
	}


	@Override
	default AnnotationMemberValue value() {
		return memberValues().get(AnnotationMember.VALUE);
	}

	static AnnotationInformation fromAnnotationInfo(AnnotationInfo annotationInfo) {
		return switch (annotationInfo) {
			case AnnotationInformation annotationInformation -> annotationInformation;
			case AnnotationInfo _ -> throw new IllegalArgumentException(
					"Only AnnotationInfo instances created by the AnnotationBuilder class are supported: " + annotationInfo);
		};
	}
}
