package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Properties;

@XmlRootElement(name = "Componenti")
public class Componenti extends SipElementBuilder<Componenti> implements SipSchemaElement {

	private Componente componente;

	@XmlElement(name = "Componente")
	public Componente getComponente() {
		return componente;
	}

	public void setComponente(Componente componente) {
		this.componente = componente;
	}

	@Override
	public Componenti buildSipElement(Properties data) {
		this.componente = new Componente().buildSipElement(data);
		return this;
	}
}
