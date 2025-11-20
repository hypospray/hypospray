package eu.jbeernink.hypospray.model.information;

import java.lang.reflect.Constructor;
import java.util.List;

import jakarta.enterprise.lang.model.declarations.ParameterInfo;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveConstructorInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Information about a constructor.
///
/// ## Equality
/// Two instances of `ConstructorInformation` are considered equal if they have the same [#declaringClass()] and [#parameterInformation()].
public sealed interface ConstructorInformation<T> extends ExecutableInformation permits
		ReflectiveConstructorInformation {

	@Deprecated
		// TODO: #53 - Remove methods directly depending on reflection.
	Constructor<T> constructorInstance();

	@Override
	ClassInformation<T> declaringClass();

	/// Receiver type, which is always null for a constructor.
	@Override
	default TypeInstance receiverType() {
		return null;
	}

	default List<ParameterInfo> parameters() {
		return List.copyOf(parameterInformation());
	}

	List<ParameterInformation> parameterInformation();
}
