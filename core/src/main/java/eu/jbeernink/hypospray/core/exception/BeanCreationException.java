package eu.jbeernink.hypospray.core.exception;

/// Exception thrown when an error occurs during the creation of a bean.
public class BeanCreationException extends RuntimeException {

	/// Constructs a new bean creation exception with the given error message.
	///
	/// @param message the error message.
	public BeanCreationException(String message) {
		super(message);
	}

	/// Constructs a new bean creation exception with the given error message and cause.
	///
	/// @param message the error message.
	/// @param cause the throwable that is the cause of the error.
	public BeanCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
