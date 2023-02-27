package com.seda.payer.rtbatch.schema.sip;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Properties;

@XmlRootElement(name = "DatiSpecifici")
@XmlType(propOrder = { "identificativoUnivocoVersamento", "identificativoUnivocoRiscossione", "dataEsitoSingoloPagamento",
		"importoTotalePagato", "soggettoPagatore", "soggettoVersante", "istitutoAttestante" })
public class DatiSpecifici extends SipElementBuilder<DatiSpecifici> implements SipSchemaElement {

	private String identificativoUnivocoVersamento;
	private String identificativoUnivocoRiscossione;
	private String dataEsitoSingoloPagamento;
	private String importoTotalePagato;
	private String soggettoPagatore;
	private String soggettoVersante;
	private String istitutoAttestante;

	
	
	public String getIdentificativoUnivocoVersamento() {
		return identificativoUnivocoVersamento;
	}



	public void setIdentificativoUnivocoVersamento(
			String identificativoUnivocoVersamento) {
		this.identificativoUnivocoVersamento = identificativoUnivocoVersamento;
	}



	public String getIdentificativoUnivocoRiscossione() {
		return identificativoUnivocoRiscossione;
	}



	public void setIdentificativoUnivocoRiscossione(
			String identificativoUnivocoRiscossione) {
		this.identificativoUnivocoRiscossione = identificativoUnivocoRiscossione;
	}



	public String getDataEsitoSingoloPagamento() {
		return dataEsitoSingoloPagamento;
	}



	public void setDataEsitoSingoloPagamento(String dataEsitoSingoloPagamento) {
		this.dataEsitoSingoloPagamento = dataEsitoSingoloPagamento;
	}



	public String getImportoTotalePagato() {
		return importoTotalePagato;
	}



	public void setImportoTotalePagato(String importoTotalePagato) {
		this.importoTotalePagato = importoTotalePagato;
	}



	public String getSoggettoPagatore() {
		return soggettoPagatore;
	}



	public void setSoggettoPagatore(String soggettoPagatore) {
		this.soggettoPagatore = soggettoPagatore;
	}



	public String getSoggettoVersante() {
		return soggettoVersante;
	}



	public void setSoggettoVersante(String soggettoVersante) {
		this.soggettoVersante = soggettoVersante;
	}



	public String getIstitutoAttestante() {
		return istitutoAttestante;
	}



	public void setIstitutoAttestante(String istitutoAttestante) {
		this.istitutoAttestante = istitutoAttestante;
	}



	@Override
	public DatiSpecifici buildSipElement(Properties data) {
		this.identificativoUnivocoVersamento = data.getProperty("datiSpecifici.identificativoUnivocoVersamento");
		this.identificativoUnivocoRiscossione = data.getProperty("datiSpecifici.identificativoUnivocoRiscossione");
		this.dataEsitoSingoloPagamento = data.getProperty("datiSpecifici.dataEsitoSingoloPagamento");
		this.importoTotalePagato = data.getProperty("datiSpecifici.importoTotalePagato");
		this.soggettoPagatore = data.getProperty("datiSpecifici.soggettoPagatore");
		this.soggettoVersante = data.getProperty("datiSpecifici.soggettoVersante");
		this.istitutoAttestante = data.getProperty("datiSpecifici.istitutoAttestante");
		return this;
	}

}
