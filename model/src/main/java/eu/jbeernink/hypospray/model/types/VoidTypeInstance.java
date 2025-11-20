package eu.jbeernink.hypospray.model.types;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.VoidType;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.todo.Todo;

public enum VoidTypeInstance implements VoidType, TypeInstance {
	VOID;

	@Override
	public Type asJavaType() {
		return Void.TYPE;
	}

	@Override
	public List<AnnotationInformation> typeAnnotations() {
		return List.of();
	}

	@Override
	public VoidTypeInstance asVoid() {
		return this;
	}

	@Override
	public boolean isAssignableFrom(TypeInstance other) {
		return false;
	}

	@Override
	public VoidTypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		// Type annotations are not supported on void.
		return this;
	}
}
