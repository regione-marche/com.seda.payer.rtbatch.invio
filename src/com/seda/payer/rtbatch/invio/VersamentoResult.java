package com.seda.payer.rtbatch.invio;


/**
 * DTO con i risultati dell'operazione di versamento telematico di una RT
 */
public class VersamentoResult {
	
	public static final String RESULT_OK = "OK";
	public static final String RESULT_KO = "KO";
	
	private String idSip;
	String resultContent;
	String errorMessage;
	String errorCodes;
	String transportOutcome;
	// String statoProtocollazione;
	
	
	
	VersamentoResult() {
		errorMessage = "";
		errorCodes = "";
	}
	
	
	
	public String getResultContent() {
		return resultContent;
	}
	
	
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	
	
	public String getErrorCodes() {
		return errorCodes;
	}
	
	
	
	public String getTransportOutcome() {
		return transportOutcome;
	}
	
	
	
	public String getIdSip() {
		return idSip;
	}
	
	
	
	public void setIdSip(String idSip) {
		this.idSip = idSip;
	}
	
	
	
//	public String getStatoProtocollazione() {
//		return statoProtocollazione;
//	}
	
	
	
	public boolean isGloballyCorrect() {
		return errorMessage.isEmpty();
	}
	

	
	@Override
	public String toString() {
		return "VersamentoResult{" +
				"idSip='" + idSip + '\'' +
				", resultContent='" + resultContent + '\'' +
				", errorMessage='" + errorMessage + '\'' +
				", errorCodes='" + errorCodes + '\'' +
				", transportOutcome='" + transportOutcome + '\'' +
//				", statoProtocollazione='" + statoProtocollazione + '\'' +
				'}';
	}
}
