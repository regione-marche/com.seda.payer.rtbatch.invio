package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "Chiave")
@XmlType(propOrder = { "numero", "anno", "tipoRegistro" })
public class Chiave extends SipElementBuilder<Chiave> implements SipSchemaElement {

	private String numero;
	private String anno;
	private String tipoRegistro;

	@XmlElement(name = "Numero")
	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	@XmlElement(name = "Anno")
	public String getAnno() {
		return anno;
	}

	public void setAnno(String anno) {
		this.anno = anno;
	}

	@XmlElement(name = "TipoRegistro")
	public String getTipoRegistro() {
		return tipoRegistro;
	}

	public void setTipoRegistro(String tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}

	@Override
	public Chiave buildSipElement(Properties data) {
		this.anno = data.getProperty("intestazione.chiave.anno");
		this.numero = data.getProperty("intestazione.chiave.numero");
		this.tipoRegistro = data.getProperty("intestazione.chiave.tipoRegistro");
		return this;
	}

}
