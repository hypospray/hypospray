package eu.jbeernink.hypospray.model.information.reflection;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.enterprise.lang.model.AnnotationInfo;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.ConstructorInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.PackageInformation;
import eu.jbeernink.hypospray.model.information.RecordComponentInformation;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;
import eu.jbeernink.hypospray.util.stream.Gatherers;

/// [ClassInformation] that wraps around a [Class] using the reflection API.
public record ReflectiveClassInformation<T>(Class<T> classInstance) implements ClassInformation<T> {

	@Override
	public int numberOfTypeParameters() {
		return classInstance.getTypeParameters().length;
	}

	@Override
	public List<TypeVariableInstance> typeParameterInstances() {
		return Arrays.stream(classInstance.getTypeParameters())
		             .map(typeVariable -> TypeFactory.getInstance().typeVariable(this, typeVariable))
		             .toList();
	}

	@Override
	public @Nullable TypeInstance superClass() {
		if (classInstance.getSuperclass() == null) {
			return null;
		}

		return TypeFactory.getInstance().fromAnnotatedType(classInstance.getAnnotatedSuperclass());
	}

	@Override
	public List<TypeInstance> superInterfaceTypes() {
		return Arrays.stream(classInstance.getAnnotatedInterfaces())
		             .map(superInterface -> TypeFactory.getInstance().fromAnnotatedType(superInterface))
		             .toList();
	}

	@Override
	public @Nullable ClassInformation<? super T> superClassDeclaration() {
		if (classInstance.getSuperclass() == null) {
			return null;
		}
		return new ReflectiveClassInformation<>(classInstance.getSuperclass());
	}

	@Override
	public List<ClassInformation<?>> superInterfaceInformation() {
		return List.copyOf(Arrays.stream(classInstance.getInterfaces()).map(ReflectiveClassInformation::new).toList());
	}

	@Override
	public boolean isEnum() {
		return classInstance.isEnum();
	}

	@Override
	public boolean isAnnotation() {
		return classInstance.isAnnotation();
	}

	@Override
	public boolean isRecord() {
		return classInstance.isRecord();
	}

	@Override
	public int modifiers() {
		return classInstance.getModifiers();
	}

	@Override
	public List<RecordComponentInformation<T>> recordComponentInformation() {
		return Arrays.stream(classInstance.getRecordComponents())
		             .map(
				             recordComponent -> (RecordComponentInformation<T>) new ReflectiveRecordComponentInformation<>(this,
						             recordComponent))
		             .toList();
	}

	@Override
	public String name() {
		return classInstance.getName();
	}

	@Override
	public String simpleName() {
		return classInstance.getSimpleName();
	}

	@Override
	public @Nullable PackageInformation packageInfo() {
		Package declaringPackage = classInstance.getPackage();
		if (isUnnamedPackage(declaringPackage)) {
			return null;
		}

		return new ReflectivePackageInformation(declaringPackage);
	}

	@Override
	public List<MethodInformation> methodInformation() {
		return Stream.concat(getClassMethods(), inheritedMethods()).distinct().toList();
	}

	@Override
	public List<MethodInformation> allMethods() {
		ClassInformation<? super T> superClassDeclaration = superClassDeclaration();
		if (superClassDeclaration == null) {
			return getClassMethods().toList();
		}
		return Stream.concat(getClassMethods(), superClassDeclaration.allMethods().stream())
		             .gather(Gatherers.distinctBy(MethodInformation::methodIdentifier))
		             .toList();
	}

	private Stream<MethodInformation> getClassMethods() {
		return Arrays.stream(classInstance.getDeclaredMethods())
		             .filter(method -> !method.isSynthetic())
		             .map(ReflectiveMethodInformation::new);
	}

	private Stream<MethodInformation> inheritedMethods() {
		Stream<MethodInformation> superInterfaceMethods =
				superInterfaceInformation().stream().flatMap(classInformation -> classInformation.methodInformation().stream());
		if (classInstance.getSuperclass() == null || classInstance.getSuperclass().equals(Object.class)) {
			return superInterfaceMethods;
		}

		return Stream.concat(requireNonNull(superClassDeclaration()).methodInformation().stream(), superInterfaceMethods);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ConstructorInformation<T>> constructorInformation() {
		return Arrays.stream(classInstance.getDeclaredConstructors())
		             .map(constructor -> new ReflectiveConstructorInformation<>((Constructor<T>) constructor))
		             .collect(toUnmodifiableList());
	}

	@Override
	public List<FieldInformation> fieldInformation() {
		return Stream.concat(Arrays.stream(classInstance.getDeclaredFields())
		                           .filter(field -> !field.isSynthetic())
		                           .map(ReflectiveFieldInformation::new), inheritedFields()).distinct().toList();
	}

	private Stream<FieldInformation> inheritedFields() {
		Stream<FieldInformation> interfaceFields =
				superInterfaceInformation().stream().flatMap(classInformation -> classInformation.fieldInformation().stream());
		if (classInstance.getSuperclass() == null || classInstance.getSuperclass().equals(Object.class)) {
			return interfaceFields;
		}

		return Stream.concat(requireNonNull(superClassDeclaration()).fieldInformation().stream(), interfaceFields);
	}

	@Override
	public TypeInstance asType() {
		return new ClassTypeInstance(this);
	}

	@Override
	public <A extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<A> annotationType) {
		return Arrays.stream(classInstance.getAnnotationsByType(annotationType))
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return Arrays.stream(classInstance.getAnnotations())
		             .map(ReflectiveAnnotationInformation::new)
		             .collect(toUnmodifiableList());
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

	private static boolean isUnnamedPackage(Package p) {
		return p.getName().isEmpty();
	}
}
