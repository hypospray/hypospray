package eu.jbeernink.hypospray.model.information.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.enterprise.invoke.Invoker;

import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.annotation.AnnotationMemberValue;
import eu.jbeernink.hypospray.model.annotation.ArrayValue;
import eu.jbeernink.hypospray.model.annotation.BooleanValue;
import eu.jbeernink.hypospray.model.annotation.ByteValue;
import eu.jbeernink.hypospray.model.annotation.CharValue;
import eu.jbeernink.hypospray.model.annotation.DoubleValue;
import eu.jbeernink.hypospray.model.annotation.EnumValue;
import eu.jbeernink.hypospray.model.annotation.FloatValue;
import eu.jbeernink.hypospray.model.annotation.IntegerValue;
import eu.jbeernink.hypospray.model.annotation.LongValue;
import eu.jbeernink.hypospray.model.annotation.NestedAnnotationValue;
import eu.jbeernink.hypospray.model.annotation.ShortValue;
import eu.jbeernink.hypospray.model.annotation.StringValue;
import eu.jbeernink.hypospray.model.annotation.TypeValue;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;

/// [AnnotationInformation] which wraps an [Annotation] using the reflection API.
public record ReflectiveAnnotationInformation<T extends Annotation>(T annotation) implements AnnotationInformation {
	@Override
	public Annotation annotationInstance() {
		return annotation;
	}

	@Override
	public ClassInformation<? extends Annotation> declaration() {
		return ClassInformationSource.getInstance().getClassInformation(annotation.annotationType());
	}

	@Override
	public Map<String, AnnotationMemberValue> memberValues() {
		var invokerFactoryManager = InvokerFactoryManager.getInstance();
		Map<String, AnnotationMemberValue> members = new HashMap<>();
		for (Method member : annotation.annotationType().getDeclaredMethods()) {
			try {
				Class<?> returnType = member.getReturnType();
				@SuppressWarnings("unchecked")
				Invoker<T, Object> invoker =
						(Invoker<T, Object>) 	invokerFactoryManager.getInvoker(member.getDeclaringClass().getName(), toMethodIdentifier(member));

				Object value = invoker.invoke(annotation, null);

				members.put(member.getName(), getMemberValue(returnType, value));
			} catch (Exception e) {
				// TODO throw better exception.
				throw new RuntimeException(e);
			}
		}

		return Map.copyOf(members);
	}

	private String toMethodIdentifier(Method method) {
		return "%s[(%s)%s]".formatted(method.getName(),
				Arrays.stream(method.getParameterTypes()).map(Class::descriptorString).collect(Collectors.joining(",")),
				method.getReturnType().descriptorString());
	}

	@Override
	public boolean equals(Object obj) {
		return switch (obj) {
			case AnnotationInformation other ->
					Objects.equals(declaration(), other.declaration()) && Objects.equals(memberValues(), other.memberValues());
			case Object _ -> false;
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(declaration(), memberValues());
	}

	private static AnnotationMemberValue getMemberValue(Class<?> returnType, Object value) {
		if (returnType.isArray()) {
			Class<?> componentType = returnType.getComponentType();

			return new ArrayValue(IntStream.range(0, Array.getLength(value))
			                               .mapToObj(i -> Array.get(value, i))
			                               .map(element -> getMemberValue(componentType, element))
			                               .toList());
		}

		if (returnType.isEnum()) {
			return new EnumValue(ClassInformationSource.getInstance().getClassInformation(returnType), (Enum<?>) value);
		}

		if (returnType.equals(Boolean.TYPE)) {
			return new BooleanValue((boolean) value);
		}

		if (returnType.equals(Byte.TYPE)) {
			return new ByteValue((byte) value);
		}

		if (returnType.equals(Character.TYPE)) {
			return new CharValue((char) value);
		}

		if (returnType.equals(Class.class)) {
			// TODO: is this correct?
			return new TypeValue(TypeFactory.getInstance().fromJavaType((Class<?>) value));
		}

		if (returnType.equals(Double.TYPE)) {
			return new DoubleValue((double) value);
		}

		if (returnType.equals(Float.TYPE)) {
			return new FloatValue((float) value);
		}

		if (returnType.equals(Integer.TYPE)) {
			return new IntegerValue((int) value);
		}

		if (returnType.equals(Long.TYPE)) {
			return new LongValue((long) value);
		}

		if (returnType.isAnnotation()) {
			return new NestedAnnotationValue(new ReflectiveAnnotationInformation<>((Annotation) value));
		}

		if (returnType.equals(Short.TYPE)) {
			return new ShortValue((short) value);
		}

		if (returnType.equals(String.class)) {
			return new StringValue((String) value);
		}

		throw new IllegalArgumentException("Unsupported member type: " + returnType);
	}
}
