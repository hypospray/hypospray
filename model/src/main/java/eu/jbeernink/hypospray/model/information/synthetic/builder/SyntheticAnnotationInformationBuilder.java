package eu.jbeernink.hypospray.model.information.synthetic.builder;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;

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
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.types.ArrayTypeInstance;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.PrimitiveTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.VoidTypeInstance;

public class SyntheticAnnotationInformationBuilder implements AnnotationBuilder {

	private static final String UNSUPPORTED_ANNOTATION_INFO_ERROR =
			"Only AnnotationInfo instances created by the container are supported: %s";
	private static final String UNSUPPORTED_CLASS_INFO_ERROR =
			"Only ClassInfo instances created by the container are supported: %s";

	private final ClassInformation<? extends Annotation> annotationClass;
	private final Map<String, AnnotationMemberValue> annotationMembers = new HashMap<>();

	public SyntheticAnnotationInformationBuilder(ClassInformation<? extends Annotation> annotationClass) {
		if (!annotationClass.isAnnotation()) {
			throw new IllegalArgumentException(annotationClass + " is not an annotation");
		}
		this.annotationClass = requireNonNull(annotationClass);
	}

	public SyntheticAnnotationInformationBuilder member(String name, AnnotationMemberValue value) {
		annotationMembers.put(name, value);

		return this;
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, AnnotationMember value) {
		switch (value) {
			case AnnotationMemberValue memberValue -> annotationMembers.put(name, memberValue);
			case AnnotationMember _ -> throw new IllegalArgumentException(
					"Unsupported annotation member type, only instances created by the container are supported: %s".formatted(
							value.getClass()));
		}
		return this;
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, boolean value) {
		return member(name, new BooleanValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, boolean[] values) {
		List<AnnotationMemberValue> list =
				IntStream.range(0, values.length).mapToObj(i -> new BooleanValue(values[i])).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, byte value) {
		return member(name, new ByteValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, byte[] values) {
		List<AnnotationMemberValue> list =
				IntStream.range(0, values.length).mapToObj(i -> new ByteValue(values[i])).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, short value) {
		return member(name, new ShortValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, short[] values) {
		List<AnnotationMemberValue> list =
				IntStream.range(0, values.length).mapToObj(i -> new ShortValue(values[i])).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, int value) {
		return member(name, new IntegerValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, int[] values) {
		List<AnnotationMemberValue> list = Arrays.stream(values).mapToObj(IntegerValue::new).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, long value) {
		return member(name, new LongValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, long[] values) {
		List<AnnotationMemberValue> list = Arrays.stream(values).mapToObj(LongValue::new).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, float value) {
		return member(name, new FloatValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, float[] values) {
		List<AnnotationMemberValue> list =
				IntStream.range(0, values.length).mapToObj(i -> new FloatValue(values[i])).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, double value) {
		return member(name, new DoubleValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, double[] values) {
		List<AnnotationMemberValue> list = Arrays.stream(values).mapToObj(DoubleValue::new).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, char value) {
		return member(name, new CharValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, char[] values) {
		List<AnnotationMemberValue> list =
				IntStream.range(0, values.length).mapToObj(i -> new CharValue(values[i])).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, String value) {
		return member(name, new StringValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, String[] values) {
		List<AnnotationMemberValue> list = Arrays.stream(values).map(StringValue::new).collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Enum<?> value) {
		return member(name,
				new EnumValue(ClassInformationSource.getInstance().getClassInformation(value.getClass()), value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Enum<?>[] values) {
		List<AnnotationMemberValue> list = Arrays.stream(values)
		                                         .map(enumValue -> new EnumValue(ClassInformationSource.getInstance()
		                                                                                               .getClassInformation(
				                                                                                               enumValue.getClass()),
				                                         enumValue))
		                                         .collect(toUnmodifiableList());
		return member(name, new ArrayValue(list));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Class<? extends Enum<?>> enumType, String enumValue) {
		return member(name, new EnumValue(ClassInformationSource.getInstance().getClassInformation(enumType),
				getEnumConstant(enumType, enumValue)));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Class<? extends Enum<?>> enumType, String[] enumValues) {
		List<AnnotationMemberValue> values = Arrays.stream(enumValues)
		                                           .map(value -> getEnumConstant(enumType, value))
		                                           .map(constant -> new EnumValue(
				                                           ClassInformationSource.getInstance().getClassInformation(enumType),
				                                           constant))
		                                           .collect(toUnmodifiableList());


		return member(name, new ArrayValue(values));
	}

	private static Enum<?> getEnumConstant(Class<? extends Enum<?>> enumType, String enumValue) {
		return Arrays.stream(enumType.getEnumConstants())
		             .filter(constant -> constant.name().equals(enumValue))
		             .findFirst()
		             .orElseThrow(() -> new IllegalArgumentException(
				             "Unknown constant for enum %s: %s.".formatted(enumType, enumValue)));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, ClassInfo enumType, String enumValue) {
		if (!enumType.isEnum()) {
			throw new IllegalArgumentException("Class must be an enum class: %s.".formatted(enumType.name()));
		}
		return switch (enumType) {
			case ReflectiveClassInformation<?>(Class<?> enumClass) ->
					member(name, (Class<? extends Enum<?>>) enumClass, enumValue);
			case ClassInformation<?> classInformation -> {
				try {
					yield member(name, (Class<? extends Enum<?>>) Class.forName(classInformation.name()), enumValue);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException(e);
				}
			}
			case ClassInfo _ -> throw new IllegalArgumentException(
					"Only ClassInfo instances created by the container are supported: %s.".formatted(enumType.getClass()));
		};
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, ClassInfo enumType, String[] enumValues) {
		if (!enumType.isEnum()) {
			throw new IllegalArgumentException("Class must be an enum class: %s.".formatted(enumType.name()));
		}

		if (enumType instanceof ClassInformation<?> classInformation) {
			if (classInformation instanceof ReflectiveClassInformation<?>(Class<?> enumClass)) {
				return member(name, (Class<? extends Enum<?>>) enumClass, enumValues);
			}

			try {
				return member(name, (Class<? extends Enum<?>>) Class.forName(classInformation.name()), enumValues);
			} catch (ClassNotFoundException e) {
				// TODO add descriptive error message.
				throw new IllegalArgumentException(e);
			}
		}

		throw new IllegalArgumentException(UNSUPPORTED_CLASS_INFO_ERROR.formatted(enumType.getClass()));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Class<?> value) {
		return member(name, TypeFactory.getInstance().fromJavaType(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Class<?>[] values) {
		var typeFactory = TypeFactory.getInstance();
		List<AnnotationMemberValue> arrayValues =
				Arrays.stream(values).map(typeFactory::fromJavaType).map(TypeValue::new).collect(toUnmodifiableList());
		return member(name, new ArrayValue(arrayValues));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, ClassInfo value) {
		return switch (value) {
			case ClassInformation<?> classInformation -> member(name, new TypeValue(classInformation.asType()));
			case ClassInfo _ ->
					throw new IllegalArgumentException(UNSUPPORTED_CLASS_INFO_ERROR.formatted(value.getClass().getName()));
		};
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, ClassInfo[] values) {
		List<AnnotationMemberValue> arrayValues = Arrays.stream(values).map(value -> {
			if (value instanceof ClassInformation<?> information) {
				return new TypeValue(new ClassTypeInstance(information));
			}

			throw new IllegalArgumentException(UNSUPPORTED_CLASS_INFO_ERROR.formatted(value.getClass().getName()));
		}).collect(toUnmodifiableList());

		return member(name, new ArrayValue(arrayValues));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Type value) {
		return member(name, getTypeValue(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Type[] values) {
		List<AnnotationMemberValue> elements =
				Arrays.stream(values).map(SyntheticAnnotationInformationBuilder::getTypeValue).collect(toUnmodifiableList());

		return member(name, new ArrayValue(elements));
	}

	private static TypeValue getTypeValue(Type type) {
		return switch (type) {
			case VoidTypeInstance voidTypeInstance -> new TypeValue(voidTypeInstance);
			case PrimitiveTypeInstance primitiveTypeInstance -> new TypeValue(primitiveTypeInstance);
			case ClassTypeInstance classTypeInstance -> new TypeValue(classTypeInstance);
			case ArrayTypeInstance(ClassTypeInstance classTypeArray, var annotations) ->
					new TypeValue(new ArrayTypeInstance(classTypeArray, annotations));
			case ArrayTypeInstance(PrimitiveTypeInstance primitiveTypeArray, var annotations) ->
					new TypeValue(new ArrayTypeInstance(primitiveTypeArray, annotations));
			case ArrayTypeInstance(var componentType, _) -> throw new IllegalArgumentException(
					"Array types in annotations only support primitive types and classes: %s.".formatted(componentType));
			case TypeInstance typeInstance -> throw new IllegalArgumentException(
					"Type must be one of the following: void, primitive, class or array of primitive types or classes: %s.".formatted(
							typeInstance));
			case Type _ -> throw new IllegalArgumentException(
					"Only Type instances created by the container are supported: %s".formatted(type.getClass().getName()));
		};
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, AnnotationInfo value) {
		return switch (value) {
			case AnnotationInformation annotationInformation ->
					member(name, new NestedAnnotationValue(annotationInformation));
			case AnnotationInfo _ ->
					throw new IllegalArgumentException(UNSUPPORTED_ANNOTATION_INFO_ERROR.formatted(value.getClass().getName()));
		};
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, AnnotationInfo[] values) {
		List<AnnotationMemberValue> nestedAnnotations =
				Arrays.stream(values).map(annotationInfo -> switch (annotationInfo) {
					case AnnotationInformation annotationInformation -> annotationInformation;
					case AnnotationInfo _ -> throw new IllegalArgumentException(
							UNSUPPORTED_ANNOTATION_INFO_ERROR.formatted(annotationInfo.getClass().getName()));
				}).map(NestedAnnotationValue::new).collect(toUnmodifiableList());

		member(name, new ArrayValue(nestedAnnotations));

		return this;
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Annotation value) {
		return member(name, new ReflectiveAnnotationInformation<>(value));
	}

	@Override
	public SyntheticAnnotationInformationBuilder member(String name, Annotation[] values) {
		List<AnnotationMemberValue> elements = Arrays.stream(values)
		                                             .map(ReflectiveAnnotationInformation::new)
		                                             .map(NestedAnnotationValue::new)
		                                             .collect(toUnmodifiableList());
		return member(name, new ArrayValue(elements));
	}

	@Override
	public SyntheticAnnotationInformationBuilder value(String value) {
		return member("value", new StringValue(value));
	}

	@Override
	public AnnotationInformation build() {
		return new SyntheticAnnotationInformation(annotationClass, annotationMembers);
	}
}
