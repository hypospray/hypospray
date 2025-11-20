package eu.jbeernink.hypospray.model.types.lazy;

import java.util.List;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.TypeVariableOwner;
import eu.jbeernink.hypospray.model.reference.LateReference;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;

public record LazyTypeVariableInstance(LateReference<TypeVariableInstance> typeVariable) implements
		TypeVariableInstance {
	@Override
	public TypeVariableOwner owner() {
		return typeVariable.get().owner();
	}

	@Override
	public String name() {
		return typeVariable.get().name();
	}

	@Override
	public List<TypeInstance> upperBounds() {
		return typeVariable.get().upperBounds();
	}

	@Override
	public List<AnnotationInformation> typeAnnotations() {
		return typeVariable.get().typeAnnotations();
	}

	@Override
	public boolean equals(Object obj) {
		return typeVariable.get().equals(obj);
	}

	@Override
	public int hashCode() {
		return typeVariable.get().hashCode();
	}

	@Override
	public String toString() {
		return typeVariable.get().toString();
	}

	@Override
	public TypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return typeVariable.get().withTypeAnnotations(annotations);
	}
}
