package com.seda.payer.rtbatch.invio;

import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_INVIO;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.seda.payer.rtbatch.StatoProtocollazione;
import com.seda.payer.rtbatch.base.util.StringUtils;
import com.seda.payer.rtbatch.invio.response.EsitoChiamataWS;
import com.seda.payer.rtbatch.invio.response.EsitoDocumentoPrincipale;
import com.seda.payer.rtbatch.invio.response.EsitoGenerale;
import com.seda.payer.rtbatch.invio.response.EsitoUnitaDocumentaria;
import com.seda.payer.rtbatch.invio.response.EsitoXSD;
import com.seda.payer.rtbatch.invio.response.ResponseAnalyzer;
import com.seda.payer.rtbatch.invio.response.SipResponseAnalysis;

/**
 * invia la ricevuta telematica al web service di versamento
 */
public class RtVersamentoExecutor {

	private static final Logger log = Logger.getLogger(LOGGER_CATEGORY_INVIO);

	private String targetUrl;

	public RtVersamentoExecutor(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	/**
	 *
	 */
	class HttpCallOutcome {
		boolean success;
		String errorMessage;
		String responseContent;

		@Override
		public String toString() {
			return "HttpCallOutcome{" + "success=" + success + ", errorMessage='" + errorMessage + '\''
					+ ", responseContent='" + responseContent + '\'' + '}';
		}

	}

	/**
	 * Esegue la chiamata http al servizio di versamento.
	 * <p>
	 * La chiamata è totalmente gestita, questo metodo non lancia eccezione, ma
	 * gestisce tutti i possibili esiti popolando l'oggetto
	 * {@link HttpCallOutcome} che incapsula i risultati.
	 * </p>
	 * 
	 * @param vmd
	 *            metadati necessari al versamento
	 * @return un {@link HttpCallOutcome}
	 */
	private HttpCallOutcome makeHttpCall(VersamentoMetadata vmd) {
		// l'oggetto per il verbo post
		HttpPost httpPost = new HttpPost(targetUrl);

		// entità di richiesta: multipart, dovendo contenere i parametri
		// testuali e la forma binaria della RT
		HttpEntity requestEntity = MultipartEntityBuilder.create().addTextBody("VERSIONE", vmd.getVersioneWS())
				.addTextBody("LOGINNAME", vmd.getLoginName()).addTextBody("PASSWORD", vmd.getPassword()).addTextBody(
						"XMLSIP", vmd.getXmlSip()).addBinaryBody(vmd.getFileId(), vmd.getFileContent()).build();

		httpPost.setEntity(requestEntity);

		// client
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

		// risposta http
		CloseableHttpResponse response = null;

		// risposta logica da restituire
		HttpCallOutcome callOutcome = new HttpCallOutcome();

		try {
			// esegue
			response = closeableHttpClient.execute(httpPost);
			logResponseInfo(response);

			// prende l'entità contenuta nella risposta
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				// se l'entità di risposta esiste, si suppone in prima istanza
				// che la chiamata ha avuto successo
				callOutcome.success = true;
				callOutcome.responseContent = EntityUtils.toString(responseEntity);
			} else {
				// se non c'è entità di risposta c'è subito una condizione di
				// errore
				callOutcome.success = false;
				callOutcome.errorMessage = "Nessuna risposta ottenuta dal web service di versamento";
			}
		}

		/* gestione delle condizioni di errore */
		catch (IOException e) {
			String errorMessage = "Errore I/O durante la chiamata al web service di versamento : ";
			log.error(errorMessage, e);
			callOutcome.success = false;
			callOutcome.errorMessage = errorMessage + e;
		} catch (Exception e) {
			String errorMessage = "Errore generico durante la chiamata al web service di versamento : ";
			log.error(errorMessage, e);
			callOutcome.success = false;
			callOutcome.errorMessage = errorMessage + e;
		}
		/* chiusura degli oggetti usati */
		finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					log.error("eccezione in chiusura della risposta (sarebbe da ignorare)", e);
				}
			}
		}
		// fine
		return callOutcome;
	}

	/**
	 * Analizza la risposta
	 * 
	 * @param sra
	 *            istanza {@link SipResponseAnalysis} derivata dall'analisi del
	 *            contenuto xml della risposta http
	 * @param vr
	 *            istanza di {@link VersamentoResult} che conterrà il risultato
	 *            della validazione
	 */
	private void validateResponse(SipResponseAnalysis sra, VersamentoResult vr) {
		String ESITO_POSITIVO = "POSITIVO";
		ArrayList<String> errorMessages = new ArrayList<String>();
		ArrayList<String> errorCodes = new ArrayList<String>();

		{
			EsitoGenerale esitoGenerale = sra.esitoGenerale();
			if (!esitoGenerale.codiceEsito().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito generale : [%s - %s - %s]", esitoGenerale.codiceEsito(),
						esitoGenerale.codiceErrore(), esitoGenerale.messaggioErrore()));
				errorCodes.add("ERGN");
			}
		}

		{
			EsitoChiamataWS esitoChiamataWS = sra.esitoChiamataWS();
			if (!esitoChiamataWS.versioneWsCorretta().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito chiamata WS, versione : [%s]", esitoChiamataWS
						.versioneWsCorretta()));
				errorCodes.add("WSVR");
			}
			if (!esitoChiamataWS.credenzialiOperatore().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito chiamata WS, credenziali operatore : [%s]", esitoChiamataWS
						.credenzialiOperatore()));
				errorCodes.add("AUTH");
			}
			if (!esitoChiamataWS.fileAttesiRicevuti().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito chiamata WS, file attesi ricevuti : [%s]", esitoChiamataWS
						.fileAttesiRicevuti()));
				errorCodes.add("EXFL");
			}
		}

		{
			EsitoXSD esitoXSD = sra.esitoXSD();
			if (!esitoXSD.codiceEsito().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito controllo XSD : [%s]", esitoXSD.codiceEsito()));
				errorCodes.add("XVAL");
			}
			if (!esitoXSD.controlloStrutturaXML().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito controllo XSD, struttura XML : [%s]", esitoXSD
						.controlloStrutturaXML()));
				errorCodes.add("XSTR");
			}
		}

		{
			EsitoUnitaDocumentaria esitoUnitaDocumentaria = sra.esitoUnitaDocumentaria();
			if (!esitoUnitaDocumentaria.codiceEsito().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito unità documentaria : [%s]", esitoUnitaDocumentaria
						.codiceEsito()));
				errorCodes.add("UDER");
			}
			if (!esitoUnitaDocumentaria.identificazioneVersatore().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito unità documentaria, identificazione versatore: [%s]",
						esitoUnitaDocumentaria.identificazioneVersatore()));
				errorCodes.add("UDVR");
			}
			if (!esitoUnitaDocumentaria.univocitaChiave().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito unità documentaria, unicità chiave : [%s]",
						esitoUnitaDocumentaria.univocitaChiave()));
				errorCodes.add("UDUK");
			}
		}

		{
			EsitoDocumentoPrincipale esitoDocumentoPrincipale = sra.esitoDocumentoPrincipale();
			if (!esitoDocumentoPrincipale.codiceEsito().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito documento principale: %s", esitoDocumentoPrincipale
						.codiceEsito()));
				errorCodes.add("DPER");
			}
			if (!esitoDocumentoPrincipale.verificaTipoDocumento().equalsIgnoreCase(ESITO_POSITIVO)) {
				errorMessages.add(String.format("Errore esito documento principale, verifica tipo documento : %s",
						esitoDocumentoPrincipale.verificaTipoDocumento()));
				errorCodes.add("DPTP");
			}
		}
		vr.errorCodes = StringUtils.listAsString(errorCodes);
		vr.errorMessage = StringUtils.listAsString(errorMessages);
		if (vr.isGloballyCorrect()) {
			log.info("esito del versamento globalmente corretto");
			vr.setIdSip(sra.getIdSIP());
		}
		log.debug(vr);
	}

	/**
	 * Esegue il versamento della RT contenuta nell'oggetto passato verso il
	 * servizio remoto
	 * 
	 * @param vmd
	 *            metadati necessari al versamento
	 * @return istanza di {@link VersamentoResult}
	 */
	public VersamentoResult eseguiVersamento(VersamentoMetadata vmd) {
		VersamentoResult versamentoResult = new VersamentoResult();

		// esegue la chiamata http
		log.info("esecuzione chiamata al web service remoto");
		HttpCallOutcome callOutcome = makeHttpCall(vmd);
		log.debug("risultato chiamata: " + callOutcome);

		// se dal risultato c'è esito positivo
		if (callOutcome.success) {
			// si esamina ulteriormente il contenuto della risposta
			SipResponseAnalysis responseAnalysis = new ResponseAnalyzer().analyzeResponse(callOutcome.responseContent);
			validateResponse(responseAnalysis, versamentoResult);
			versamentoResult.transportOutcome = VersamentoResult.RESULT_OK;
		} else {
			// altrimenti si marca il risultato globale come fallito
			versamentoResult.transportOutcome = VersamentoResult.RESULT_KO;
			versamentoResult.errorMessage = callOutcome.errorMessage;
		}

		// impostazioni finali dell'oggetto da restituire
//		if (versamentoResult.isGloballyCorrect()) {
//			versamentoResult.statoProtocollazione = StatoProtocollazione.;
//		} else {
//			versamentoResult.statoProtocollazione = "NON PROTOCOLLATO";
//		}

		// fine
		return versamentoResult;
	}

	/**
	 * helper di debug
	 * 
	 * @param chr
	 *            risposta da loggare
	 */
	private void logResponseInfo(CloseableHttpResponse chr) {
		log.debug("getProtocolVersion()         :" + chr.getProtocolVersion());
		log.debug("getStatusCode()              :" + chr.getStatusLine().getStatusCode());
		log.debug("statusLine.toString()        :" + chr.getStatusLine().toString());
		log.debug("statusLine.getReasonPhrase() :" + chr.getStatusLine().getReasonPhrase());
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		/*
		 * curl --location --request POST "https://postman-echo.com/post" \
		 * --data "This is expected to be sent back as part of response body."
		 */
		// l'oggetto per il verbo post
		// HttpPost httpPost = new HttpPost("https://postman-echo.com/post");
		HttpPost httpPost = new HttpPost("https://stage-poloconservazione.regione.marche.it");

		// entità di richiesta: multipart, dovendo contenere i parametri
		// testuali e la forma binaria della RT
		HttpEntity requestEntity = MultipartEntityBuilder.create().addTextBody("VERSIONE", "1.4").addTextBody(
				"LOGINNAME", "nazzo").addTextBody("PASSWORD", "soSecret-MuchSecurity").addTextBody("XMLSIP", "sipsip")
				.addBinaryBody("SAMPLE_ID", "contenuto_fittizio_nondimeno_mutabile_in_binario".getBytes()).build();

		httpPost.setEntity(requestEntity);

		// client
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

		// risposta http
		CloseableHttpResponse response = null;

		try {
			// esegue
			response = closeableHttpClient.execute(httpPost);
			System.out.println("getProtocolVersion()         :" + response.getProtocolVersion());
			System.out.println("getStatusCode()              :" + response.getStatusLine().getStatusCode());
			System.out.println("statusLine.toString()        :" + response.getStatusLine().toString());
			System.out.println("statusLine.getReasonPhrase() :" + response.getStatusLine().getReasonPhrase());

			// prende l'entità contenuta nella risposta
			HttpEntity responseEntity = response.getEntity();

			if (responseEntity != null) {
				String responseContent = EntityUtils.toString(responseEntity);
				System.out.println("responseContent : " + responseContent);
			} else {
				// se non c'è entità di risposta c'è subito una condizione di
				// errore
				System.err.println("errore....");
			}
		}

		/* gestione delle condizioni di errore */
		catch (IOException e) {
			String errorMessage = "Errore I/O durante la chiamata al web service di versamento : ";
			System.err.println(errorMessage);
			e.printStackTrace();
		} catch (Exception e) {
			String errorMessage = "Errore generico durante la chiamata al web service di versamento : ";
			System.err.println(errorMessage);
		}
		/* chiusura degli oggetti usati */
		finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					log.error("eccezione in chiusura della risposta (sarebbe da ignorare)", e);
				}
			}
		}

	}

}
