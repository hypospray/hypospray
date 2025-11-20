package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import static eu.jbeernink.hypospray.core.todo.Todo.warnNotYetImplemented;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.DisposerInfo;
import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.inject.build.compatible.spi.StereotypeInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.types.Type;
import jakarta.inject.Named;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer.BeanProducerInformation;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer.ConstructorBeanProducerInformation;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer.FieldBeanProducerInformation;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.producer.MethodBeanProducerInformation;
import eu.jbeernink.hypospray.model.information.AnnotationInformation;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.FieldInformation;
import eu.jbeernink.hypospray.model.information.MethodInformation;
import eu.jbeernink.hypospray.model.types.TypeInstance;

public record BeanInformation<T>(ScopeInformation scope, List<TypeInstance> typeInstances,
                                 Set<AnnotationInformation> qualifierInformation, ClassInformation<T> declaringClass,
                                 boolean isSynthetic, BeanProducerInformation<T> beanProducer, Integer priority,
                                 DisposerInfo disposer, List<StereotypeInfo> stereotypes) implements BeanInfo {


	@Override
	public boolean isClassBean() {
		return beanProducer instanceof ConstructorBeanProducerInformation<?>;
	}

	@Override
	public boolean isProducerMethod() {
		return beanProducer instanceof MethodBeanProducerInformation<?>;
	}

	@Override
	public boolean isProducerField() {
		return beanProducer instanceof FieldBeanProducerInformation<?>;
	}

	@Override
	public MethodInformation producerMethod() {
		return switch (beanProducer) {
			case MethodBeanProducerInformation(MethodConfiguration producerMethod, _, _) ->
					producerMethod.info();
			case ConstructorBeanProducerInformation<?> _ -> null;
			case FieldBeanProducerInformation<?> _ -> null;
		};
	}

	@Override
	public FieldInformation producerField() {
		return switch (beanProducer) {
			case MethodBeanProducerInformation<?> _ -> null;
			case ConstructorBeanProducerInformation<?> _ -> null;
			case FieldBeanProducerInformation(FieldInformation fieldInformation, _) -> fieldInformation;
		};
	}

	@Override
	public boolean isAlternative() {
		warnNotYetImplemented();
		return false;
	}

	@Override
	public String name() {
		return qualifierInformation.stream()
		                           .filter(qualifier -> qualifier.name().equals(Named.class.getName()))
		                           .map(info -> info.member("value").asString())
		                           .findFirst()
		                           .orElse(null);
	}

	@Override
	public DisposerInfo disposer() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Collection<Type> types() {
		return List.copyOf(typeInstances);
	}

	@Override
	public Collection<AnnotationInfo> qualifiers() {
		return Set.copyOf(qualifierInformation);
	}

	@Override
	public List<StereotypeInfo> stereotypes() {
		return List.copyOf(stereotypes);
	}

	@Override
	public Set<InjectionPointInfo> injectionPoints() {
		return Set.copyOf(beanProducer.injectionPoints());
	}
}
