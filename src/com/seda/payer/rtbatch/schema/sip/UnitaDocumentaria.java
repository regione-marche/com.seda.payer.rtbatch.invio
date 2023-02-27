package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "UnitaDocumentaria")
@XmlType(propOrder = { "intestazione", /* "configurazione",*/ "profiloUnitaDocumentaria", "datiSpecifici", "documentoPrincipale" })
public class UnitaDocumentaria extends SipElementBuilder<UnitaDocumentaria> implements SipSchemaElement {

	private Intestazione intestazione;
//	private Configurazione configurazione;
	private ProfiloUnitaDocumentaria profiloUnitaDocumentaria;
	private DatiSpecifici datiSpecifici;
	private DocumentoPrincipale documentoPrincipale;

	@XmlElement(name = "Intestazione")
	public Intestazione getIntestazione() {
		return intestazione;
	}

	public void setIntestazione(Intestazione intestazione) {
		this.intestazione = intestazione;
	}

//	@XmlElement(name = "Configurazione")
//	public Configurazione getConfigurazione() {
//		return configurazione;
//	}
//
//	public void setConfigurazione(Configurazione configurazione) {
//		this.configurazione = configurazione;
//	}

	@XmlElement(name = "ProfiloUnitaDocumentaria")
	public ProfiloUnitaDocumentaria getProfiloUnitaDocumentaria() {
		return profiloUnitaDocumentaria;
	}

	public void setProfiloUnitaDocumentaria(ProfiloUnitaDocumentaria profiloUnitaDocumentaria) {
		this.profiloUnitaDocumentaria = profiloUnitaDocumentaria;
	}

	@XmlElement(name = "DatiSpecifici")
	public DatiSpecifici getDatiSpecifici() {
		return datiSpecifici;
	}

	public void setDatiSpecifici(DatiSpecifici datiSpecifici) {
		this.datiSpecifici = datiSpecifici;
	}

	@XmlElement(name = "DocumentoPrincipale")
	public DocumentoPrincipale getDocumentoPrincipale() {
		return documentoPrincipale;
	}

	public void setDocumentoPrincipale(DocumentoPrincipale documentoPrincipale) {
		this.documentoPrincipale = documentoPrincipale;
	}

	@Override
	public UnitaDocumentaria buildSipElement(Properties data) {
		this.intestazione = new Intestazione().buildSipElement(data);
//		this.configurazione = new Configurazione().buildSipElement(data);
		this.profiloUnitaDocumentaria = new ProfiloUnitaDocumentaria().buildSipElement(data);
		this.datiSpecifici = new DatiSpecifici().buildSipElement(data);
		this.documentoPrincipale = new DocumentoPrincipale().buildSipElement(data);
		return this;
	}
}
