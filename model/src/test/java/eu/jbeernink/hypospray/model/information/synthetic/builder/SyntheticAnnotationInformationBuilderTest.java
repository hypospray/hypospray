package eu.jbeernink.hypospray.model.information.synthetic.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import jakarta.enterprise.inject.literal.InjectLiteral;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.PackageInfo;
import jakarta.enterprise.lang.model.declarations.RecordComponentInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.information.synthetic.SyntheticAnnotationInformation;
import eu.jbeernink.hypospray.model.types.ArrayTypeInstance;
import eu.jbeernink.hypospray.model.types.ClassTypeInstance;
import eu.jbeernink.hypospray.model.types.PrimitiveTypeInstance;
import eu.jbeernink.hypospray.model.types.TypeInstance;
import eu.jbeernink.hypospray.model.types.VoidTypeInstance;

@DisplayName("SyntheticAnnotationInformationBuilder")
class SyntheticAnnotationInformationBuilderTest {

	@Test
	@DisplayName("build() returns a annotation information instance.")
	void build_returnsAnnotationInformation() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		var classInformation = new ReflectiveClassInformation<>(Named.class);

		AnnotationInformation annotationInformation = builder.build();

		SyntheticAnnotationInformation expectedAnnotationInformation =
				new SyntheticAnnotationInformation(classInformation, Map.of());
		assertEquals(expectedAnnotationInformation, annotationInformation);
	}

	@Test
	@DisplayName("member(String, AnnotationMemberValue) sets the value on the builder.")
	void member_withAnnotationMemberValue_setsValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		AnnotationMemberValue annotationMemberValue = new StringValue("foo");

		builder.member("value", annotationMemberValue);

		AnnotationInformation annotationInformation = builder.build();
		assertEquals(annotationMemberValue, annotationInformation.members().get("value"));
	}

	@Test
	@DisplayName("member(String, AnnotationMember) sets the value on the builder.")
	void member_withAnnotationMember_setsValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		AnnotationMember annotationMember = new StringValue("foo");

		builder.member("value", annotationMember);

		AnnotationInformation annotationInformation = builder.build();
		assertEquals(annotationMember, annotationInformation.members().get("value"));
	}

	@Test
	@DisplayName("member(String, boolean) sets a boolean value.")
	void member_withBoolean_setsBooleanValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("foo", true);

		AnnotationInformation annotation = builder.build();
		assertEquals(new BooleanValue(true), annotation.members().get("foo"));
	}

	@Test
	@DisplayName("member(String, boolean[]) sets array of boolean values.")
	void member_withBooleanArray_setsArrayOfBooleanValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("foo", new boolean[]{true, false});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(List.of(new BooleanValue(true), new BooleanValue(false))),
				annotation.members().get("foo"));
	}

	@Test
	@DisplayName("member(String, byte) sets byte value.")
	void member_withByte_setsByteValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("y", (byte) 1);

		AnnotationInformation annotation = builder.build();
		assertEquals(new ByteValue((byte) 1), annotation.members().get("y"));
	}

	@Test
	@DisplayName("member(String, byte[]) sets array of byte values.")
	void member_withByteArray_setsArrayOfByteValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("z", new byte[]{1, 2});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(List.of(new ByteValue((byte) 1), new ByteValue((byte) 2))),
				annotation.members().get("z"));
	}

	@Test
	@DisplayName("member(String, short) sets short value.")
	void member_withShort_setsShortValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("x", (short) 42);

		AnnotationInformation annotation = builder.build();
		assertEquals(new ShortValue((short) 42), annotation.members().get("x"));
	}

	@Test
	@DisplayName("member(String, short[]) sets an array of short values.")
	void member_withShortArray_setsArrayOfShortValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("test", new short[]{47, 42, 256});

		AnnotationInformation annotation = builder.build();
		assertEquals(
				new ArrayValue(List.of(new ShortValue((short) 47), new ShortValue((short) 42), new ShortValue((short) 256))),
				annotation.member("test"));
	}

	@Test
	@DisplayName("member(String, int) sets an int value.")
	void member_withInt_setsIntValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("int", 42);

		AnnotationInformation annotation = builder.build();
		assertEquals(new IntegerValue(42), annotation.members().get("int"));
	}

	@Test
	@DisplayName("member(String, int[]) sets array of int values.")
	void member_withIntArray_setsArrayOfIntValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("test", new int[]{2_000_000, 256});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(List.of(new IntegerValue(2_000_000), new IntegerValue(256))),
				annotation.members().get("test"));
	}

	@Test
	@DisplayName("member(String, long) sets a long value.")
	void member_withLong_setsLongValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("l", 42L);

		AnnotationInformation annotation = builder.build();
		assertEquals(new LongValue(42), annotation.members().get("l"));
	}

	@Test
	@DisplayName("member(String, long[]) sets an array of long values.")
	void member_withLongArray_setsArrayOfLongValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("longs", new long[]{2_000_000_000_000L, 256});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(List.of(new LongValue(2_000_000_000_000L), new LongValue(256))),
				annotation.members().get("longs"));
	}

	@Test
	@DisplayName("member(String, float) sets a float value.")
	void member_withFloat_setsFloatValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("f", 4.2f);

		AnnotationInformation annotation = builder.build();
		assertEquals(new FloatValue(4.2f), annotation.members().get("f"));
	}

	@Test
	@DisplayName("member(String, float[]) sets an array of float values.")
	void member_withFloatArray_setsArrayOfFloatValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("floats", new float[]{1.2f, 3.4f});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(List.of(new FloatValue(1.2f), new FloatValue(3.4f))), annotation.member("floats"));
	}

	@Test
	@DisplayName("member(String, double) sets a double value.")
	void member_withDouble_setsDoubleValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("d", 4.2d);

		AnnotationInformation annotation = builder.build();
		assertEquals(new DoubleValue(4.2d), annotation.members().get("d"));
	}

	@Test
	@DisplayName("member(String, double[]) sets an array of double values.")
	void member_withDoubleArray_setsArrayOfDoubleValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("doubles", new double[]{1.2d, 3.14d});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(List.of(new DoubleValue(1.2d), new DoubleValue(3.14d))), annotation.member("doubles"));
	}

	@Test
	@DisplayName("member(String, char) sets a char value.")
	void member_withChar_setsCharValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("ch", 'c');

		AnnotationInformation annotation = builder.build();
		assertEquals(new CharValue('c'), annotation.members().get("ch"));
	}

	@Test
	@DisplayName("member(String, char[]) sets an array of char values.")
	void member_withCharArray_setsArrayOfCharValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("chars", new char[]{'a', 'b', 'c'});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(List.of(new CharValue('a'), new CharValue('b'), new CharValue('c')));
		assertEquals(expectedValue, annotation.members().get("chars"));
	}

	@Test
	@DisplayName("member(String, String) sets a string value.")
	void member_withString_setsStringValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("string", "A string!");

		AnnotationInformation annotation = builder.build();
		assertEquals(new StringValue("A string!"), annotation.members().get("string"));
	}

	@Test
	@DisplayName("member(String, String[]) sets an array of string values.")
	void member_withStringArray_setsArrayOfStringValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("strings", new String[]{"A string!", "Another string?"});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(List.of(new StringValue("A string!"), new StringValue("Another string?")));
		assertEquals(expectedValue, annotation.members().get("strings"));
	}

	@Test
	@DisplayName("member(String, Enum<?>) sets an enum value.")
	void member_withEnum_setsEnumValue() {
		enum Foo {BAR}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("foo", Foo.BAR);

		AnnotationInformation annotation = builder.build();
		assertEquals(new EnumValue(new ReflectiveClassInformation<>(Foo.class), Foo.BAR), annotation.members().get("foo"));
	}

	@Test
	@DisplayName("member(String, Enum[]) sets an array of enum values.")
	void member_withEnumArray_setsArrayOfEnumValues() {
		enum Foo {BAR, BAZ}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("foo", new Enum<?>[]{Foo.BAR, Foo.BAZ});

		AnnotationInformation annotation = builder.build();
		var fooClassInformation = new ReflectiveClassInformation<>(Foo.class);
		var expectedValue = new ArrayValue(
				List.of(new EnumValue(fooClassInformation, Foo.BAR), new EnumValue(fooClassInformation, Foo.BAZ)));
		assertEquals(expectedValue, annotation.members().get("foo"));
	}

	@Test
	@DisplayName("member(String, Class<? extends Enum<?>>, String) sets an enum value.")
	void member_withEnumClassAndString_setsEnumValue() {
		enum MyEnum {A, B, C}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("myEnum", MyEnum.class, "A");

		AnnotationInformation annotation = builder.build();
		assertEquals(new EnumValue(new ReflectiveClassInformation<>(MyEnum.class), MyEnum.A),
				annotation.members().get("myEnum"));
	}

	@Test
	@DisplayName(
			"member(String, Class<? extends Enum<?>>, String) with an unknown enum constant, throws IllegalArgumentException.")
	void member_withEnumClassAndStringAndUnknownEnumConstant_throwsIllegalArgumentException() {
		enum MyEnum {}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class, () -> builder.member("myEnum", MyEnum.class, "A"));

		assertEquals(
				"Unknown constant for enum class eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$2MyEnum: A.",
				exception.getMessage());
	}

	@Test
	@DisplayName(
			"member(String, Class<? extends Enum<?>>, String[]) with an unknown enum constant, throws IllegalArgumentException.")
	void member_withEnumClassAndStringArrayAndUnknownEnumConstant_throwsIllegalArgumentException() {
		enum SomeEnum {}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception =
				assertThrows(IllegalArgumentException.class, () -> builder.member("myEnum", SomeEnum.class, new String[]{"A"}));

		assertEquals(
				"Unknown constant for enum class eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$1SomeEnum: A.",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, Class<? extends Enum<?>>, String[]) sets an array of enum constants as value.")
	void member_withEnumClassAndStringArray_setsArrayOfEnumConstants() {
		enum AnotherEnum {ONE, TWO, THREE}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("anotherEnum", AnotherEnum.class, new String[]{"ONE", "THREE"});

		AnnotationInformation annotation = builder.build();
		var anotherEnumClassInformation = new ReflectiveClassInformation<>(AnotherEnum.class);
		var expectedValue = new ArrayValue(List.of(new EnumValue(anotherEnumClassInformation, AnotherEnum.ONE),
				new EnumValue(anotherEnumClassInformation, AnotherEnum.THREE)));
		assertEquals(expectedValue, annotation.members().get("anotherEnum"));
	}

	@Test
	@DisplayName("member(String, ClassInfo, String) with a non-enum type, throws IllegalArgumentException.")
	void member_withNonEnumClassInfoAndStringType_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("test", new ReflectiveClassInformation<>(String.class), "fo"));

		assertEquals("Class must be an enum class: java.lang.String.", exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo, String) with an unknown constant name, throws IllegalArgumentException.")
	void member_withClassInfoAndUnknownEnumConstantName_throwsIllegalArgumentException() {
		enum Foo {}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("test", new ReflectiveClassInformation<>(Foo.class), "BAR"));

		assertEquals(
				"Unknown constant for enum class eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$3Foo: BAR.",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo, String) with a custom class info type, throws IllegalArgumentException.")
	void member_withCustomClassInfoAndEnumString_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("test", new ReflectiveClassInformation<>(Object.class), "BAR"));

		assertEquals("Class must be an enum class: java.lang.Object.", exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo, String) sets an enum value.")
	void member_withEnumClassAndClassInfo_setsEnumValue() {
		enum TestEnum {TEST}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("test", new ReflectiveClassInformation<>(TestEnum.class), "TEST");

		AnnotationInformation annotation = builder.build();
		assertEquals(new EnumValue(new ReflectiveClassInformation<>(TestEnum.class), TestEnum.TEST),
				annotation.members().get("test"));
	}

	@Test
	@DisplayName("member(String, ClassInfo, String[]) with a custom class info type, throws IllegalArgumentException.")
	void member_withCustomClassInfoAndEnumStringArray_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("test", new CustomClassInfo(), new String[]{"test"}));

		assertEquals("Only ClassInfo instances created by the container are supported: class eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$CustomClassInfo", exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo, String[]) with a non-enum class, throws IllegalArgumentException.")
	void member_withNonEnumClassInfoAndEnumStringArray_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("test", new ReflectiveClassInformation<>(Runnable.class), new String[]{"BAR"}));

		assertEquals("Class must be an enum class: java.lang.Runnable.", exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo, String[]) with an unknown enum string, throws IllegalArgumentException.")
	void member_withClassInfoAndEnumStringArrayContainingUnknownConstant_throwsIllegalArgumentException() {
		enum Foo {BAR}
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("test", new ReflectiveClassInformation<>(Foo.class), new String[]{"BAR", "BAZ"}));

		assertEquals("Unknown constant for enum class eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$4Foo: BAZ.", exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo, String[]) sets an array of enum values.")
	void member_withClassInfoAndEnumStringArray_setsEnumValueArray() {
		enum TestEnum {A, B, C}
		var enumClassInformation = new ReflectiveClassInformation<TestEnum>(TestEnum.class);
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("test", enumClassInformation, new String[] {"A", "C"});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(List.of(new EnumValue(enumClassInformation, TestEnum.A), new EnumValue(enumClassInformation, TestEnum.C)));
		assertEquals(expectedValue, annotation.members().get("test"));
	}

	@Test
	@DisplayName("member(String, Class<?>) sets a type value.")
	void member_withClass_setsTypeValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("class", String.class);

		AnnotationInformation annotation = builder.build();
		assertEquals(new TypeValue(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class))),
				annotation.members().get("class"));
	}

	@Test
	@DisplayName("member(String, Class<?>[]) sets an array of type values.")
	void member_withClassArray_setsArrayOfTypeValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("classes", new Class[]{String.class, Object.class, Runnable.class});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(
				List.of(new TypeValue(new ClassTypeInstance(new ReflectiveClassInformation<>(String.class))),
						new TypeValue(new ClassTypeInstance(new ReflectiveClassInformation<>(Object.class))),
						new TypeValue(new ClassTypeInstance(new ReflectiveClassInformation<>(Runnable.class)))));
		assertEquals(expectedValue, annotation.member("classes"));
	}

	@Test
	@DisplayName("member(String, ClassInfo) with custom ClassInfo, throws IllegalArgumentException.")
	void member_withCustomClassInfo_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class, () -> builder.member("class", new CustomClassInfo()));

		assertEquals(
				"Only ClassInfo instances created by the container are supported: eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$CustomClassInfo",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo) with class information sets type value.")
	void member_withClassInformation_setsTypeValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		var classInformation = new ReflectiveClassInformation<>(String.class);

		builder.member("class", classInformation);

		AnnotationInformation annotation = builder.build();
		assertEquals(new TypeValue(new ClassTypeInstance(classInformation)), annotation.member("class"));
	}

	@Test
	@DisplayName(
			"member(String, ClassInfo[]) with class info array containing custom class info, throws IllegalArgumentException.")
	void member_withClassInfoArrayContainingCustomClassInfo_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class, () -> builder.member("class",
				new ClassInfo[]{new ReflectiveClassInformation<>(String.class), new CustomClassInfo()}));

		assertEquals(
				"Only ClassInfo instances created by the container are supported: eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$CustomClassInfo",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, ClassInfo[]) sets array of type values.")
	void member_withClassInfoArray_setsArrayOfTypeValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		var classInfo1 = new ReflectiveClassInformation<>(String.class);
		var classInfo2 = new ReflectiveClassInformation<>(Runnable.class);

		builder.member("classes", new ClassInfo[]{classInfo1, classInfo2});

		AnnotationInformation annotation = builder.build();
		assertEquals(new ArrayValue(
						List.of(new TypeValue(new ClassTypeInstance(classInfo1)), new TypeValue(new ClassTypeInstance(classInfo2)))),
				annotation.member("classes"));
	}

	@Test
	@DisplayName("member(String, AnnotationInfo) with custom annotation info, throws IllegalArgumentException.")
	void member_withCustomAnnotationInfo_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception =
				assertThrows(IllegalArgumentException.class, () -> builder.member("annotation", new CustomAnnotationInfo()));

		assertEquals(
				"Only AnnotationInfo instances created by the container are supported: eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$CustomAnnotationInfo",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, AnnotationInfo) sets a nested annotation value")
	void member_withAnnotationInfo_setsNestedAnnotationValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		SyntheticAnnotationInformation expectedAnnotationValue =
				new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Test.class), Map.of());

		builder.member("annotation", expectedAnnotationValue);

		AnnotationInformation annotation = builder.build();
		assertEquals(new NestedAnnotationValue(expectedAnnotationValue), annotation.member("annotation"));
	}

	@Test
	@DisplayName(
			"member(String, AnnotationInfo[]) with an array containing a custom annotation info, throws IllegalArgumentException.")
	void member_withContainingCustomAnnotationInfo_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class, () -> builder.member("annotations",
				new AnnotationInfo[]{new CustomAnnotationInfo(),
				                     new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Inject.class),
						                     Map.of())}));

		assertEquals(
				"Only AnnotationInfo instances created by the container are supported: eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$CustomAnnotationInfo",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, AnnotationInfo[]) sets an array of nested annotation values.")
	void member_withAnnotationInfoArray_setsNestedAnnotationValues() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		var nestedAnnotation1 = new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Test.class), Map.of());
		var nestedAnnotation2 =
				new SyntheticAnnotationInformation(new ReflectiveClassInformation<>(Inject.class), Map.of());

		builder.member("annotations", new AnnotationInfo[]{nestedAnnotation1, nestedAnnotation2});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(
				List.of(new NestedAnnotationValue(nestedAnnotation1), new NestedAnnotationValue(nestedAnnotation2)));
		assertEquals(expectedValue, annotation.member("annotations"));
	}

	@Test
	@DisplayName("member(String, Type) with a custom type, throws IllegalArgumentException.")
	void member_withCustomType_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class, () -> builder.member("type", new CustomType()));

		assertEquals(
				"Only Type instances created by the container are supported: eu.jbeernink.hypospray.model.information.synthetic.builder.SyntheticAnnotationInformationBuilderTest$CustomType",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, Type) with an unsupported type instance, throws IllegalArgumentException.")
	void member_withUnsupportedTypeInstance_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("type", TypeFactory.getInstance().wildcardUnbounded()));

		assertEquals(
				"Type must be one of the following: void, primitive, class or array of primitive types or classes: WildcardTypeInstance[upperBounds=[], lowerBounds=[], typeAnnotations=[]].",
				exception.getMessage());
	}

	@Test
	@DisplayName(
			"member(String, Type) with an array type with unsupported array element, throws IllegalArgumentException.")
	void member_withUnsupportedArrayType_throwsIllegalArgumentException() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		var typeFactory = TypeFactory.getInstance();

		var exception = assertThrows(IllegalArgumentException.class,
				() -> builder.member("type", typeFactory.ofArray(typeFactory.wildcardUnbounded(), 1)));

		assertEquals(
				"Array types in annotations only support primitive types and classes: WildcardTypeInstance[upperBounds=[], lowerBounds=[], typeAnnotations=[]].",
				exception.getMessage());
	}

	@Test
	@DisplayName("member(String, Type) with a void type, sets type value.")
	void member_withVoidType_setsTypeValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("void", (TypeInstance) VoidTypeInstance.VOID);

		AnnotationInformation annotation = builder.build();
		assertEquals(new TypeValue(VoidTypeInstance.VOID), annotation.member("void"));
	}

	@Test
	@DisplayName("member(String, Type) with a primitive type, sets a type value.")
	void member_withPrimitiveType_setsTypeValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("primitive", (TypeInstance) PrimitiveTypeInstance.INT);

		AnnotationInformation annotation = builder.build();
		assertEquals(new TypeValue(PrimitiveTypeInstance.INT), annotation.member("primitive"));
	}

	@Test
	@DisplayName("member(String, Type) with a class type, sets a type value.")
	void member_withClassType_setsTypeValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("class", TypeFactory.getInstance().of(SyntheticAnnotationInformationBuilderTest.class));

		AnnotationInformation annotation = builder.build();
		assertEquals(new TypeValue(
						new ClassTypeInstance(new ReflectiveClassInformation<>(SyntheticAnnotationInformationBuilderTest.class))),
				annotation.member("class"));
	}

	@Test
	@DisplayName("member(String, Type) with an array type, sets a type value.")
	void member_withArrayType_setsTypeValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		TypeFactory typeFactory = TypeFactory.getInstance();

		builder.member("array", typeFactory.ofArray(typeFactory.of(String.class), 1));

		AnnotationInformation annotation = builder.build();
		assertEquals(new TypeValue(new ArrayTypeInstance(typeFactory.of(String.class), List.of())), annotation.member("array"));
	}

	@Test
	@DisplayName("member(String, Type[]) with an array of types, sets an array of types as value.")
	void member_withArrayOfTypes_setsTypeArrayValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		TypeFactory typeFactory = TypeFactory.getInstance();

		builder.member("types", new Type[]{VoidTypeInstance.VOID, PrimitiveTypeInstance.BOOLEAN, typeFactory.ofObject(),
		                                   typeFactory.ofArray(typeFactory.of(String.class), 1),
		                                   typeFactory.ofArray(PrimitiveTypeInstance.CHAR, 1)});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(
				List.of(new TypeValue(VoidTypeInstance.VOID), new TypeValue(PrimitiveTypeInstance.BOOLEAN),
						new TypeValue(typeFactory.ofObject()), new TypeValue(new ArrayTypeInstance(typeFactory.of(String.class), List.of())),
						new TypeValue(new ArrayTypeInstance(PrimitiveTypeInstance.CHAR, List.of()))));
		assertEquals(expectedValue, annotation.member("types"));
	}

	@Test
	@DisplayName("member(name, Annotation) sets a nested annotation value.")
	void member_withAnnotation_setsAnnotationValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));
		var annotationValue = NamedLiteral.of("foo");

		builder.member("annotation", annotationValue);

		AnnotationInformation annotation = builder.build();
		assertEquals(new NestedAnnotationValue(new ReflectiveAnnotationInformation<>(annotationValue)),
				annotation.member("annotation"));
	}

	@Test
	@DisplayName("member(String, Annotation[]) sets an array of nested annotation values.")
	void member_withArrayOfAnnotations_setsAnnotationArrayValue() {
		var builder = new SyntheticAnnotationInformationBuilder(new ReflectiveClassInformation<>(Named.class));

		builder.member("annotations", new Annotation[]{NamedLiteral.of("bar"), InjectLiteral.INSTANCE});

		AnnotationInformation annotation = builder.build();
		var expectedValue = new ArrayValue(
				List.of(new NestedAnnotationValue(new ReflectiveAnnotationInformation<>(NamedLiteral.of("bar"))),
						new NestedAnnotationValue(new ReflectiveAnnotationInformation<>(InjectLiteral.INSTANCE))));
		assertEquals(expectedValue, annotation.member("annotations"));
	}

	private static class CustomClassInfo implements ClassInfo {

		@Override
		public String name() {
			return "";
		}

		@Override
		public String simpleName() {
			return "";
		}

		@Override
		public PackageInfo packageInfo() {
			return null;
		}

		@Override
		public List<TypeVariable> typeParameters() {
			return List.of();
		}

		@Override
		public Type superClass() {
			return null;
		}

		@Override
		public ClassInfo superClassDeclaration() {
			return null;
		}

		@Override
		public List<Type> superInterfaces() {
			return List.of();
		}

		@Override
		public List<ClassInfo> superInterfacesDeclarations() {
			return List.of();
		}

		@Override
		public boolean isPlainClass() {
			return false;
		}

		@Override
		public boolean isInterface() {
			return false;
		}

		@Override
		public boolean isEnum() {
			return true;
		}

		@Override
		public boolean isAnnotation() {
			return false;
		}

		@Override
		public boolean isRecord() {
			return false;
		}

		@Override
		public boolean isAbstract() {
			return false;
		}

		@Override
		public boolean isFinal() {
			return false;
		}

		@Override
		public int modifiers() {
			return 0;
		}

		@Override
		public Collection<MethodInfo> constructors() {
			return List.of();
		}

		@Override
		public Collection<MethodInfo> methods() {
			return List.of();
		}

		@Override
		public Collection<FieldInfo> fields() {
			return List.of();
		}

		@Override
		public Collection<RecordComponentInfo> recordComponents() {
			return List.of();
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
			return false;
		}

		@Override
		public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
			return false;
		}

		@Override
		public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
			return null;
		}

		@Override
		public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
			return List.of();
		}

		@Override
		public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
			return List.of();
		}

		@Override
		public Collection<AnnotationInfo> annotations() {
			return List.of();
		}
	}

	private static final class CustomAnnotationInfo implements AnnotationInfo {
		@Override
		public ClassInfo declaration() {
			return null;
		}

		@Override
		public boolean hasMember(String name) {
			return false;
		}

		@Override
		public AnnotationMember member(String name) {
			return null;
		}

		@Override
		public Map<String, AnnotationMember> members() {
			return Map.of();
		}
	}

	private static final class CustomType implements Type {

		@Override
		public Kind kind() {
			return Kind.WILDCARD_TYPE;
		}

		@Override
		public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
			return false;
		}

		@Override
		public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
			return false;
		}

		@Override
		public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
			return null;
		}

		@Override
		public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
			return List.of();
		}

		@Override
		public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
			return List.of();
		}

		@Override
		public Collection<AnnotationInfo> annotations() {
			return List.of();
		}
	}
}