package eu.jbeernink.hypospray.model.annotation;

import eu.jbeernink.hypospray.model.types.TypeInstance;

public record TypeValue(TypeInstance type) implements AnnotationMemberValue {

	@Override
	public TypeInstance asType() {
		return type;
	}
}
