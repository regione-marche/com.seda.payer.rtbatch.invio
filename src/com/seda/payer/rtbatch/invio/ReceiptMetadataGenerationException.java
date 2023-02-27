package com.seda.payer.rtbatch.invio;

public class ReceiptMetadataGenerationException extends EnteProcessingException {

	private static final long serialVersionUID = 1L;

	public ReceiptMetadataGenerationException(String message) {
		super(message);
	}

	public ReceiptMetadataGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

}
