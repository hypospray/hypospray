package eu.jbeernink.hypospray.core.exception;

public class DeploymentException extends RuntimeException {
	public DeploymentException(String message) {
		super(message);
	}

	public DeploymentException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeploymentException(Throwable cause) {
		super(cause);
	}
}
