package com.seda.payer.rtbatch.invio.response;

import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_INVIO;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.seda.payer.rtbatch.base.util.XmlUtils;

/**
 *
 */
public class ResponseAnalyzer {

	private static final Logger log = Logger.getLogger(LOGGER_CATEGORY_INVIO);

	private final XmlUtils xmlUtils;

	public ResponseAnalyzer() {
		xmlUtils = new XmlUtils();
	}

	private void checkEsitoGenerale(Document doc, SipResponseAnalysis responseAnalysis) {
		NodeList nl = xmlUtils.searchInXml(doc, "/EsitoVersamento/EsitoGenerale");
		if (nl != null) {
			Element element = (Element) nl.item(0);

			EsitoGenerale esitoGenerale = new EsitoGenerale();
			esitoGenerale.codiceEsito = element.getElementsByTagName("CodiceEsito").item(0).getTextContent();
			esitoGenerale.codiceErrore = element.getElementsByTagName("CodiceErrore").item(0).getTextContent();
			esitoGenerale.messaggioErrore = element.getElementsByTagName("MessaggioErrore").item(0).getTextContent();

			responseAnalysis.esitoGenerale = esitoGenerale;
		} else {
			responseAnalysis.setErroreLogico("Errore nel recupero dell'esito generale");
		}
	}

	private void checkEsitoChiamataWS(Document doc, SipResponseAnalysis responseAnalysis) {
		NodeList nl = xmlUtils.searchInXml(doc, "/EsitoVersamento/EsitoChiamataWS");

		if (nl != null) {
			Element element = (Element) nl.item(0);

			EsitoChiamataWS esitoChiamataWS = new EsitoChiamataWS();
			esitoChiamataWS.versioneWsCorretta = element.getElementsByTagName("VersioneWSCorretta").item(0)
					.getTextContent();
			esitoChiamataWS.credenzialiOperatore = element.getElementsByTagName("CredenzialiOperatore").item(0)
					.getTextContent();
			esitoChiamataWS.fileAttesiRicevuti = element.getElementsByTagName("FileAttesiRicevuti").item(0)
					.getTextContent();

			responseAnalysis.esitoChiamataWS = esitoChiamataWS;
		} else {
			responseAnalysis.setErroreLogico("Errore nel recupero dell'esito chiamata al web service");
		}
	}

	private void checkEsitoXSD(Document doc, SipResponseAnalysis responseAnalysis) {
		NodeList nl = xmlUtils.searchInXml(doc, "/EsitoVersamento/EsitoXSD");
		if (nl != null) {
			Element element = (Element) nl.item(0);

			EsitoXSD esitoXSD = new EsitoXSD();
			esitoXSD.codiceEsito = element.getElementsByTagName("CodiceEsito").item(0).getTextContent();
			esitoXSD.controlloStrutturaXML = element.getElementsByTagName("ControlloStrutturaXML").item(0)
					.getTextContent();

			responseAnalysis.esitoXSD = esitoXSD;
		} else {
			responseAnalysis
					.setErroreLogico("Errore nel recupero dell'esito del controllo di conformità allo schema xsd");
		}
	}

	private void checkEsitoUnitaDocumentaria(Document doc, SipResponseAnalysis responseAnalysis) {
		NodeList nl = xmlUtils.searchInXml(doc, "/EsitoVersamento/UnitaDocumentaria/EsitoUnitaDocumentaria");

		if (nl != null) {
			Element element = (Element) nl.item(0);

			EsitoUnitaDocumentaria esitoUnitaDocumentaria = new EsitoUnitaDocumentaria();
			esitoUnitaDocumentaria.codiceEsito = element.getElementsByTagName("CodiceEsito").item(0).getTextContent();
			esitoUnitaDocumentaria.identificazioneVersatore = element.getElementsByTagName("IdentificazioneVersatore")
					.item(0).getTextContent();
			esitoUnitaDocumentaria.univocitaChiave = element.getElementsByTagName("UnivocitaChiave").item(0)
					.getTextContent();
			responseAnalysis.esitoUnitaDocumentaria = esitoUnitaDocumentaria;
		} else {
			responseAnalysis.setErroreLogico("Errore nel recupero dell'esito sull'unità documentaria");
		}
	}

	private void checkEsitoDocumentoPrincipale(Document doc, SipResponseAnalysis responseAnalysis) {
		NodeList nl = xmlUtils
				.searchInXml(doc, "/EsitoVersamento/UnitaDocumentaria/DocumentoPrincipale/EsitoDocumento");
		if (nl != null) {
			Element element = (Element) nl.item(0);

			EsitoDocumentoPrincipale esitoDocumentoPrincipale = new EsitoDocumentoPrincipale();
			esitoDocumentoPrincipale.codiceEsito = element.getElementsByTagName("CodiceEsito").item(0).getTextContent();
			esitoDocumentoPrincipale.verificaTipoDocumento = element.getElementsByTagName("VerificaTipoDocumento")
					.item(0).getTextContent();

			responseAnalysis.esitoDocumentoPrincipale = esitoDocumentoPrincipale;
		} else {
			responseAnalysis.setErroreLogico("Errore nel recupero dell'esito sul documento principale");
		}
	}

	private void extractIdSip(Document doc, SipResponseAnalysis responseAnalysis) {
		NodeList nl = xmlUtils.searchInXml(doc, "/EsitoVersamento/IdSIP");
		if (nl != null) {
			Element element = (Element) nl.item(0);
			String idSip = element.getTextContent();
			responseAnalysis.setIdSIP(idSip);
		} else {
			responseAnalysis.setErroreLogico("Errore nel recupero dell'id SIP");
		}
	}

	/**
	 * Analizza la risposta alla chiamata del web service fornita come stringa e
	 * restituisce un oggetto che riassume l'esito
	 * 
	 * @param response
	 *            il contenuto del corpo della risposta http ottenuta dalla
	 *            chiamata al web service di versamento
	 * @return istanza di {@link SipResponseAnalysis}
	 */
	public SipResponseAnalysis analyzeResponse(String response) {

		SipResponseAnalysis sipResponseAnalysis = new SipResponseAnalysis();

		// tenta la conversione del corpo risposta in documento xml
		Document xmlDocument = xmlUtils.convertToXmlDocument(response);
		if (xmlDocument == null) {
			log.error("errore nella conversione in XML della risposta");
			sipResponseAnalysis
					.setErroreLogico("impossibile ricavare un documento XML dalla stringa specificata - conversione fallita");
			return sipResponseAnalysis;
		}

		checkEsitoGenerale(xmlDocument, sipResponseAnalysis);
		checkEsitoChiamataWS(xmlDocument, sipResponseAnalysis);
		checkEsitoXSD(xmlDocument, sipResponseAnalysis);
		checkEsitoUnitaDocumentaria(xmlDocument, sipResponseAnalysis);
		checkEsitoDocumentoPrincipale(xmlDocument, sipResponseAnalysis);

		if (!sipResponseAnalysis.isPresenzaErroreLogico()) {
			extractIdSip(xmlDocument, sipResponseAnalysis);
		}
		return sipResponseAnalysis;
	}

}
