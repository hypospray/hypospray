package eu.jbeernink.hypospray.demo.synthetic;

import java.lang.System.Logger;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;

public class BeanProducer implements SyntheticBeanCreator<String>, SyntheticBeanDisposer<String> {
	private static final Logger logger = System.getLogger(BeanProducer.class.getName());

	@Override
	public String create(Instance<Object> lookup, Parameters params) {
		return "This is a synthetic bean!";
	}

	@Override
	public void dispose(String instance, Instance<Object> lookup, Parameters params) {
		logger.log(Logger.Level.INFO, "Disposing " + instance);
	}
}
