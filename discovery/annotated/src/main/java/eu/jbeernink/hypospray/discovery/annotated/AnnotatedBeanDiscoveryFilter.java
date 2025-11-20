package eu.jbeernink.hypospray.discovery.annotated;

import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.ANNOTATED;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.DeploymentException;

import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryFilter;
import eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode;

public class AnnotatedBeanDiscoveryFilter implements BeanDiscoveryFilter {

	@Override
	public BeanDiscoveryMode discoveryMode() {
		return ANNOTATED;
	}

	@Override
	public boolean isCandidateClass(String className) {
		BeanContainer beanContainer = CDI.current().getBeanContainer();

		return hasBeanDefiningAnnotations(className, beanContainer);
	}

	private boolean hasBeanDefiningAnnotations(String discoveredClass, BeanContainer beanContainer) {
		try {
			Class<?> beanClass = Class.forName(discoveredClass);

			return Arrays.stream(beanClass.getAnnotations())
			             .anyMatch(annotation -> isBeanDefiningAnnotation(annotation, beanContainer));
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("Unable to load bean class: " + discoveredClass, e);
		}
	}

	private boolean isBeanDefiningAnnotation(Annotation annotation, BeanContainer beanContainer) {
		Class<? extends Annotation> annotationClass = annotation.annotationType();

		return annotation instanceof Dependent || beanContainer.isQualifier(annotationClass) ||
		       beanContainer.isNormalScope(annotationClass) || beanContainer.isInterceptorBinding(annotationClass) ||
		       beanContainer.isStereotype(annotationClass);
	}
}
