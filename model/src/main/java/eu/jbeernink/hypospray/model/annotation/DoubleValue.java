package eu.jbeernink.hypospray.model.annotation;

public record DoubleValue(double value) implements AnnotationMemberValue {

	@Override
	public double asDouble() {
		return value;
	}
}
