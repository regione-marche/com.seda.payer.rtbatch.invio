package com.seda.payer.rtbatch.invio;

public class EnteProcessingException extends BatchExecutionException {

	private static final long serialVersionUID = 1L;

	public EnteProcessingException(String message) {
		super(message);
	}

	public EnteProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
