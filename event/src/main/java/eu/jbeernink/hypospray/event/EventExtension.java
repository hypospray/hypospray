package eu.jbeernink.hypospray.event;

import static eu.jbeernink.hypospray.core.priority.ExtensionPriority.EVENT_EXTENSION;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

import eu.jbeernink.hypospray.core.event.DummyEventInstance;

@Priority(EVENT_EXTENSION)
public class EventExtension implements BuildCompatibleExtension {

	@Discovery
	public void registerClasses(ScannedClasses scannedClasses) {
		scannedClasses.add(EventInstanceProducer.class.getName());
		scannedClasses.add(EventBus.class.getName());
	}

	@Enhancement(types = {DummyEventInstance.class})
	public void vetoDummyEventInstance(ClassConfig classConfig) {
		classConfig.addAnnotation(AnnotationBuilder.of(Vetoed.class).build());
	}
}
