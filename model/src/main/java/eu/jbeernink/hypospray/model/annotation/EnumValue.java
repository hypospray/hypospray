package eu.jbeernink.hypospray.model.annotation;

import eu.jbeernink.hypospray.model.information.ClassInformation;

public record EnumValue(ClassInformation<?> enumClassInformation, Enum<?> value) implements AnnotationMemberValue {

	@Override
	public <E extends Enum<E>> E asEnum(Class<E> enumType) {
		return enumType.cast(value);
	}

	@Override
	public ClassInformation<?> asEnumClass() {
		return enumClassInformation;
	}

	@Override
	public String asEnumConstant() {
		return value.name();
	}
}
