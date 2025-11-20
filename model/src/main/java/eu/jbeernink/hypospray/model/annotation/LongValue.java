package eu.jbeernink.hypospray.model.annotation;

public record LongValue(long value) implements AnnotationMemberValue {
	@Override
	public long asLong() {
		return value;
	}
}
