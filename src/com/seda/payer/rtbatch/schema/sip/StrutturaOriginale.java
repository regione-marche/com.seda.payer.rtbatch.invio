package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "StrutturaOriginale")
@XmlType(propOrder = { "tipoStruttura", "componenti" })
public class StrutturaOriginale extends SipElementBuilder<StrutturaOriginale> implements SipSchemaElement {

	private String TipoStruttura;

	private Componenti Componenti;

	@XmlElement(name = "TipoStruttura")
	public String getTipoStruttura() {
		return TipoStruttura;
	}

	public void setTipoStruttura(String tipoStruttura) {
		this.TipoStruttura = tipoStruttura;
	}

	@XmlElement(name = "Componenti")
	public Componenti getComponenti() {
		return Componenti;
	}

	public void setComponenti(Componenti componenti) {
		this.Componenti = componenti;
	}

	@Override
	public StrutturaOriginale buildSipElement(Properties data) {
		this.TipoStruttura = data.getProperty("documentoPrincipale.StrutturaOriginale.TipoStruttura");
		this.Componenti = new Componenti().buildSipElement(data);
		return this;
	}
}
