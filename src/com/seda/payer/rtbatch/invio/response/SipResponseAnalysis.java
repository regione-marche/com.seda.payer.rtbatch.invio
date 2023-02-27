package com.seda.payer.rtbatch.invio.response;


/**
 * Contiene il risultato dell'analisi di un SIP inviato
 */
public class SipResponseAnalysis {
	
	private String erroreLogico;
	
	private String idSIP;
	
	EsitoGenerale esitoGenerale;
	EsitoChiamataWS esitoChiamataWS;
	EsitoXSD esitoXSD;
	EsitoUnitaDocumentaria esitoUnitaDocumentaria;
	EsitoDocumentoPrincipale esitoDocumentoPrincipale;
	
	
	
	public SipResponseAnalysis() {
		this.erroreLogico = "";
	}
	
	
	
	public String getErroreLogico() {
		return erroreLogico;
	}
	
	
	
	public void setErroreLogico(String erroreLogico) {
		if (!isPresenzaErroreLogico()) {
			this.erroreLogico = erroreLogico;
		} else {
			StringBuilder sb = new StringBuilder(this.erroreLogico);
			sb.append(" - ").append(erroreLogico);
			this.erroreLogico = sb.toString();
		}
	}
	
	
	
	public boolean isPresenzaErroreLogico() {
		return !erroreLogico.isEmpty();
	}
	
	
	
	public String getIdSIP() {
		return idSIP;
	}
	
	
	
	public void setIdSIP(String idSIP) {
		this.idSIP = idSIP;
	}
	
	
	
	public EsitoGenerale esitoGenerale() {
		return esitoGenerale;
	}
	
	
	
	public EsitoChiamataWS esitoChiamataWS() {
		return esitoChiamataWS;
	}
	
	
	
	public EsitoXSD esitoXSD() {
		return esitoXSD;
	}
	
	
	
	public EsitoUnitaDocumentaria esitoUnitaDocumentaria() {
		return esitoUnitaDocumentaria;
	}
	
	
	
	public EsitoDocumentoPrincipale esitoDocumentoPrincipale() {
		return esitoDocumentoPrincipale;
	}
	
	
	
	@Override
	public String toString() {
		return "SipResponseAnalysis{" +
				"erroreLogico='" + erroreLogico + '\'' +
				", idSIP='" + idSIP + '\'' +
				", esitoGenerale=" + esitoGenerale +
				", esitoChiamataWS=" + esitoChiamataWS +
				", esitoXSD=" + esitoXSD +
				", esitoUnitaDocumentaria=" + esitoUnitaDocumentaria +
				", esitoDocumentoPrincipale=" + esitoDocumentoPrincipale +
				'}';
	}
}
