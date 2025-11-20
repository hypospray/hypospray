package eu.jbeernink.hypospray.model.types;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.TypeVariableOwner;

public record DeclaredTypeVariableInstance(TypeVariableOwner owner, String name, List<TypeInstance> upperBounds,
                                           List<AnnotationInformation> typeAnnotations) implements
		TypeVariableInstance {

	public DeclaredTypeVariableInstance {
		requireNonNull(owner);
		requireNonNull(name);
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case TypeVariableInstance otherTypeVariable ->
					Objects.equals(owner, otherTypeVariable.owner()) && Objects.equals(name, otherTypeVariable.name());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(owner, name);
	}

	@Override
	public TypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return new DeclaredTypeVariableInstance(owner, name, upperBounds, annotations);
	}
}
