package eu.jbeernink.hypospray.demo.extension;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;

import eu.jbeernink.hypospray.demo.BeanWithDependencies;
import eu.jbeernink.hypospray.demo.ScopedBean;
import eu.jbeernink.hypospray.demo.UnscopedBean;

public class DemoBuildCompatibleExtension implements BuildCompatibleExtension {

	@Discovery
	void discovery(ScannedClasses scannedClasses, MetaAnnotations metaAnnotations, Messages messages) {
		messages.info("Discovery phase!");
	}

	@Enhancement(types = {ScopedBean.class, UnscopedBean.class})
	void enhancement(ClassConfig classConfig, Messages messages) {
		messages.info("Enhancement!");
	}

	@Enhancement(types = {ScopedBean.class, UnscopedBean.class})
	void enhancement(MethodInfo methodInfo, Messages messages) {
		messages.info(methodInfo.toString());
	}

	@Enhancement(types = {BeanWithDependencies.class})
	void enhancement(ClassInfo beanWithDependenciesInfo, Messages messages) {
		messages.info(beanWithDependenciesInfo.methods() + "");
	}

	@Synthesis
	void synthesis(SyntheticComponents syntheticComponents, Messages messages) {
		messages.info("Synthesis!");
	}

	@Registration(types = {ScopedBean.class, UnscopedBean.class})
	void registration(BeanInfo beanInfo, Messages messages) {
		messages.info("Registered bean!", beanInfo);
	}

	@Registration(types = {BeanWithDependencies.class})
	void registerBeanWithDependencies(BeanInfo info, Messages messages) {
		messages.info("Bean with dependencies", info);
	}

	@Validation
	void validation(Messages messages) {
		messages.info("Validation phase!");
	}

}
