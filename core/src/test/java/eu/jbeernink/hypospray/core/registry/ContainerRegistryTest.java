package eu.jbeernink.hypospray.core.registry;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.annotation.Wildcard;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.model.TypeFactory;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveAnnotationInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;
import eu.jbeernink.hypospray.model.types.ParameterizedTypeInstance;
import eu.jbeernink.hypospray.model.types.reflection.ParameterizedTypeImpl;

@DisplayName("ContainerRegistry")
class ContainerRegistryTest {

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	private ContainerRegistry registry;

	@BeforeEach
	void setup() {
		registry = new ContainerRegistry();
	}

	@Nested
	@DisplayName("with no beans")
	class WithNoBeans {

		@Test
		@DisplayName(
				"resolveBeans(Type, Set<Annotation>) with object and no qualifiers and will return an empty set of beans.")
		void resolveBeans_withObjectAndNoQualifiers_returnsEmptySet() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.ofObject(), Set.of());

			assertEquals(Set.of(), beans);
		}

		@Test
		@DisplayName("registerBean(Bean<?>) adds a bean to the registry.")
		void registerBean_addsBeanToRegistry() {
			Bean<String> bean = createBean(String.class);

			registry.registerBean(bean);

			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(String.class), Set.of());
			assertEquals(Set.of(bean), beans);
		}
	}

	@Nested
	@DisplayName("with multiple beans")
	class WithMultipleBeans {

		Bean<String> defaultBean;
		Bean<String> qualifiedBean;

		@BeforeEach
		void setup() {
			defaultBean = createBean(String.class);
			registry.registerBean(defaultBean);

			qualifiedBean = createBean(String.class, Set.of(Qualified.Literal.INSTANCE));
			registry.registerBean(qualifiedBean);
		}

		@Test
		@DisplayName("resolveBeans(Type, Set<Annotation>) with Object and without qualifiers matches all beans.")
		void resolveBeans_withObjectAndWithoutQualifier_matchesAllBeans() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.ofObject(), Set.of());

			assertEquals(Set.of(defaultBean, qualifiedBean), beans);
		}

		@Test
		@DisplayName("resolveBeans(Type, Set<Annotation>) without qualifier matches all beans of the correct type.")
		void resolveBeans_withoutQualifier_matchesAllBeansOfMatchingType() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(String.class), Set.of());

			assertEquals(Set.of(defaultBean, qualifiedBean), beans);
		}

		@Test
		@DisplayName("resolveBeans(Type, Set<Annotation>) with Object and a qualifier matches only qualified beans.")
		void resolveBeans_withObjectAndQualifier_matchesDefaultQualifiedBean() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.ofObject(), Set.of(Qualified.Literal.INSTANCE));

			assertEquals(Set.of(qualifiedBean), beans);
		}

		@Test
		@DisplayName(
				"resolveBeans(Type, Set<Annotation>) with a qualifier matches only beans of matching type and qualifier.")
		void resolveBeans_withQualifier_matchesOnlyQualifiedBean() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(String.class), Set.of(Qualified.Literal.INSTANCE));

			assertEquals(Set.of(qualifiedBean), beans);
		}

		@Test
		@DisplayName(
				"resolveBeans(Type, Set<Annotation> with Object and @Default qualifier matches the @Default qualified bean.")
		void resolveBeans_withObjectAndDefaultQualifiers_matchesDefaultQualifiedBean() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.ofObject(), Set.of(Default.Literal.INSTANCE));

			assertEquals(Set.of(defaultBean), beans);
		}

		@Test
		@DisplayName("resolveBeans(Type, Set<Annotation>) with @Default qualifier matches only @Default qualified bean.")
		void resolveBeans_withDefaultQualifier_matchesOnlyDefaultQualifiedBean() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(String.class), Set.of(Default.Literal.INSTANCE));

			assertEquals(Set.of(defaultBean), beans);
		}
	}

	@Nested
	@DisplayName("with beans with multiple qualifiers")
	class WithBeansWithMultipleQualifiers {

		Bean<String> defaultBean;
		Bean<String> singleQualifierBean;
		Bean<String> multipleQualifierBean;

		@BeforeEach
		void setup() {
			defaultBean = createBean(String.class);
			registry.registerBean(defaultBean);

			singleQualifierBean = createBean(String.class, Set.of(Qualified.Literal.INSTANCE));
			registry.registerBean(singleQualifierBean);

			multipleQualifierBean =
					createBean(String.class, Set.of(Qualified.Literal.INSTANCE, SecondQualifier.Literal.INSTANCE));
			registry.registerBean(multipleQualifierBean);
		}

		@Test
		@DisplayName(
				"resolveBeans(Type, Set<Annotation>) with matching type and single qualifier returns all beans with matching qualifier.")
		void resolveBeans_withMatchingTypeAndSingleQualifier_returnsBeansWithMatchingQualifier() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(String.class), Set.of(Qualified.Literal.INSTANCE));

			assertEquals(Set.of(singleQualifierBean, multipleQualifierBean), beans);
		}

		@Test
		@DisplayName(
				"resolveBeans(Type, Set<Annotation>) with matching type and mulitple qualifiers matches only beans with all required qualifiers.")
		void resolveBeans_withMatchingTypeAndMultipleQualifiers_returnsBeansWithAllRequiredQualifiers() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(String.class),
					Set.of(Qualified.Literal.INSTANCE, SecondQualifier.Literal.INSTANCE));

			assertEquals(Set.of(multipleQualifierBean), beans);
		}
	}

	@Nested
	@DisplayName("with bean with wildcard qualifier")
	class WithBeanWithWildcardQualifier {

		Bean<String> singleQualifierBean;
		Bean<String> wildcardQualifierBean;

		@BeforeEach
		void setup() {
			singleQualifierBean = createBean(String.class, Set.of(Qualified.Literal.INSTANCE));
			registry.registerBean(singleQualifierBean);

			wildcardQualifierBean = createBean(String.class, Set.of(Wildcard.Literal.INSTANCE));
			registry.registerBean(wildcardQualifierBean);
		}

		@Test
		@DisplayName(
				"resolveBeans(TypeInstance, Set<Annotation>) with default qualifier, returns bean with wildcard qualifier.")
		void resolveBeans_withDefaultQualifier_willReturnWildcardBean() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.ofObject(), Set.of(Default.Literal.INSTANCE));

			assertEquals(Set.of(wildcardQualifierBean), beans);
		}
	}

	@Nested
	@DisplayName("with beans with generic type signatures")
	class WithBeanWithGenericTypeSignatures {

		@SuppressWarnings("unused")
		interface Foo<E> {}

		@SuppressWarnings("unused")
		class Bar<E> {}

		@SuppressWarnings("unused")
		class Baz extends Bar<String> implements Foo<String> {}

		Bean<Bar> typeParameterBean;
		Bean<Baz> concreteGenericTypeBean;

		@BeforeEach
		void setup() {
			TypeVariable<Class<Bar>> typeVariable = Bar.class.getTypeParameters()[0];
			typeParameterBean = createBean(Bar.class, Set.of(Default.Literal.INSTANCE),
					Set.of(Bar.class, new ParameterizedTypeImpl(Foo.class, null, List.of(typeVariable))));
			registry.registerBean(typeParameterBean);

			concreteGenericTypeBean = createBean(Baz.class, Set.of(Default.Literal.INSTANCE),
					Set.of(Baz.class, new ParameterizedTypeImpl(Bar.class, null, List.of(String.class)),
							new ParameterizedTypeImpl(Foo.class, null, List.of(String.class))));
			registry.registerBean(concreteGenericTypeBean);
		}

		@Test
		@DisplayName(
				"resolveBeans(TypeInstance, Set<Annotation>) with matching concrete type, will return both concrete generic type and type parameter beans.")
		void resolveBeans_withMatchingConcreteType_willReturnBothConcreteGenericBeanAndTypeParameterBeans() {
			ParameterizedTypeInstance parameterizedTypeInstance =
					TypeFactory.getInstance().parameterized(Bar.class, String.class);
			Set<Bean<?>> beans = registry.resolveBeans(parameterizedTypeInstance, Set.of(Default.Literal.INSTANCE));

			assertEquals(Set.of(concreteGenericTypeBean, typeParameterBean), beans);
		}

		@Test
		@DisplayName(
				"resolveBeans(TypeInstance, Set<Annotation>) with non matching concrete type, will return type parameter bean.")
		void resolveBeans_withNonMatchingConcreteType_willReturnTypeParameterBean() {
			Set<Bean<?>> beans =
					registry.resolveBeans(typeFactory.parameterized(Bar.class, Number.class), Set.of(Default.Literal.INSTANCE));

			assertEquals(Set.of(typeParameterBean), beans);
		}

		@Test
		@DisplayName("resolveBeans(TypeInstance, Set<Annotation>) with raw type, will return all matching generic beans.")
		void resolveBeans_withRawType_willReturnAllMatchingGenericBeans() {
			Set<Bean<?>> beans = registry.resolveBeans(typeFactory.of(Bar.class), Set.of(Default.Literal.INSTANCE));

			assertEquals(Set.of(concreteGenericTypeBean, typeParameterBean), beans);
		}
	}

	private static <T> Bean<T> createBean(Class<T> type) {
		return createBean(type, Set.of(Default.Literal.INSTANCE));
	}

	private static <T> Bean<T> createBean(Class<T> type, Set<Annotation> qualifiers) {
		return new DiscoveredBean<>(new ReflectiveClassInformation<>(type),
				Set.of(TypeFactory.getInstance().of(type), TypeFactory.getInstance().of(Object.class)),
				qualifiers.stream().map(ReflectiveAnnotationInformation::new).collect(toUnmodifiableSet()),
				new ReflectiveClassInformation<>(Dependent.class), Set.of(), null, null, false);
	}

	private static <T> Bean<T> createBean(Class<T> type, Set<Annotation> qualifiers, Set<Type> types) {
		return new DiscoveredBean<>(new ReflectiveClassInformation<>(type),
				types.stream().map(TypeFactory.getInstance()::fromJavaType).collect(toUnmodifiableSet()),
				qualifiers.stream().map(ReflectiveAnnotationInformation::new).collect(toUnmodifiableSet()),
				new ReflectiveClassInformation<>(Dependent.class), Set.of(), null, null, false);
	}

	@Qualifier
	private @interface Qualified {
		@SuppressWarnings({"ClassExplicitlyAnnotation"})
		final class Literal extends AnnotationLiteral<Qualified> implements Qualified {
			static final Literal INSTANCE = new Literal();
		}
	}

	@Qualifier
	private @interface SecondQualifier {
		@SuppressWarnings({"ClassExplicitlyAnnotation"})
		final class Literal extends AnnotationLiteral<SecondQualifier> implements SecondQualifier {
			static final Literal INSTANCE = new Literal();
		}
	}
}