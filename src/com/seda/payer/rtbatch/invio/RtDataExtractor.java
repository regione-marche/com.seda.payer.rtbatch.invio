package com.seda.payer.rtbatch.invio;


import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * Estrae informazioni rilevanti dalla ricevuta telematica fornita come solo
 * testo
 */
public class RtDataExtractor {

	private static XPath xpath;

	static {
		xpath = XPathFactory.newInstance().newXPath();
	}

	private Document xmlDocument = null;



	/**
	 * Inizializza il motore di estrazione ed esegue il parsing della ricevuta
	 * telematica specificata
	 * <p>
	 * Ci si aspetta che la RT passata sia in xml sintatticamente corretto
	 *
	 * @param strRt ricevuta telematica in formato stringa
	 */
	public RtDataExtractor(String strRt) throws ReceiptMetadataGenerationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ReceiptMetadataGenerationException("Errore nella creazione del document builder XML", e);
		}

		InputSource inputSource = new InputSource(new StringReader(strRt));

		try {
			xmlDocument = builder.parse(inputSource);
		} catch (Exception e) {
			String errorMessage = String.format("Errore nella trasformazione della ricevuta in XML - ricevuta in esame: [%s]",
					strRt);
			throw new ReceiptMetadataGenerationException(errorMessage, e);
		}
	}



	public String getIdentificativoUnivocoVersamento() throws ReceiptMetadataGenerationException {
		NodeList nl = extractNodelist("/RT/datiPagamento/identificativoUnivocoVersamento");
		Element element = (Element) nl.item(0);
		return element.getTextContent();
	}



	public String getDataEsitoSingoloPagamento() throws ReceiptMetadataGenerationException {
		NodeList nl = extractNodelist("/RT/datiPagamento/datiSingoloPagamento/dataEsitoSingoloPagamento");
		Element element = (Element) nl.item(0);
		return element.getTextContent();
	}



	public String getIdentificativoUnivocoRiscossione() throws ReceiptMetadataGenerationException {
		NodeList nl = extractNodelist("/RT/datiPagamento/datiSingoloPagamento/identificativoUnivocoRiscossione");
		Element element = (Element) nl.item(0);
		return element.getTextContent();
	}



	public String getImportoTotalePagato() throws ReceiptMetadataGenerationException {
		NodeList nl = extractNodelist("/RT/datiPagamento/importoTotalePagato");
		Element element = (Element) nl.item(0);
		return element.getTextContent();
	}



	public String getSoggettoPagatore() throws ReceiptMetadataGenerationException {
		NodeList nl = extractNodelist("/RT/soggettoPagatore/anagraficaPagatore");
		Element element = (Element) nl.item(0);
		return element.getTextContent();
	}



	public String getSoggettoVersante() throws ReceiptMetadataGenerationException {
		String soggettoVersante = "";
		NodeList nl = extractNodelist("/RT/soggettoVersante/anagraficaVersante");
		if (nl!=null) {
			Element element = (Element) nl.item(0);
			if (element!=null)
				soggettoVersante = element.getTextContent();
		}
		return soggettoVersante;
	}



	public String getIstitutoAttestante() throws ReceiptMetadataGenerationException {
		NodeList nl = extractNodelist("/RT/istitutoAttestante/denominazioneAttestante");
		Element element = (Element) nl.item(0);
		return element.getTextContent();
	}



	private NodeList extractNodelist(String strXpath) throws ReceiptMetadataGenerationException {
		XPathExpression xPathExpression = null;
		try {
			xPathExpression = xpath.compile(strXpath);
		} catch (XPathExpressionException e) {
			// ignorata
		}

		try {
			return (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			String errorMessage = String
					.format("Errore nell'estrazione dei dati dalla forma XML della ricevuta - xpath richiesto [%s]", strXpath);
			throw new ReceiptMetadataGenerationException(errorMessage, e);
		}
	}

}
