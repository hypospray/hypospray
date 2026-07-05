package eu.jbeernink.hypospray.discovery.scanner;

import static eu.jbeernink.hypospray.core.priority.ExtensionPriority.BEAN_DISCOVERY_EXTENSION;
import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.ALL;
import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.ANNOTATED;
import static eu.jbeernink.hypospray.discovery.scanner.BeanDiscoveryMode.NONE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.spi.DeploymentException;

import eu.jbeernink.hypospray.xml.BeansConfigReader;
import eu.jbeernink.hypospray.xml.model.BeanConfig;

@Priority(BEAN_DISCOVERY_EXTENSION)
public class BeanDiscoveryExtension implements BuildCompatibleExtension {

	private final BeanDiscoveryMode DEFAULT_DISCOVERY_MODE = ANNOTATED;

	private final Map<BeanDiscoveryMode, BeanDiscoveryFilter> beanDiscoveryFilters;

	public BeanDiscoveryExtension() {
		beanDiscoveryFilters = ServiceLoader.load(BeanDiscoveryFilter.class)
		                                    .stream()
		                                    .map(ServiceLoader.Provider::get)
		                                    .collect(
				                                    toUnmodifiableMap(BeanDiscoveryFilter::discoveryMode, filter -> filter));
	}

	@Discovery
	public void discoverBeans(ScannedClasses scannedClasses) {
		getModules().filter(BeanDiscoveryExtension::isBeanArchive)
		            .flatMap(this::findCandidateClasses)
		            .forEach(scannedClasses::add);
	}


	private static Stream<ModuleReference> getModules() {
		String modulePath = System.getProperty("jdk.module.path");
		if (modulePath != null) {
			return getModulesFromPath(modulePath);
		}

		if (System.getProperty("java.class.path") != null) {
			return getModulesFromPath(System.getProperty("java.class.path"));
		}

		return ModuleLayer.boot().configuration().modules().stream().map(ResolvedModule::reference);
	}

	private Stream<String> findCandidateClasses(ModuleReference beanArchive) {
		BeanDiscoveryFilter beanDiscoveryFilter = getFilter(beanArchive);

		return getAccessibleClasses(beanArchive).filter(beanDiscoveryFilter::isCandidateClass);
	}

	private BeanDiscoveryFilter getFilter(ModuleReference beanArchive) {
		BeanDiscoveryMode beanDiscoveryMode = getBeanDiscoveryMode(beanArchive);
		if (!beanDiscoveryFilters.containsKey(beanDiscoveryMode)) {
			throw new DeploymentException(String.format(
					"Bean archive %s has a bean discovery mode of \"%s\", but this is not supported in the current environment.",
					beanArchive.descriptor().name(), beanDiscoveryMode));
		}

		return beanDiscoveryFilters.get(beanDiscoveryMode);
	}

	private static Stream<ModuleReference> getModulesFromPath(String path) {
		String rawModulePath = requireNonNull(path);
		String pathSeparator = requireNonNull(File.pathSeparator);
		List<Path> modulePathEntries =
				Arrays.stream(rawModulePath.split(Pattern.quote(pathSeparator))).map(Paths::get).toList();
		var moduleFinder = ModuleFinder.of(modulePathEntries.toArray(Path[]::new));

		return moduleFinder.findAll().stream();
	}

	private BeanDiscoveryMode getBeanDiscoveryMode(ModuleReference moduleReference) {
		try (ModuleReader reader = moduleReference.open();
		     InputStream in = reader.open("META-INF/beans.xml").orElseThrow()) {

			return BeansConfigReader.newInstance()
			                        .readConfig(in)
			                        .map(this::extractBeanDiscoveryMode)
			                        .orElse(DEFAULT_DISCOVERY_MODE);
		} catch (IOException e) {
			throw new UncheckedIOException(moduleReference.toString(), e);
		} catch (FileSystemNotFoundException e) {
			throw new UncheckedIOException(moduleReference.toString(), new IOException(e));
		}
	}

	private BeanDiscoveryMode extractBeanDiscoveryMode(BeanConfig beanConfig) {
		return switch (beanConfig.beanDiscoveryMode()) {
			case "all" -> ALL;
			case "annotated" -> ANNOTATED;
			case "none" -> NONE;
			case String s -> throw new IllegalArgumentException("Unknown bean discovery mode: " + s);
		};
	}

	private Stream<String> getAccessibleClasses(ModuleReference moduleReference) {
		if (isFullyOpen(moduleReference)) {
			return listClasses(moduleReference);
		}
		Set<String> accessiblePackages = findAccessiblePackages(moduleReference);
		return listClasses(moduleReference).filter(
				className -> getPackageName(className).map(accessiblePackages::contains).orElse(false));
	}


	private static Optional<String> getPackageName(String className) {
		int lastDot = className.lastIndexOf(".");
		if (lastDot < 0) {
			// Ignore class as it doesn't appear to be in a named package.
			return Optional.empty();
		}

		return Optional.of(className.substring(0, lastDot));
	}

	private Set<String> findAccessiblePackages(ModuleReference moduleReference) {
		List<String> exportedPackages = moduleReference.descriptor()
		                                               .exports()
		                                               .stream()
		                                               .filter(export -> !export.isQualified() || export.targets()
		                                                                                                .contains(
				                                                                                                "eu.jbeernink.hypospray.core"))
		                                               .map(ModuleDescriptor.Exports::source)
		                                               .toList();
		List<String> openedPackages = moduleReference.descriptor()
		                                             .opens()
		                                             .stream()
		                                             .filter(opens -> !opens.isQualified() || opens.targets()
		                                                                                           .contains(
				                                                                                           "eu.jbeernink.hypospray.core"))
		                                             .map(ModuleDescriptor.Opens::source)
		                                             .toList();
		return Stream.concat(exportedPackages.stream(), openedPackages.stream()).collect(toUnmodifiableSet());
	}

	private boolean isFullyOpen(ModuleReference beanArchive) {
		return beanArchive.descriptor().isOpen() || beanArchive.descriptor().isAutomatic();
	}

	private Stream<String> listClasses(ModuleReference moduleReference) {
		try (ModuleReader reader = moduleReference.open()) {
			return reader.list()
			             .filter(fileName -> fileName.endsWith(".class"))
			             .filter(
					             fileName -> !fileName.endsWith("module-info.class") && !fileName.endsWith("package-info.class"))
			             .map(fileName -> fileName.replaceAll(Pattern.quote("/"), ".").replace(".class", ""));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static boolean isBeanArchive(ModuleReference module) {
		try (ModuleReader reader = module.open()) {
			return reader.find("META-INF/beans.xml").isPresent();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
