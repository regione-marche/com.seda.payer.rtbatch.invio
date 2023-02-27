package com.seda.payer.rtbatch.invio.response;

public class EsitoGenerale {

	String codiceEsito;
	String codiceErrore;
	String messaggioErrore;

	EsitoGenerale() {
	}

	public String codiceEsito() {
		return codiceEsito;
	}

	public String codiceErrore() {
		return codiceErrore;
	}

	public String messaggioErrore() {
		return messaggioErrore;
	}
}
