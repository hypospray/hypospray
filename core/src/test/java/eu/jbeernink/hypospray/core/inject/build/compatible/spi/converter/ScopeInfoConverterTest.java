package eu.jbeernink.hypospray.core.inject.build.compatible.spi.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Scope;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.core.context.spi.CreationalContextManager;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ScopeInformation;
import eu.jbeernink.hypospray.core.inject.spi.InjectableBeanContainer;
import eu.jbeernink.hypospray.core.registry.ContainerRegistry;
import eu.jbeernink.hypospray.model.information.ClassInformation;
import eu.jbeernink.hypospray.model.information.reflection.ReflectiveClassInformation;

class ScopeInfoConverterTest {

	private ScopeInfoConverter scopeInfoConverter;
	private BeanContainer beanContainer;

	@BeforeEach
	void setup() {
		beanContainer = new InjectableBeanContainer(new CreationalContextManager(), new ContainerRegistry());
		scopeInfoConverter = new ScopeInfoConverter();
	}

	@Test
	@DisplayName(
			"apply(Class<? extends Annotation>, BeanContainer) with normal scope annotation, returns normal scoped ScopeInformation.")
	void apply_withNormalScopedAnnotation_returnsExpectedScopeInformation() {
		ScopeInformation scopeInformation = scopeInfoConverter.apply(NormalScoped.class, beanContainer);

		ClassInformation<NormalScoped> annotationClassInfo = new ReflectiveClassInformation<>(NormalScoped.class);
		assertEquals(new ScopeInformation(annotationClassInfo, true), scopeInformation);
	}

	@Test
	@DisplayName(
			"apply(Class<? extends Annotation>, BeanContainer) with non-normal scope annotation, returns non-normal scoped ScopeInformation.")
	void apply_withNonNormalScopedAnnotation_returnsExpectedScopeInformation() {
		ScopeInformation scopeInformation = scopeInfoConverter.apply(NonNormalScoped.class, beanContainer);

		ClassInformation<NonNormalScoped> annotationClassInfo = new ReflectiveClassInformation<>(NonNormalScoped.class);
		assertEquals(new ScopeInformation(annotationClassInfo, false), scopeInformation);
	}

	@NormalScope
	@interface NormalScoped {}

	@Scope
	@interface NonNormalScoped {}
}