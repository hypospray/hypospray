package eu.jbeernink.hypospray.core.messages;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.lang.model.AnnotationTarget;

import eu.jbeernink.hypospray.core.exception.DeploymentException;

/// Logging system independent logger to be used by [jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension] instances.
public final class ContainerInitializationLogger implements Messages {

	private final Logger logger;

	private ContainerInitializationLogger(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void info(String message) {
		logger.log(INFO, message);
	}

	@Override
	public void info(String message, AnnotationTarget relatedTo) {
		log(INFO, message, relatedTo);
	}

	@Override
	public void info(String message, BeanInfo relatedTo) {
		log(INFO, message, relatedTo);
	}

	@Override
	public void info(String message, ObserverInfo relatedTo) {
		log(INFO, message, relatedTo);
	}

	@Override
	public void warn(String message) {
		logger.log(WARNING, message);
	}

	@Override
	public void warn(String message, AnnotationTarget relatedTo) {
		log(WARNING, message, relatedTo);
	}

	@Override
	public void warn(String message, BeanInfo relatedTo) {
		log(WARNING, message, relatedTo);
	}

	@Override
	public void warn(String message, ObserverInfo relatedTo) {
		log(WARNING, message, relatedTo);
	}

	@Override
	public void error(String message) {
		logger.log(ERROR, message);
		throw new DeploymentException(message);
	}

	@Override
	public void error(String message, AnnotationTarget relatedTo) {
		log(ERROR, message, relatedTo);
		throw new DeploymentException(formatAnnotationTargetMessage(message, relatedTo));
	}

	@Override
	public void error(String message, BeanInfo relatedTo) {
		log(ERROR, message, relatedTo);
		throw new DeploymentException(formatBeanInfoMessage(message, relatedTo));
	}

	@Override
	public void error(String message, ObserverInfo relatedTo) {
		log(ERROR, message, relatedTo);
		throw new DeploymentException(formatObserverMessage(message, relatedTo));
	}

	@Override
	public void error(Exception exception) {
		logger.log(ERROR, exception.getMessage(), exception);
		throw new DeploymentException(exception);
	}

	private void log(Level level, String message, BeanInfo relatedTo) {
		logger.log(level, formatBeanInfoMessage(message, relatedTo));
	}

	private static String formatBeanInfoMessage(String message, BeanInfo relatedTo) {
		return String.format("Bean %s: %s", relatedTo, message);
	}

	private void log(Level level, String message, AnnotationTarget relatedTo) {
		logger.log(level, formatAnnotationTargetMessage(message, relatedTo));
	}

	private static String formatAnnotationTargetMessage(String message, AnnotationTarget relatedTo) {
		return String.format("Annotation target %s: %s", relatedTo, message);
	}

	private void log(Level level, String message, ObserverInfo relatedTo) {
		logger.log(level, formatObserverMessage(message, relatedTo));
	}

	private static String formatObserverMessage(String message, ObserverInfo relatedTo) {
		return String.format("Observer %s: %s", relatedTo, message);
	}

	public static ContainerInitializationLogger forClass(Class<?> clazz) {
		return new ContainerInitializationLogger(System.getLogger(clazz.getName()));
	}
}
