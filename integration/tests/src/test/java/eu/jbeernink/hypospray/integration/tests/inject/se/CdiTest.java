package eu.jbeernink.hypospray.integration.tests.inject.se;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.jbeernink.hypospray.integration.tests.util.testing.beans.BeanInterface;
import eu.jbeernink.hypospray.integration.tests.util.testing.beans.Qualified;
import eu.jbeernink.hypospray.integration.tests.util.testing.beans.QualifiedBeanA;
import eu.jbeernink.hypospray.integration.tests.util.testing.beans.QualifiedBeanB;
import eu.jbeernink.hypospray.integration.tests.util.testing.beans.UnqualifiedBean;
import eu.jbeernink.hypospray.integration.tests.util.testing.beans.UnrelatedQualifiedBean;
import eu.jbeernink.hypospray.testing.extension.InContainer;

@InContainer
@DisplayName("CDI")
public class CdiTest {

	private CDI<Object> cdi;

	@BeforeEach
	void setup() {
		cdi = CDI.current();
	}

	@Test
	@DisplayName("current() returns a reference to the current container.")
	void current_returnsReferenceToCurrentContainer() {
		CDI<Object> current = CDI.current();

		assertInstanceOf(SeContainer.class, current);
	}

	@Test
	@DisplayName("select(Class<?>, Annotation... qualifiers) with single annotation limits the set of beans.")
	void select_limitsTheSetOfBeans() {
		Instance<BeanInterface> instance = cdi.select(BeanInterface.class, Qualified.Literal.INSTANCE);

		assertEquals(Set.of(QualifiedBeanA.class, QualifiedBeanB.class),
				instance.handlesStream().map(handle -> handle.getBean().getBeanClass()).collect(toSet()));
	}

	@Test
	@DisplayName("select(Class<?>, Annotation... qualifiers) with no qualifiers limits the set of beans.")
	void select_withoutAnnotations_limitsTheSetOfBeans() {
		Instance<BeanInterface> instance = cdi.select(BeanInterface.class);

		assertEquals(Set.of(UnqualifiedBean.class),
				instance.handlesStream().map(handle -> handle.getBean().getBeanClass()).collect(toSet()));
	}

	@Test
	@DisplayName("select(Annotation... qualifiers) with a single annotation limits the set of beans.")
	void select_withAnnotation_limitsTheSetOfBeans() {
		Instance<Object> instance = cdi.select(Qualified.Literal.INSTANCE);

		assertEquals(Set.of(QualifiedBeanA.class, QualifiedBeanB.class, UnrelatedQualifiedBean.class),
				instance.handlesStream().map(handle -> handle.getBean().getBeanClass()).collect(toSet()));
	}

	@Test
	@DisplayName("get() throws AmbiguousResolutionException.")
	void get_throwsAmbiguousResolutionException() {
		assertThrows(AmbiguousResolutionException.class, () -> cdi.get());
	}

	@Test
	@DisplayName("getHandle() throws AmbiguousResolutionException.")
	void getHandle_throwsAmbiguousResolutionException() {
		assertThrows(AmbiguousResolutionException.class, () -> cdi.getHandle());
	}

	@Test
	@DisplayName("getBeanContainer() returns a BeanContainer.")
	void getBeanContainer_returnsBeanContainer() {
		BeanContainer beanContainer = cdi.getBeanContainer();

		assertNotNull(beanContainer);
	}

	@Test
	@Disabled
	@DisplayName("getBeanManager() returns a BeanManager.")
	void getBeanManager_returnsBeanManager() {
		BeanManager beanManager = cdi.getBeanManager();

		assertNotNull(beanManager);
	}
}
