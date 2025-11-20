package eu.jbeernink.hypospray.testing.extension;

import static java.lang.reflect.AccessFlag.MANDATED;
import static java.lang.reflect.AccessFlag.STATIC_PHASE;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class WithProfileExtension implements TestTemplateInvocationContextProvider {
	@Override
	public boolean supportsTestTemplate(ExtensionContext extensionContext) {
		return extensionContext.getTestMethod()
		                       .filter(method -> method.getAnnotation(WithProfile.class) != null)
		                       .isPresent();
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ExtensionContext extensionContext) {
		return null;
	}

	private static Object setupContainerWithinModule(TestInstanceFactoryContext testInstanceFactoryContext) {
		try {
			Set<Module> modules1 = ModuleLayer.boot().modules();
			String rawModulePath = System.getProperty("jdk.module.path");
			String pathSeparator = System.getProperty("path.separator");
			List<Path> modulePathEntries =
					Arrays.stream(rawModulePath.split(Pattern.quote(pathSeparator))).map(Paths::get).toList();
			ModuleFinder moduleFinder = ModuleFinder.of(modulePathEntries.toArray(new Path[0]));
			Optional<ModuleReference> moduleReference = moduleFinder.find("eu.jbeernink.hypospray.lite");

			ModuleDescriptor descriptor = moduleReference.orElseThrow().descriptor();

			var modules = new HashMap<String, ModuleReference>();
			findModules(modules, moduleFinder, "eu.jbeernink.hypospray.lite");

			Path[] modulePaths =
					modules.values().stream().flatMap(module -> module.location().stream()).map(Paths::get).toArray(Path[]::new);
			ModuleFinder testModuleFinder = ModuleFinder.of(modulePaths);
			Configuration testConfiguration = Configuration.resolveAndBind(ModuleFinder.of(), List.of(), testModuleFinder,
					List.of("eu.jbeernink.hypospray.lite"));

			ModuleLayer.Controller testModuleController =
					ModuleLayer.defineModulesWithManyLoaders(testConfiguration, List.of(ModuleLayer.empty()), null);

			Module module = testModuleController.layer().findModule("eu.jbeernink.hypospray.integration.tests").orElseThrow();
			ClassLoader classLoader = module.getClassLoader();
			classLoader.loadClass(testInstanceFactoryContext.getTestClass().getName());
		} catch (ClassNotFoundException e) {
			throw new TestInstantiationException(e.getMessage());
		}

		return null;
	}

	public static void findModules(Map<String, ModuleReference> foundModules, ModuleFinder moduleFinder,
	                               String moduleName) {
		if (foundModules.containsKey(moduleName)) {
			return;
		}
		if (ModuleFinder.ofSystem().find(moduleName).isPresent()) {
			return;
		}

		ModuleReference module = moduleFinder.find(moduleName).orElseThrow();
		foundModules.put(moduleName, module);
		for (ModuleDescriptor.Requires requires : module.descriptor().requires()) {
			String requiredModuleName = requires.name();
			if (!requires.accessFlags().contains(STATIC_PHASE) && !requires.accessFlags().contains(MANDATED)) {
				findModules(foundModules, moduleFinder, requiredModuleName);
			}
		}
	}
}
