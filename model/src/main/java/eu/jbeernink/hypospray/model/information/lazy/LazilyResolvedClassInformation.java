package eu.jbeernink.hypospray.model.information.lazy;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;
import eu.jbeernink.hypospray.model.information.RecordComponentInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;

/// [ClassInformation] instance that is only resolved once needed.
///
/// @param classInformationSupplier a [Supplier] that returns the class information instance, may be invoked multiple times and must always return the same instance.
/// @param <T>                      the type of the class modeled by this [ClassInformation].
public record LazilyResolvedClassInformation<T>(Supplier<ClassInformation<T>> classInformationSupplier) implements
		ClassInformation<T> {

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return classInformationSupplier.get().annotationInformation();
	}

	@Override
	public Class<T> classInstance() {
		return classInformationSupplier.get().classInstance();
	}

	@Override
	public List<TypeVariableInstance> typeParameterInstances() {
		return classInformationSupplier.get().typeParameterInstances();
	}

	@Override
	public List<MethodInformation> methodInformation() {
		return classInformationSupplier.get().methodInformation();
	}

	@Override
	public List<MethodInformation> allMethods() {
		return classInformationSupplier.get().allMethods();
	}

	@Override
	public List<ConstructorInformation<T>> constructorInformation() {
		return classInformationSupplier.get().constructorInformation();
	}

	@Override
	public List<FieldInformation> fieldInformation() {
		return classInformationSupplier.get().fieldInformation();
	}

	@Override
	public TypeInstance asType() {
		return classInformationSupplier.get().asType();
	}

	@Override
	public String name() {
		return classInformationSupplier.get().name();
	}

	@Override
	public String simpleName() {
		return classInformationSupplier.get().simpleName();
	}

	@Override
	public PackageInformation packageInfo() {
		return classInformationSupplier.get().packageInfo();
	}

	@Override
	public @Nullable TypeInstance superClass() {
		return classInformationSupplier.get().superClass();
	}

	@Override
	public @Nullable ClassInformation<? super T> superClassDeclaration() {
		return classInformationSupplier.get().superClassDeclaration();
	}

	@Override
	public List<TypeInstance> superInterfaceTypes() {
		return classInformationSupplier.get().superInterfaceTypes();
	}

	@Override
	public List<ClassInformation<?>> superInterfaceInformation() {
		return classInformationSupplier.get().superInterfaceInformation();
	}

	@Override
	public boolean isEnum() {
		return classInformationSupplier.get().isEnum();
	}

	@Override
	public boolean isAnnotation() {
		return classInformationSupplier.get().isAnnotation();
	}

	@Override
	public boolean isRecord() {
		return classInformationSupplier.get().isRecord();
	}

	@Override
	public int modifiers() {
		return classInformationSupplier.get().modifiers();
	}

	@Override
	public Collection<RecordComponentInfo> recordComponents() {
		return classInformationSupplier.get().recordComponents();
	}

	@Override
	public List<RecordComponentInformation<T>> recordComponentInformation() {
		return classInformationSupplier.get().recordComponentInformation();
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case ClassInformation<?> other -> Objects.equals(name(), other.name());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(name());
	}
}
