package eu.jbeernink.hypospray.core.inject.build.compatible.spi;

import java.util.List;

import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.ParameterConfig;

public sealed interface ExecutableConfiguration extends MethodConfig, AnnotatedConfiguration permits
		MethodConfiguration, ConstructorConfiguration {

	List<ParameterConfiguration> parameterConfigurations();

	@Override
	default List<ParameterConfig> parameters() {
		return List.copyOf(parameterConfigurations());
	}
}
