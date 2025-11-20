package eu.jbeernink.hypospray.model.information;

import java.lang.reflect.Method;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveMethodInformation;

/// Information about a Java method.
///
/// ## Equality
/// Two instances of `MethodInformation` are considered equal if they have the same [#declaringClass()], [#name()] and [#parameterTypes()].
///
/// ### Hash code
/// The hash code of a `MethodInformation` must be calculated as follows: {@snippet :
///  Objects.hash(methodInformation.declaringClass(), methodInformation.name(), methodInformation.parameterTypes());
/// }
public sealed interface MethodInformation extends ExecutableInformation, TypeVariableOwner permits
		ReflectiveMethodInformation {

	@Deprecated
		// TODO: #53 - Remove methods directly depending on reflection.
	Method methodInstance();


}
