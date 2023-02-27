package com.seda.payer.rtbatch.invio;

public class BatchExecutionException extends Exception {

	private static final long serialVersionUID = 1L;

	public BatchExecutionException(String message) {
		super(message);
	}

	public BatchExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
