package eu.jbeernink.hypospray.model.annotation;

public record StringValue(String value) implements AnnotationMemberValue {

	@Override
	public String asString() {
		return value;
	}
}
