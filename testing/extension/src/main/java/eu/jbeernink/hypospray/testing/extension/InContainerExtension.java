package eu.jbeernink.hypospray.testing.extension;


import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestInstantiationException;

public class InContainerExtension implements Extension, TestInstanceFactory, TestInstancePreDestroyCallback {

	private SeContainer container;

	@Override
	public Object createTestInstance(TestInstanceFactoryContext testInstanceFactoryContext,
	                                 ExtensionContext extensionContext) throws TestInstantiationException {
		container = SeContainerInitializer.newInstance().initialize();
		return container.select(testInstanceFactoryContext.getTestClass()).get();
	}

	@Override
	public void preDestroyTestInstance(ExtensionContext extensionContext) {
		container.close();
	}

}
