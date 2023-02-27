package com.seda.payer.rtbatch.schema.sip;

import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Configurazione extends SipElementBuilder<Configurazione> implements SipSchemaElement {

	private String SimulaSalvataggioDatiInDB;
	private Boolean forzaConservazione;
	private Boolean forzaAccettazione;

	@XmlElement
	public String getSimulaSalvataggioDatiInDB() {
		return SimulaSalvataggioDatiInDB;
	}

	public void setSimulaSalvataggioDatiInDB(String simulaSalvataggioDatiInDB) {
		SimulaSalvataggioDatiInDB = simulaSalvataggioDatiInDB;
	}

	@XmlElement(name = "ForzaConservazione")
	public Boolean getForzaConservazione() {
		return forzaConservazione;
	}

	public void setForzaConservazione(Boolean forzaConservazione) {
		this.forzaConservazione = forzaConservazione;
	}

	@XmlElement(name ="ForzaAccettazione")
	public Boolean getForzaAccettazione() {
		return forzaAccettazione;
	}

	public void setForzaAccettazione(Boolean forzaAccettazione) {
		this.forzaAccettazione = forzaAccettazione;
	}

	@Override
	public Configurazione buildSipElement(Properties data) {
//		forzaConservazione = Boolean.valueOf(data.getProperty("configurazione.forzaConservazione"));
//		forzaAccettazione = Boolean.valueOf(data.getProperty("configurazione.forzaAccettazione"));
		return this;
	}

}
