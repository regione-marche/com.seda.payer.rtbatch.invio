package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "ProfiloUnitaDocumentaria")
@XmlType(propOrder = { "oggetto", "data" })
public class ProfiloUnitaDocumentaria extends SipElementBuilder<ProfiloUnitaDocumentaria> implements SipSchemaElement {

	private String oggetto;
	private String data;

	@XmlElement(name = "Oggetto")
	public String getOggetto() {
		return oggetto;
	}

	public void setOggetto(String oggetto) {
		this.oggetto = oggetto;
	}

	@XmlElement(name = "Data")
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public ProfiloUnitaDocumentaria buildSipElement(Properties data) {
		this.oggetto = data.getProperty("profiloUnitaDocumentaria.oggetto");
		this.data = data.getProperty("profiloUnitaDocumentaria.data");
		return this;
	}
}
