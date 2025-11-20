package eu.jbeernink.hypospray.model.types;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.ArrayType;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;

public record ArrayTypeInstance(TypeInstance componentType, List<AnnotationInformation> typeAnnotations) implements TypeInstance, ArrayType {

	public ArrayTypeInstance {
		typeAnnotations = List.copyOf(typeAnnotations);
	}

	@SuppressWarnings("deprecated")
	@Deprecated
	@Override
	public Type asJavaType() {
		return ((Class<?>) componentType.asJavaType()).arrayType();
	}

	@Override
	public ArrayTypeInstance asArray() {
		return this;
	}

	@Override
	public boolean isAssignableFrom(TypeInstance other) {
		return switch (other) {
			case ArrayTypeInstance arrayTypeInstance -> componentType.isAssignableFrom(arrayTypeInstance.componentType());
			case ClassTypeInstance _, ParameterizedTypeInstance _, PrimitiveTypeInstance _, TypeVariableInstance _,
			     VoidTypeInstance _, WildcardTypeInstance _ -> false;
		};
	}

	@Override
	public TypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return new ArrayTypeInstance(componentType, annotations);
	}
}
