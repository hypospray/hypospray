package eu.jbeernink.hypospray.model.annotation;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;

public record NestedAnnotationValue(AnnotationInformation value) implements AnnotationMemberValue {
	@Override
	public AnnotationInformation asNestedAnnotation() {
		return value;
	}
}
