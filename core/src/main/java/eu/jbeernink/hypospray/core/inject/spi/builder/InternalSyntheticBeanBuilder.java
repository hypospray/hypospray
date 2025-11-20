package eu.jbeernink.hypospray.core.inject.spi.builder;

import static eu.jbeernink.hypospray.core.todo.Todo.unimplemented;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.InvokerInfo;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.invoke.Invoker;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;

import org.jspecify.annotations.Nullable;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ImmutableParameters;
import eu.jbeernink.hypospray.core.inject.spi.DiscoveredBean;
import eu.jbeernink.hypospray.core.inject.spi.producer.SyntheticBeanProducer;
import eu.jbeernink.hypospray.core.invoke.ReflectiveConstructorInvoker;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.source.ClassInformationSource;
import eu.jbeernink.hypospray.model.types.TypeInstance;

/// Builder for creating instances of synthetic beans.
@Nullable
public class InternalSyntheticBeanBuilder<T> implements SyntheticBeanBuilder<T> {

	private final Map<String, Object> beanParameters = new HashMap<>();

	private @Nullable Invoker<Void, ? extends SyntheticBeanCreator<T>> creatorConstructorInvoker;
	private Invoker<Void, ? extends SyntheticBeanDisposer<T>> disposerConstructorInvoker =
			(_, _) -> new NoOpSyntheticBeanDisposer<>();

	private final DiscoveredBean.Builder<T> discoveredBeanBuilder;
	private final Supplier<BeanContainer> beanContainerSupplier;

	public InternalSyntheticBeanBuilder(Class<T> beanClass, Supplier<BeanContainer> beanContainerSupplier) {
		this(ClassInformationSource.getInstance().getClassInformation(beanClass), beanContainerSupplier);
	}

	public InternalSyntheticBeanBuilder(ClassInformation<T> beanClass, Supplier<BeanContainer> beanContainerSupplier) {
		discoveredBeanBuilder = DiscoveredBean.newBuilder(beanClass);
		this.beanContainerSupplier = beanContainerSupplier;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> type(Class<?> type) {
		discoveredBeanBuilder.addType(type);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> type(ClassInfo type) {
		return type(type.asType());
	}

	@Override
	public InternalSyntheticBeanBuilder<T> type(Type type) {
		switch (type) {
			case TypeInstance typeInstance -> {
				discoveredBeanBuilder.addType(typeInstance);

				return this;
			}
			case Type _ -> throw new IllegalArgumentException(
					"Only types that have been created by the container are supported: %s.".formatted(type.getClass().getName()));
		}
	}

	@Override
	public InternalSyntheticBeanBuilder<T> qualifier(Class<? extends Annotation> annotationType) {
		AnnotationInfo info = AnnotationBuilder.of(annotationType).build();

		return qualifier(info);
	}

	@Override
	public InternalSyntheticBeanBuilder<T> qualifier(AnnotationInfo qualifierAnnotation) {
		switch (qualifierAnnotation) {
			case AnnotationInformation annotationInformation -> discoveredBeanBuilder.addQualifier(annotationInformation);
			case AnnotationInfo annotationInfo -> throw new IllegalArgumentException(
					"Only AnnotationInfo instances created by the container are supported: %s.".formatted(
							annotationInfo.getClass().getName()));
		}

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> qualifier(Annotation qualifierAnnotation) {
		discoveredBeanBuilder.addQualifier(qualifierAnnotation);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> scope(Class<? extends Annotation> scopeAnnotation) {
		discoveredBeanBuilder.setScope(scopeAnnotation);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> alternative(boolean isAlternative) {
		discoveredBeanBuilder.setAlternative(isAlternative);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> priority(int priority) {
		unimplemented();

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> name(String beanName) {
		discoveredBeanBuilder.setElName(beanName);

		return this;
	}

	@Override
	public SyntheticBeanBuilder<T> stereotype(Class<? extends Annotation> stereotypeAnnotation) {
		return unimplemented();
	}

	@Override
	public SyntheticBeanBuilder<T> stereotype(ClassInfo stereotypeAnnotation) {
		return unimplemented();
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, boolean value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, boolean[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, int value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, int[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, long value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, long[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, double value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, double[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, String value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, String[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, Enum<?> value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, Enum<?>[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, Class<?> value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, ClassInfo value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, Class<?>[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, ClassInfo[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, AnnotationInfo value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, Annotation value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, AnnotationInfo[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, Annotation[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, InvokerInfo value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> withParam(String key, InvokerInfo[] value) {
		beanParameters.put(key, value);

		return this;
	}

	@Override
	public InternalSyntheticBeanBuilder<T> createWith(Class<? extends SyntheticBeanCreator<T>> creatorClass) {
		if (this.creatorConstructorInvoker != null) {
			throw new IllegalStateException("createWith can only be called on a SyntheticBeanBuilder.");
		}

		try {
			Constructor<? extends SyntheticBeanCreator<T>> beanCreatorConstructor = creatorClass.getConstructor();

			if (isPublic(beanCreatorConstructor)) {
				throw new IllegalArgumentException(
						"The no-args constructor on a synthetic bean creator must be public: %s".formatted(creatorClass.getName()));
			}

			creatorConstructorInvoker = new ReflectiveConstructorInvoker<>(beanCreatorConstructor);

			return this;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"A synthetic bean creator must have a public no-args constructor: %s.".formatted(creatorClass.getName()));
		}
	}

	@Override
	public InternalSyntheticBeanBuilder<T> disposeWith(Class<? extends SyntheticBeanDisposer<T>> disposerClass) {
		try {
			Constructor<? extends SyntheticBeanDisposer<T>> beanDisposerConstructor = disposerClass.getConstructor();

			if (isPublic(beanDisposerConstructor)) {
				throw new IllegalArgumentException(
						"The no-args constructor on a synthetic bean disposer must be public: %s".formatted(
								disposerClass.getName()));
			}

			disposerConstructorInvoker = new ReflectiveConstructorInvoker<>(beanDisposerConstructor);

		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					"A synthetic bean disposer must have a public no-args constructor: %s.".formatted(disposerClass.getName()));
		}

		return this;
	}

	public DiscoveredBean<T> build() {
		if (creatorConstructorInvoker == null) {
			throw new DefinitionException("The bean creator must be defined for a synthetic bean.");
		}

		SyntheticBeanProducer<T> beanProducer =
				new SyntheticBeanProducer<>(beanContainerSupplier.get(), new ImmutableParameters(beanParameters),
						creatorConstructorInvoker, disposerConstructorInvoker);

		return discoveredBeanBuilder.setInjectionTarget(beanProducer).build();
	}

	private static boolean isPublic(Constructor<?> constructor) {
		return (constructor.getModifiers() & Modifier.PUBLIC) == 0;
	}
}
