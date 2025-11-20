package eu.jbeernink.hypospray.model.annotation;

public record BooleanValue(boolean value) implements AnnotationMemberValue {
	@Override
	public boolean asBoolean() {
		return value;
	}
}
