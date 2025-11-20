package eu.jbeernink.hypospray.core.exception;

/// Exception thrown when an error occurs during bean disposal.
public class DisposalException extends RuntimeException {
	public DisposalException(String s, Exception e) {
		super(s, e);
	}
}
