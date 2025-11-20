package eu.jbeernink.hypospray.model.information.reflection;

import static eu.jbeernink.hypospray.model.todo.Todo.unimplemented;

import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Objects;

import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.information.RecordComponentInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// [RecordComponentInformation] that wraps the reflection API to obtain information about a [RecordComponent].
///
/// @param <T> The type of the record that declares this [RecordComponent].
public record ReflectiveRecordComponentInformation<T>(ClassInformation<T> declaringRecord,
                                                      RecordComponent recordComponent) implements
		RecordComponentInformation<T> {

	@Override
	public String name() {
		return recordComponent.getName();
	}

	@Override
	public TypeInstance type() {
		return TypeFactory.getInstance().of(recordComponent.getType());
	}

	@Override
	public FieldInformation field() {
		return unimplemented();
	}

	@Override
	public MethodInformation accessor() {
		return unimplemented();
	}

	@Override
	public List<AnnotationInformation> annotationInformation() {
		return unimplemented();
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case RecordComponentInformation<?> other ->
					Objects.equals(declaringRecord, other.declaringRecord()) && Objects.equals(name(), other.name());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(declaringRecord, name());
	}
}
