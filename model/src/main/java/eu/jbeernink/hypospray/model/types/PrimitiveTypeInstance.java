package eu.jbeernink.hypospray.model.types;

import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.PrimitiveType;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;

public record PrimitiveTypeInstance(PrimitiveKind primitiveKind, List<AnnotationInformation> typeAnnotations) implements
		TypeInstance, PrimitiveType {
	public static final PrimitiveTypeInstance BOOLEAN = new PrimitiveTypeInstance(PrimitiveKind.BOOLEAN, List.of());
	public static final PrimitiveTypeInstance BYTE = new PrimitiveTypeInstance(PrimitiveKind.BYTE, List.of());
	public static final PrimitiveTypeInstance SHORT = new PrimitiveTypeInstance(PrimitiveKind.SHORT, List.of());
	public static final PrimitiveTypeInstance INT = new PrimitiveTypeInstance(PrimitiveKind.INT, List.of());
	public static final PrimitiveTypeInstance LONG = new PrimitiveTypeInstance(PrimitiveKind.LONG, List.of());
	public static final PrimitiveTypeInstance FLOAT = new PrimitiveTypeInstance(PrimitiveKind.FLOAT, List.of());
	public static final PrimitiveTypeInstance DOUBLE = new PrimitiveTypeInstance(PrimitiveKind.DOUBLE, List.of());
	public static final PrimitiveTypeInstance CHAR = new PrimitiveTypeInstance(PrimitiveKind.CHAR, List.of());

	public PrimitiveTypeInstance {
		typeAnnotations = List.copyOf(typeAnnotations);
	}

	@Override
	public String name() {
		return asJavaType().getName();
	}

	@Override
	public Class<?> asJavaType() {
		return switch (this.primitiveKind) {
			case BOOLEAN -> Boolean.TYPE;
			case BYTE -> Byte.TYPE;
			case SHORT -> Short.TYPE;
			case INT -> Integer.TYPE;
			case LONG -> Long.TYPE;
			case FLOAT -> Float.TYPE;
			case DOUBLE -> Double.TYPE;
			case CHAR -> Character.TYPE;
		};
	}

	@Override
	public PrimitiveTypeInstance asPrimitive() {
		return this;
	}

	@Override
	public boolean isAssignableFrom(TypeInstance other) {
		return switch (other) {
			case PrimitiveTypeInstance otherPrimitive when this.equals(otherPrimitive) -> true;
			case ClassTypeInstance(var declaration, _) when declaration.name().equals(getBoxedClassName()) -> true;
			case ArrayTypeInstance _, ClassTypeInstance _, ParameterizedTypeInstance _, PrimitiveTypeInstance _,
			     TypeVariableInstance _, VoidTypeInstance _, WildcardTypeInstance _ -> false;
		};
	}

	public String getBoxedClassName() {
		return switch (this.primitiveKind) {
			case BOOLEAN -> Boolean.class.getName();
			case BYTE -> Byte.class.getName();
			case CHAR -> Character.class.getName();
			case DOUBLE -> Double.class.getName();
			case FLOAT -> Float.class.getName();
			case INT -> Integer.class.getName();
			case LONG -> Long.class.getName();
			case SHORT -> Short.class.getName();
		};
	}

	@Override
	public TypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return new PrimitiveTypeInstance(primitiveKind, annotations);
	}

	public static PrimitiveTypeInstance fromPrimitiveKind(PrimitiveKind primitiveKind) {
		return new PrimitiveTypeInstance(primitiveKind, List.of());
	}
}
