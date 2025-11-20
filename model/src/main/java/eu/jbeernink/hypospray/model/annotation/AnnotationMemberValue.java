package eu.jbeernink.hypospray.model.annotation;

import java.util.List;

import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public sealed interface AnnotationMemberValue extends AnnotationMember permits ArrayValue, BooleanValue, ByteValue,
		CharValue, DoubleValue, EnumValue, FloatValue, IntegerValue, LongValue, NestedAnnotationValue,
		ShortValue, StringValue, TypeValue {

	@Override
	default Kind kind() {
		return switch (this) {
			case ArrayValue _ -> Kind.ARRAY;
			case BooleanValue _ -> Kind.BOOLEAN;
			case ByteValue _ -> Kind.BYTE;
			case CharValue _ -> Kind.CHAR;
			case DoubleValue _ -> Kind.DOUBLE;
			case EnumValue _ -> Kind.ENUM;
			case FloatValue _ -> Kind.FLOAT;
			case IntegerValue _ -> Kind.INT;
			case LongValue _ -> Kind.LONG;
			case NestedAnnotationValue _ -> Kind.NESTED_ANNOTATION;
			case ShortValue _ -> Kind.SHORT;
			case StringValue _ -> Kind.STRING;
			case TypeValue _ -> Kind.CLASS;
		};
	}

	@Override
	default boolean asBoolean() {
		throw new IllegalStateException("Value is not a boolean");
	}

	@Override
	default byte asByte() {
		throw new IllegalStateException("Value is not a byte");
	}

	@Override
	default short asShort() {
		throw new IllegalStateException("Value is not a short");
	}

	@Override
	default int asInt() {
		throw new IllegalStateException("Value is not an int");
	}

	@Override
	default long asLong() {
		throw new IllegalStateException("Value is not a long");
	}

	@Override
	default float asFloat() {
		throw new IllegalStateException("Value is not a float");
	}

	@Override
	default double asDouble() {
		throw new IllegalStateException("Value is not a double");
	}

	@Override
	default char asChar() {
		throw new IllegalStateException("Value is not a char");
	}

	@Override
	default String asString() {
		throw new IllegalStateException("Value is not a String");
	}

	@Override
	default <E extends Enum<E>> E asEnum(Class<E> enumType) {
		throw new IllegalStateException("Value is not an enum");
	}

	@Override
	default ClassInfo asEnumClass() {
		throw new IllegalStateException("Value is not an enum");
	}

	@Override
	default String asEnumConstant() {
		throw new IllegalStateException("Value is not an enum");
	}


	@Override
	default AnnotationInformation asNestedAnnotation() {
		throw new IllegalStateException("Value is not a nested annotation");
	}

	@Override
	default List<AnnotationMember> asArray() {
		throw new IllegalStateException("Value is not an array");
	}

	default TypeInstance asType() {
		throw new IllegalArgumentException("Value is not a class type.");
	}
}
