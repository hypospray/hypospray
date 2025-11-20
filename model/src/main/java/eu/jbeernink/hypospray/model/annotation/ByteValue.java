package eu.jbeernink.hypospray.model.annotation;

public record ByteValue(byte value) implements AnnotationMemberValue {

	@Override
	public byte asByte() {
		return value;
	}
}
