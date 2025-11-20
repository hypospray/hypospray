package eu.jbeernink.hypospray.core.test.extension;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;

public class FakeBuildCompatibleExtension implements BuildCompatibleExtension {

	public sealed interface LifecyclePhase {}

	public record DiscoveryPhase(ScannedClasses scannedClasses, MetaAnnotations metaAnnotations,
	                             Messages messages) implements LifecyclePhase {}

	private enum Phase {DISCOVERY, REGISTRATION}

	public record Call(Phase phase, List<Object> parameters) {}

	private static final Queue<LifecyclePhase> callLog = new ConcurrentLinkedQueue<>();


	@Discovery
	public void discovery(ScannedClasses scannedClasses, MetaAnnotations metaAnnotations, Messages messages) {
		callLog.add(new DiscoveryPhase(scannedClasses, metaAnnotations, messages));
	}

	public static List<LifecyclePhase> phases() {
		return List.copyOf(callLog);
	}

	public static void reset() {
		callLog.clear();
	}
}
