package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;

@XmlRootElement
public class Versatore extends SipElementBuilder<Versatore> implements SipSchemaElement {

	private String ambiente;
	private String ente;
	private String struttura;
	private String userID;

	@XmlElement(name = "Ambiente")
	public String getAmbiente() {
		return ambiente;
	}

	public void setAmbiente(String ambiente) {
		this.ambiente = ambiente;
	}

	@XmlElement(name = "Ente")
	public String getEnte() {
		return ente;
	}

	public void setEnte(String ente) {
		this.ente = ente;
	}

	@XmlElement(name = "Struttura")
	public String getStruttura() {
		return struttura;
	}

	public void setStruttura(String struttura) {
		this.struttura = struttura;
	}

	@XmlElement(name = "UserID")
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	@Override
	public Versatore buildSipElement(Properties data) {
		this.ambiente = data.getProperty("intestazione.versatore.ambiente");
		this.ente = data.getProperty("intestazione.versatore.ente");
		this.struttura = data.getProperty("intestazione.versatore.struttura");
		this.userID = data.getProperty("intestazione.versatore.userID");
		return this;
	}

}
