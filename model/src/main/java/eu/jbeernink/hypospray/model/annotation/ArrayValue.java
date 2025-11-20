package eu.jbeernink.hypospray.model.annotation;

import java.util.List;

import jakarta.enterprise.lang.model.AnnotationMember;

public record ArrayValue(List<AnnotationMemberValue> values) implements AnnotationMemberValue {
	public ArrayValue {
		values = List.copyOf(values);
	}

	@Override
	public List<AnnotationMember> asArray() {
		return List.copyOf(values);
	}
}
