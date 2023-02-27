package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement
@XmlType(propOrder = { "versione", "versatore", "chiave", "tipologiaUnitaDocumentaria" })
public class Intestazione extends SipElementBuilder<Intestazione> implements SipSchemaElement {

	private String versione;
	private Versatore versatore;
	private Chiave chiave;
	private String tipologiaUnitaDocumentaria;

	@XmlElement(name = "Versione")
	public String getVersione() {
		return versione;
	}

	public void setVersione(String versione) {
		this.versione = versione;
	}

	@XmlElement(name = "Versatore")
	public Versatore getVersatore() {
		return versatore;
	}

	public void setVersatore(Versatore versatore) {
		this.versatore = versatore;
	}

	@XmlElement(name = "Chiave")
	public Chiave getChiave() {
		return chiave;
	}

	public void setChiave(Chiave chiave) {
		this.chiave = chiave;
	}

	@XmlElement(name = "TipologiaUnitaDocumentaria")
	public String getTipologiaUnitaDocumentaria() {
		return tipologiaUnitaDocumentaria;
	}

	public void setTipologiaUnitaDocumentaria(String tipologiaUnitaDocumentaria) {
		this.tipologiaUnitaDocumentaria = tipologiaUnitaDocumentaria;
	}

	@Override
	public Intestazione buildSipElement(Properties data) {
		this.versione = data.getProperty("intestazione.versione");
		this.tipologiaUnitaDocumentaria = data.getProperty("intestazione.tipologiaUnitaDocumentaria");
		this.versatore = new Versatore().buildSipElement(data);
		this.chiave = new Chiave().buildSipElement(data);
		return this;
	}
}
