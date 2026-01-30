import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.BuildServices;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.enterprise.inject.spi.CDIProvider;

import eu.jbeernink.hypospray.compile.annotation.GenerateServiceDescriptors;
import eu.jbeernink.hypospray.core.ContainerBuildServices;
import eu.jbeernink.hypospray.core.ContainerInitializer;
import eu.jbeernink.hypospray.core.ContainerProvider;
import eu.jbeernink.hypospray.core.inject.spi.factory.ContextualFactory;
import eu.jbeernink.hypospray.core.inject.spi.factory.ManagedBeanFactory;
import eu.jbeernink.hypospray.core.inject.spi.factory.ManagedInterceptorFactory;
import eu.jbeernink.hypospray.core.invoke.GeneratedInvokerFactoryManager;
import eu.jbeernink.hypospray.invoker.factory.InvokerFactoryManager;

@GenerateServiceDescriptors
module eu.jbeernink.hypospray.core {
	requires jakarta.cdi;
	requires static jakarta.el;
	requires eu.jbeernink.hypospray.codegeneration.generator;
	requires eu.jbeernink.hypospray.model;
	requires eu.jbeernink.hypospray.invoker;
	requires eu.jbeernink.hypospray.util;
	requires org.jspecify;
	requires eu.jbeernink.hypospray.compile.annotation;

	exports eu.jbeernink.hypospray.core.annotation to eu.jbeernink.hypospray.event;
	exports eu.jbeernink.hypospray.core.event to eu.jbeernink.hypospray.event;
	exports eu.jbeernink.hypospray.core.priority to eu.jbeernink.hypospray.event, eu.jbeernink.hypospray.scope.application, eu.jbeernink.hypospray.scope.request, eu.jbeernink.hypospray.discovery.scanner;

	provides BuildServices with ContainerBuildServices;
	provides SeContainerInitializer with ContainerInitializer;
	provides CDIProvider with ContainerProvider;
	provides ContextualFactory with ManagedInterceptorFactory, ManagedBeanFactory;
	provides InvokerFactoryManager with GeneratedInvokerFactoryManager;

	uses BuildCompatibleExtension;
	uses ContextualFactory;
}