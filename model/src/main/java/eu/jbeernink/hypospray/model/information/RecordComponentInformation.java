package eu.jbeernink.hypospray.model.information;

import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;

import eu.jbeernink.hypospray.model.information.reflection.ReflectiveRecordComponentInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Single component within a record.
///
/// ## Equality
///
/// Two instances of `RecordComponentInformation` are considered equal if they have they both have an equal [#declaringRecord()] and they have the same name.
public sealed interface RecordComponentInformation<T> extends RecordComponentInfo, AnnotatedDeclaration,
		ClassMember permits ReflectiveRecordComponentInformation {
	@Override
	TypeInstance type();

	@Override
	FieldInformation field();

	@Override
	MethodInformation accessor();

	@Override
	default ClassInformation<T> declaringClass() {
		return declaringRecord();
	}

	@Override
	ClassInformation<T> declaringRecord();
}
