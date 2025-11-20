package eu.jbeernink.hypospray.model.annotation;

public record FloatValue(float value) implements AnnotationMemberValue {

	@Override
	public float asFloat() {
		return value;
	}
}
