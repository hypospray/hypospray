package eu.jbeernink.hypospray.model.annotation;

public record IntegerValue(int value) implements AnnotationMemberValue {
	@Override
	public int asInt() {
		return value;
	}
}
