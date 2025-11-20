package eu.jbeernink.hypospray.demo.synthetic;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;

public class SyntheticBeanRegistrationExtension implements BuildCompatibleExtension {

	@Synthesis
	public void registerSyntheticBean(SyntheticComponents components) {
		components.addBean(String.class)
				.type(String.class)
				.qualifier(SyntheticBean.Literal.INSTANCE)
				.createWith(BeanProducer.class)
				.disposeWith(BeanProducer.class);
	}
}
