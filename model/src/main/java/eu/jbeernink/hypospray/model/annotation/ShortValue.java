package eu.jbeernink.hypospray.model.annotation;

public record ShortValue(short value) implements AnnotationMemberValue {

	@Override
	public short asShort() {
		return value;
	}
}
