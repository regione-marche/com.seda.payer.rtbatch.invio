package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "Componente")
@XmlType(propOrder = { "ID", "ordinePresentazione", "tipoComponente", "tipoSupportoComponente", "nomeComponente",
		"formatoFileVersato", "hashVersato", "IDComponenteVersato", "utilizzoDataFirmaPerRifTemp" })
public class Componente extends SipElementBuilder<Componente> implements SipSchemaElement {

	private String ID;
	private String OrdinePresentazione;
	private String TipoComponente;
	private String TipoSupportoComponente;
	private String NomeComponente;
	private String FormatoFileVersato;
	private String HashVersato;
	private String IDComponenteVersato;
	private String UtilizzoDataFirmaPerRifTemp;

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	@XmlElement(name = "OrdinePresentazione")
	public String getOrdinePresentazione() {
		return OrdinePresentazione;
	}

	public void setOrdinePresentazione(String ordinePresentazione) {
		OrdinePresentazione = ordinePresentazione;
	}

	@XmlElement(name = "TipoComponente")
	public String getTipoComponente() {
		return TipoComponente;
	}

	public void setTipoComponente(String tipoComponente) {
		TipoComponente = tipoComponente;
	}

	@XmlElement(name = "TipoSupportoComponente")
	public String getTipoSupportoComponente() {
		return TipoSupportoComponente;
	}

	public void setTipoSupportoComponente(String tipoSupportoComponente) {
		TipoSupportoComponente = tipoSupportoComponente;
	}
	
	@XmlElement(name = "NomeComponente")
	public String getNomeComponente() {
		return NomeComponente;
	}

	public void setNomeComponente(String nomeComponente) {
		NomeComponente = nomeComponente;
	}

	@XmlElement(name = "FormatoFileVersato")
	public String getFormatoFileVersato() {
		return FormatoFileVersato;
	}

	public void setFormatoFileVersato(String formatoFileVersato) {
		FormatoFileVersato = formatoFileVersato;
	}

	@XmlElement(name = "HashVersato")
	public String getHashVersato() {
		return HashVersato;
	}

	public void setHashVersato(String hashVersato) {
		HashVersato = hashVersato;
	}

	public String getIDComponenteVersato() {
		return IDComponenteVersato;
	}

	public void setIDComponenteVersato(String iDComponenteVersato) {
		IDComponenteVersato = iDComponenteVersato;
	}

	@XmlElement(name = "UtilizzoDataFirmaPerRifTemp")
	public String getUtilizzoDataFirmaPerRifTemp() {
		return UtilizzoDataFirmaPerRifTemp;
	}

	public void setUtilizzoDataFirmaPerRifTemp(String utilizzoDataFirmaPerRifTemp) {
		UtilizzoDataFirmaPerRifTemp = utilizzoDataFirmaPerRifTemp;
	}

	@Override
	public Componente buildSipElement(Properties data) {
		this.ID = data.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.id");
		this.OrdinePresentazione = data
				.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.OrdinePresentazione");
		this.TipoComponente = data.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.TipoComponente");
		this.TipoSupportoComponente = data
				.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.TipoSupportoComponente");
		this.NomeComponente = data.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.NomeComponente");
		this.FormatoFileVersato = data
				.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.FormatoFileVersato");
		this.HashVersato = data.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.HashVersato");
		this.IDComponenteVersato = data
				.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.IDComponenteVersato");
		this.UtilizzoDataFirmaPerRifTemp = data
				.getProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.UtilizzoDataFirmaPerRifTemp");
		return this;
	}
}
