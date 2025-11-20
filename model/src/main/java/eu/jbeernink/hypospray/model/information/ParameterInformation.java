package eu.jbeernink.hypospray.model.information;

import java.lang.reflect.Parameter;

import jakarta.enterprise.lang.model.declarations.ParameterInfo;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveParameterInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Information about a method or constructor parameter.
///
/// ## Equality
/// Two instances of `ParameterInformation` are considered equal if they have the same [#name()] and [#type()].
///
/// ### Hash code
/// The [#hashCode()] should be calculated as follows: {@snippet :
///  Objects.hash(parameterInformation.name(), parameterInformation.type())
/// }
public sealed interface ParameterInformation extends ParameterInfo, AnnotatedDeclaration permits ReflectiveParameterInformation {
	TypeInstance type();

	@Override
	ExecutableInformation declaringMethod();

	@Deprecated
	Parameter parameterInstance();
}
