package eu.jbeernink.hypospray.model.annotation;

public record CharValue(char value) implements AnnotationMemberValue{

	@Override
	public char asChar() {
		return value;
	}
}
