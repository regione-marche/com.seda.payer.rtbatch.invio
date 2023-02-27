package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "DocumentoPrincipale")
@XmlType(propOrder = { "IDDocumento", "tipoDocumento", "strutturaOriginale" })
public class DocumentoPrincipale extends SipElementBuilder<DocumentoPrincipale> implements SipSchemaElement {

	private String IDDocumento;
	private String TipoDocumento;
	private StrutturaOriginale strutturaOriginale;

	@XmlElement(name = "IDDocumento")
	public String getIDDocumento() {
		return IDDocumento;
	}

	public void setIDDocumento(String iDDocumento) {
		IDDocumento = iDDocumento;
	}

	@XmlElement(name = "TipoDocumento")
	public String getTipoDocumento() {
		return TipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		TipoDocumento = tipoDocumento;
	}

	@XmlElement(name = "StrutturaOriginale")
	public StrutturaOriginale getStrutturaOriginale() {
		return strutturaOriginale;
	}

	public void setStrutturaOriginale(StrutturaOriginale strutturaOriginale) {
		this.strutturaOriginale = strutturaOriginale;
	}

	@Override
	public DocumentoPrincipale buildSipElement(Properties data) {
		this.IDDocumento = data.getProperty("documentoPrincipale.iDDocumento");
		this.TipoDocumento = data.getProperty("documentoPrincipale.TipoDocumento");
		this.strutturaOriginale = new StrutturaOriginale().buildSipElement(data);
		return this;
	}
}
