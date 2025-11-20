package eu.jbeernink.hypospray.model.types;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.lang.model.types.ClassType;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;

public record ClassTypeInstance(ClassInformation<?> declaration, List<AnnotationInformation> typeAnnotations) implements TypeInstance, ClassType {

	public ClassTypeInstance {
		typeAnnotations = List.copyOf(typeAnnotations);
	}

	/// Create a new class type with an empty list of annotations.
	public ClassTypeInstance(ClassInformation<?> declaration) {
		this(declaration, List.of());
	}

	@SuppressWarnings("deprecated")
	@Override
	public Class<?> asJavaType() {
		return declaration.classInstance();
	}

	public List<TypeInstance> parentTypes() {
		return Stream.concat(Optional.ofNullable(declaration.superClass()).stream(),
				declaration.superInterfaceTypes().stream()).toList();
	}

	@Override
	public ClassTypeInstance asClass() {
		return this;
	}

	@Override
	public String toString() {
		return asJavaType().getName();
	}

	@Override
	public boolean isAssignableFrom(TypeInstance other) {
		if (this.equals(other)) {
			return true;
		}

		return switch (other) {
			case ClassTypeInstance(ClassInformation<?> otherDeclaration, _) -> {
				TypeInstance superClass = otherDeclaration.superClass();
				if (superClass != null && isAssignableFrom(superClass)) {
					yield true;
				}

				yield otherDeclaration.superInterfaceTypes().stream().anyMatch(this::isAssignableFrom);
			}
			case ParameterizedTypeInstance(ClassTypeInstance rawType, _, _) -> isAssignableFrom(rawType);
			case PrimitiveTypeInstance primitiveType -> declaration.name().equals(primitiveType.getBoxedClassName());
			case TypeVariableInstance typeVariable ->
					typeVariable.upperBounds().stream().allMatch(bound -> bound.isAssignableFrom(this));
			case ArrayTypeInstance _, VoidTypeInstance _, WildcardTypeInstance _ -> false;
		};
	}

	@Override
	public ClassTypeInstance withTypeAnnotations(List<AnnotationInformation> annotations) {
		return new ClassTypeInstance(declaration, annotations);
	}
}
