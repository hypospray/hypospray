package eu.jbeernink.hypospray.core.extension;

import static eu.jbeernink.hypospray.core.priority.ExtensionPriority.DEFAULT_EXTENSION_PRIORITY;
import static eu.jbeernink.hypospray.core.todo.Todo.warnNotYetImplemented;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNullElseGet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.InterceptorInfo;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.types.Type;

import eu.jbeernink.hypospray.core.inject.build.compatible.spi.BeanInformation;
import eu.jbeernink.hypospray.core.inject.build.compatible.spi.ClassConfiguration;
import eu.jbeernink.hypospray.core.inject.spi.builder.SyntheticComponentManager;
import eu.jbeernink.hypospray.core.messages.ContainerInitializationLogger;
import eu.jbeernink.hypospray.core.settings.ContainerSettings;
import eu.jbeernink.hypospray.model.TypeFactory;

public class BuildCompatibleExtensionManager {

	private record ExtensionMethod(BuildCompatibleExtension extension, Method method) {

		public int priority() {
			return getMethodPriority().orElseGet(() -> getExtensionPriority().orElse(DEFAULT_EXTENSION_PRIORITY));
		}

		private OptionalInt getExtensionPriority() {
			return getPriority(extension.getClass().getAnnotation(Priority.class));
		}

		private OptionalInt getMethodPriority() {
			return getPriority(method.getAnnotation(Priority.class));
		}

		private OptionalInt getPriority(Priority priority) {
			if (priority == null) {
				return OptionalInt.empty();
			}

			return OptionalInt.of(priority.value());
		}
	}

	private final List<BuildCompatibleExtension> buildCompatibleExtensions;

	private final Map<Class<? extends BuildCompatibleExtension>, ContainerInitializationLogger> extensionLoggers =
			new HashMap<>();

	private final TypeFactory typeFactory = TypeFactory.getInstance();

	public BuildCompatibleExtensionManager(List<BuildCompatibleExtension> buildCompatibleExtensions) {
		this.buildCompatibleExtensions = List.copyOf(buildCompatibleExtensions);
	}

	public void discoverClasses(ScannedClasses scannedClasses, MetaAnnotations metaAnnotations,
	                            ContainerSettings containerSettings) {
		findMethodsWith(Discovery.class).forEach(extensionMethod -> invokeLifecycleMethod(extensionMethod,
				List.of(scannedClasses, metaAnnotations, containerSettings, getLogger(extensionMethod.extension))));
	}

	public List<ClassConfiguration<?>> performEnhancement(List<ClassConfiguration<?>> classConfigurations) {
		List<ExtensionMethod> extensionMethods = findMethodsWith(Enhancement.class).toList();

		for (ExtensionMethod extensionMethod : extensionMethods) {
			for (ClassConfiguration<?> classConfiguration : classConfigurations) {
				Enhancement enhancement = extensionMethod.method.getAnnotation(Enhancement.class);
				if (matchesType(classConfiguration, enhancement)) {
					ContainerInitializationLogger logger = getLogger(extensionMethod.extension);
					if (hasParameterOfType(extensionMethod.method, ClassConfig.class)) {
						invokeLifecycleMethod(extensionMethod, List.of(classConfiguration, typeFactory, logger));
					} else if (hasParameterOfType(extensionMethod.method, ClassInfo.class)) {
						invokeLifecycleMethod(extensionMethod, List.of(classConfiguration.info(), typeFactory, logger));
					} else if (hasParameterOfType(extensionMethod.method, MethodConfig.class)) {
						Stream.concat(classConfiguration.constructors().stream(), classConfiguration.methods().stream())
						      .forEach(method -> invokeLifecycleMethod(extensionMethod, List.of(method, typeFactory, logger)));
					} else if (hasParameterOfType(extensionMethod.method, MethodInfo.class)) {
						Stream.concat(classConfiguration.constructors().stream(), classConfiguration.methods().stream())
						      .map(MethodConfig::info)
						      .forEach(method -> invokeLifecycleMethod(extensionMethod, List.of(method, typeFactory, logger)));
					} else if (hasParameterOfType(extensionMethod.method, FieldConfig.class)) {
						classConfiguration.fields()
						                  .forEach(
								                  field -> invokeLifecycleMethod(extensionMethod, List.of(field, typeFactory, logger)));
					} else if (hasParameterOfType(extensionMethod.method, FieldInfo.class)) {
						classConfiguration.fields()
						                  .stream()
						                  .map(FieldConfig::info)
						                  .forEach(
								                  field -> invokeLifecycleMethod(extensionMethod, List.of(field, typeFactory, logger)));
					}
				}
			}
		}

		return classConfigurations;
	}

	private boolean matchesType(ClassConfiguration<?> classConfiguration, Enhancement enhancement) {
		warnNotYetImplemented();

		return Arrays.stream(enhancement.types())
		             .anyMatch(type -> type.isAssignableFrom(classConfiguration.info().classInstance()));
	}

	public void notifyRegistration(BeanInformation<?> beanInfo) {
		List<ExtensionMethod> extensionMethods = findMethodsWith(Registration.class).filter(
				extensionMethod -> hasParameterOfType(extensionMethod.method, BeanInfo.class)).toList();


		for (ExtensionMethod extensionMethod : extensionMethods) {
			Method lifecycleMethod = extensionMethod.method;
			BuildCompatibleExtension extension = extensionMethod.extension;

			Registration registration = lifecycleMethod.getAnnotation(Registration.class);
			if (requireNonNullElseGet(registration.types(), () -> new Class[0]).length == 0 ||
			    hasMatchingType(beanInfo, registration.types())) {
				invokeLifecycleMethod(extensionMethod, List.of(beanInfo, typeFactory, getLogger(extension)));
			}
		}

	}

	public void notifyRegistration(InterceptorInfo interceptorInfo) {
		// TODO implement.
	}

	public void notifyRegistration(ObserverInfo interceptorInfo) {
		// TODO implement.
	}

	public void performSynthesis(SyntheticComponentManager syntheticComponents) {
		findMethodsWith(Synthesis.class).forEach(extensionMethod -> invokeLifecycleMethod(extensionMethod,
				List.of(syntheticComponents, typeFactory, getLogger(extensionMethod.extension))));
	}

	public void performValidation() {
		findMethodsWith(Validation.class).forEach(extensionMethod -> invokeLifecycleMethod(extensionMethod,
				List.of(typeFactory, getLogger(extensionMethod.extension))));
	}

	private void invokeLifecycleMethod(ExtensionMethod extensionMethod, List<Object> availableParameters) {
		Method lifecycleMethod = extensionMethod.method;
		BuildCompatibleExtension extension = extensionMethod.extension;

		Class<?>[] parameterTypes = lifecycleMethod.getParameterTypes();
		Object[] parameters = prepareParameters(parameterTypes, availableParameters);

		if (!lifecycleMethod.canAccess(extension)) {
			lifecycleMethod.setAccessible(true);
		}

		try {
			lifecycleMethod.invoke(extension, parameters);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO change to more appropriate exception
			throw new RuntimeException(e);
		}
	}

	private boolean hasParameterOfType(Method method, Class<?> expectedParameterType) {
		Class<?>[] parameterTypes = method.getParameterTypes();

		for (Class<?> parameterType : parameterTypes) {
			if (parameterType.equals(expectedParameterType)) {
				return true;
			}
		}

		return false;
	}

	private boolean hasMatchingType(BeanInformation<?> beanInfo, Class<?>[] types) {
		return beanInfo.typeInstances()
		               .stream()
		               .filter(Type::isClass)
		               .map(type -> type.asClass().asJavaType())
		               .anyMatch(c -> Arrays.asList(types).contains(c));
	}

	private ContainerInitializationLogger getLogger(BuildCompatibleExtension extension) {
		return extensionLoggers.computeIfAbsent(extension.getClass(), ContainerInitializationLogger::forClass);
	}

	private Stream<ExtensionMethod> findMethodsWith(Class<? extends Annotation> annotationClass) {
		return buildCompatibleExtensions.stream()
		                                .flatMap(extension -> getExtensionMethods(extension, annotationClass))
		                                .sorted(comparingInt(ExtensionMethod::priority));
	}

	private static Stream<ExtensionMethod> getExtensionMethods(BuildCompatibleExtension extension,
	                                                           Class<? extends Annotation> annotationClass) {
		return Arrays.stream(extension.getClass().getDeclaredMethods())
		             .filter(method -> method.isAnnotationPresent(annotationClass))
		             .map(method -> new ExtensionMethod(extension, method));
	}

	private static Object[] prepareParameters(Class<?>[] parameterTypes, List<Object> availableParameters) {
		Object[] parameters = new Object[parameterTypes.length];
		for (int i = 0; i < parameters.length; i++) {
			Class<?> parameterType = parameterTypes[i];

			for (Object parameter : availableParameters) {
				if (parameterType.isAssignableFrom(parameter.getClass())) {
					parameters[i] = parameter;
				}
			}

			// TODO validate all parameters have been assigned.
		}

		return parameters;
	}

}
