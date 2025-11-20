package eu.jbeernink.hypospray.model.information;

/// A member of a class.
public sealed interface ClassMember extends AnnotatedDeclaration permits ExecutableInformation, FieldInformation,
		RecordComponentInformation {

	/// Returns the [eu.jbeernink.hypospray.model.information.ClassInformation] for the class this element is a member of.
	///
	/// @return the class this element is a member of.
	ClassInformation<?> declaringClass();
}
