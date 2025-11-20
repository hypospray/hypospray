package eu.jbeernink.hypospray.model.information.synthetic;

import static eu.jbeernink.hypospray.model.todo.Todo.unimplemented;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;

import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;

public record SyntheticAnnotationInformation(ClassInformation<? extends Annotation> declaration,
                                             Map<String, AnnotationMemberValue> memberValues) implements
		AnnotationInformation {

	public SyntheticAnnotationInformation {
		memberValues = Map.copyOf(memberValues);
	}

	@Override
	public Annotation annotationInstance() {
		return unimplemented();
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case AnnotationInformation other ->
					Objects.equals(declaration, other.declaration()) && Objects.equals(memberValues, other.memberValues());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(declaration, memberValues);
	}
}
