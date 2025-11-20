package eu.jbeernink.hypospray.model.information;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.model.information.lazy.LazilyResolvedClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.TypeVariableInstance;

/// Runtime information regarding a class.
///
/// ## Equality
///
/// Two [ClassInformation] instances are considered equal if they represent a class of the same full name. The
/// hashcode of a [ClassInformation] is determined by taking the hashcode of the full class name.
///
/// @param <T> the generic type represented by the class.
public sealed interface ClassInformation<T> extends ClassInfo, AnnotatedDeclaration, TypeVariableOwner permits
		LazilyResolvedClassInformation, ReflectiveClassInformation {

	/// Returns the class represented by this [ClassInformation].
	///
	/// This method can only be called if reflection is available for the class represented by this instance at runtime.
	/// If reflection is not available at runtime, an exception will be thrown.
	@Deprecated
	// TODO: #53 - Remove methods directly depending on reflection.
	Class<T> classInstance();

	/// Returns the number of type parameters defined on the class represented by this [ClassInformation].
	///
	/// @return the number of type parameters defined on the class.
	default int numberOfTypeParameters() {
		return typeParameterInstances().size();
	}

	List<TypeVariableInstance> typeParameterInstances();

	@Override
	default List<TypeVariable> typeParameters() {
		return List.copyOf(typeParameterInstances());
	}

	@Override
	@Nullable
	TypeInstance superClass();

	@Override
	@Nullable
	ClassInformation<? super T> superClassDeclaration();

	@Override
	default List<Type> superInterfaces() {
		return List.copyOf(superInterfaceTypes());
	}

	/// Returns a list of the types of the super interfaces of the class represented by this [ClassInformation].
	///
	/// @return a list of the types of the super interfaces.
	List<TypeInstance> superInterfaceTypes();

	@Override
	default List<ClassInfo> superInterfacesDeclarations() {
		return List.copyOf(superInterfaceInformation());
	}

	List<ClassInformation<?>> superInterfaceInformation();

	@Override
	@Nullable
	PackageInformation packageInfo();

	/// A list of information about all methods declared by this class, including inherited methods.
	///
	/// Similar to [#methods()], the methods declared by the [Object] class are excluded, unless this
	/// `ClassInformation` represents the [Object] class itself.
	List<MethodInformation> methodInformation();

	@Override
	default Collection<MethodInfo> methods() {
		return List.copyOf(methodInformation());
	}

	/// A list of all methods of declared and inherited by this class, including those inherited from [Object].
	List<MethodInformation> allMethods();

	List<ConstructorInformation<T>> constructorInformation();

	@Override
	default Collection<MethodInfo> constructors() {
		return List.copyOf(constructorInformation());
	}

	List<FieldInformation> fieldInformation();

	@Override
	default Collection<FieldInfo> fields() {
		return List.copyOf(fieldInformation());
	}

	@Override
	default Collection<RecordComponentInfo> recordComponents() {
		return List.copyOf(recordComponentInformation());
	}

	List<RecordComponentInformation<T>> recordComponentInformation();

	@Override
	default boolean isPlainClass() {
		return !isInterface() && !isEnum() && !isAnnotation() && !isRecord();
	}

	@Override
	default boolean isAbstract() {
		return Modifier.isAbstract(modifiers());
	}

	@Override
	default boolean isFinal() {
		return Modifier.isFinal(modifiers());
	}

	@Override
	default boolean isInterface() {
		return Modifier.isInterface(modifiers()) && !isAnnotation();
	}

	@Override
	TypeInstance asType();
}
