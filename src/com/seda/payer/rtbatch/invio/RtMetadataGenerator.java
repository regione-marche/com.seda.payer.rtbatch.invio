package com.seda.payer.rtbatch.invio;

import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_CONTROLLO;
import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_INVIO;

import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.seda.payer.rtbatch.base.commons.EnteDto;
import com.seda.payer.rtbatch.base.commons.RicevutaTelematicaDto;
import com.seda.payer.rtbatch.base.util.HashUtils;
import com.seda.payer.rtbatch.base.util.StringUtils;
import com.seda.payer.rtbatch.schema.sip.UnitaDocumentaria;

/**
 * Genera i metadati sulla ricevuta telematica da inviare.
 */
class RtMetadataGenerator {

	private static final Logger log = Logger.getLogger(LOGGER_CATEGORY_INVIO);
	private static final Logger logControllo = Logger.getLogger(LOGGER_CATEGORY_CONTROLLO);

	private static final String VERSIONE = "1.4";
	private static final String TIPO_REGISTRO = "RT";
	private static final String TIPOLOGIA_UNITA_DOCUMENTARIA = "Ricevuta Telematica";
	private static final String OGGETTO = "Oggetto della ricevuta telematica con IUV: ";
	private static final String TIPO_STRUTTURA = "DocumentoGenerico";

	private static final String INTESTAZIONE_VERSATORE_AMBIENTE = "MARCHE DIGIP_PRE";

	private static final String COMPONENTE_ID = "P1";
	private static final String TIPO_COMPONENTE = "CONTENUTO";
	private static final String TIPO_SUPPORTO_COMPONENTE = "FILE";
	private static final String FORMATO_FILE_VERSATO = "xml";
	
	private static final DateFormat DF_YEAR = new SimpleDateFormat("yyyy");
	private static final DateFormat DF_PLAIN_DATE = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Genera i metadati SIP per l'invio
	 * 
	 * @param ente
	 *            {@link EnteDto} corrente in elaborazione
	 * @param ricevutaTelematicaDto
	 *            {@link RicevutaTelematicaDto} da spedire
	 * @param sequenceNumber
	 *            progressivo di invio
	 * @return VersamentoMetadata con i dati per l'invio al web service
	 */
	VersamentoMetadata generateMetadata(EnteDto ente, RicevutaTelematicaDto ricevutaTelematicaDto, long sequenceNumber)
			throws ReceiptMetadataGenerationException {

		final Date currentDate = new Date();

		try {
			RtDataExtractor rtDataExtractor = new RtDataExtractor(ricevutaTelematicaDto.getCRPTRT());
			log.info("generazione metadati per : " + ricevutaTelematicaDto);
			
			String[] sSplit = getSplit_NString(4, ente.getCCFTIDPA());
			String sCcftidpa = sSplit[0];
			String sAmbiente = sSplit[1];
			String sEnte = sSplit[2];
			String sStruttura = sSplit[3];
			
			if (sCcftidpa.trim().length()==0) 
				throw new Exception ("Configurazione codice IDPA assente");
			if (sAmbiente.trim().length()==0) 
				throw new Exception ("Descrizione ambiente versatore assente");
			if (sEnte.trim().length()==0) 
				throw new Exception ("Descrizione ente versatore assente");
			if (sStruttura.trim().length()==0) 
				throw new Exception ("Descrizione struttura versatore assente");
			
			Properties p = new Properties();
			p.setProperty("intestazione.versione", VERSIONE);
			p.setProperty("intestazione.tipologiaUnitaDocumentaria", TIPOLOGIA_UNITA_DOCUMENTARIA);

			p.setProperty("intestazione.versatore.ambiente", sAmbiente);
						
			p.setProperty("intestazione.versatore.ente", sEnte);
			p.setProperty("intestazione.versatore.struttura", sStruttura);
			p.setProperty("intestazione.versatore.userID", ente.getCCFTUSER());

			p.setProperty("intestazione.chiave.anno", DF_YEAR.format(currentDate));
			p.setProperty("intestazione.chiave.numero", String.format("%010d", sequenceNumber));
			p.setProperty("intestazione.chiave.tipoRegistro", TIPO_REGISTRO);

			p.setProperty("configurazione.forzaConservazione", Boolean.TRUE.toString());
			p.setProperty("configurazione.forzaAccettazione", Boolean.TRUE.toString());

			p.setProperty("profiloUnitaDocumentaria.oggetto", OGGETTO.concat(rtDataExtractor
					.getIdentificativoUnivocoVersamento().trim()));
			p.setProperty("profiloUnitaDocumentaria.data", DF_PLAIN_DATE.format(currentDate));
			p.setProperty("datiSpecifici.identificativoUnivocoVersamento", rtDataExtractor
					.getIdentificativoUnivocoVersamento().trim());

			p.setProperty("datiSpecifici.identificativoUnivocoRiscossione", rtDataExtractor
					.getIdentificativoUnivocoRiscossione().trim());
			//inizio LP PG22XX06 - 20220630 - Bug dataSingoloPagamento in isoDate
			//p.setProperty("datiSpecifici.dataEsitoSingoloPagamento", rtDataExtractor.getDataEsitoSingoloPagamento()
			//		.trim());
			p.setProperty("datiSpecifici.dataEsitoSingoloPagamento", rtDataExtractor.getDataEsitoSingoloPagamento().trim().substring(0, 10));
			//fine LP PG22XX06 - 20220630 - Bug dataSingoloPagamento in isoDate
			p.setProperty("datiSpecifici.importoTotalePagato", rtDataExtractor.getImportoTotalePagato().trim());

			p.setProperty("datiSpecifici.soggettoPagatore", rtDataExtractor.getSoggettoPagatore().trim());
			p.setProperty("datiSpecifici.soggettoVersante", rtDataExtractor.getSoggettoVersante().trim());
			p.setProperty("datiSpecifici.istitutoAttestante", rtDataExtractor.getIstitutoAttestante().trim());

			p.setProperty("documentoPrincipale.iDDocumento", UUID.randomUUID().toString());
			p.setProperty("documentoPrincipale.TipoDocumento", TIPOLOGIA_UNITA_DOCUMENTARIA); // "ricevuta telematica"
			p.setProperty("documentoPrincipale.StrutturaOriginale.TipoStruttura", TIPO_STRUTTURA);

			long timestamp = System.currentTimeMillis();
			String nomeFileXml = timestamp + ".xml";

			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.id", COMPONENTE_ID);
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.OrdinePresentazione", "1");
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.TipoComponente",
					TIPO_COMPONENTE);
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.TipoSupportoComponente",
					TIPO_SUPPORTO_COMPONENTE);
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.NomeComponente", nomeFileXml);
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.FormatoFileVersato", FORMATO_FILE_VERSATO);

			String sipHash = null;
			try {
				sipHash = HashUtils.getSha256Hash(ricevutaTelematicaDto.getCRPTRT());
			} catch (NoSuchAlgorithmException e) {
				throw new ReceiptMetadataGenerationException("Impossibile ottenere l'hash del documento", e);
			}
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.HashVersato", sipHash);
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.IDComponenteVersato", ""
					+ timestamp);
			p.setProperty("documentoPrincipale.StrutturaOriginale.Componenti.Componente.UtilizzoDataFirmaPerRifTemp",
					"false");

			UnitaDocumentaria unitaDocumentaria = new UnitaDocumentaria().buildSipElement(p);

//			if (Main.isDryRun()) {
//				unitaDocumentaria.getConfigurazione().setSimulaSalvataggioDatiInDB("true");
//			}

			String xmlSip = convertToXmlAndMarshal(unitaDocumentaria);
			log.info("SIP XML da inviare:\n" + xmlSip);
			logControllo.debug("visualizzazione preventiva del SIP che si sta per inviare: " + xmlSip);
			VersamentoMetadata versamentoMetadata = new VersamentoMetadata();
			versamentoMetadata.setXmlSip(xmlSip);

			versamentoMetadata.setFileContent(ricevutaTelematicaDto.getCRPTRT().getBytes());
			// fileId viene usato come identificativo per la parte binaria della
			// richiesta http
			// ora è impostato al valore dell'id dell' (unico) componente del
			// doc principale dell'unità documentaria
			versamentoMetadata.setFileId(unitaDocumentaria.getDocumentoPrincipale().getStrutturaOriginale()
					.getComponenti().getComponente().getID());
			versamentoMetadata.setLoginName(ente.getCCFTUSER());

			String decodedPassword = StringUtils.base64Decode(ente.getCCFTPASW());
			versamentoMetadata.setPassword(decodedPassword);

			return versamentoMetadata;
		} catch (Exception e) {
			log.error("errore nel processo di generazione metadati", e);
			throw new ReceiptMetadataGenerationException("errore nel processo di generazione metadati", e);
		}
	}

	private String convertToXmlAndMarshal(UnitaDocumentaria ud) throws ReceiptMetadataGenerationException {
		JAXBContext context = null;
		Marshaller marshaller = null;
		try {
			context = JAXBContext.newInstance(ud.getClass());
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter sw = new StringWriter();
			marshaller.marshal(ud, sw);
			return sw.getBuffer().toString();
		} catch (JAXBException e) {
			throw new ReceiptMetadataGenerationException("Errore nella trasformazione del SIP in XML", e);
		}
	}
	
	public String[] getSplit_NString(int iNumStrings, String sToSplit)
	{
		String[] sSplit = null;
		if (sToSplit != null)
		{
			String[] sSplitTemp = sToSplit.split("\\|",-1);
			
			if (sSplitTemp.length != iNumStrings)
			{
				sSplit = new String[iNumStrings];
				for (int k=0; k<iNumStrings; k++)
					sSplit[k] = "";
			}
			else
				sSplit = sSplitTemp;	
		}
		else
		{
			sSplit = new String[iNumStrings];
			for (int k=0; k<iNumStrings; k++)
				sSplit[k] = "";
		}
			
		return sSplit;
	}

//	public static void main(String[] args) throws Exception {
//		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pay_i:RT xmlns:pay_i=\"http://www.digitpa.gov.it/schemas/2011/Pagamenti/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"/opt/proctele/resources/PagInf_RPT_RT_6_2_0.xsd\"><pay_i:versioneOggetto>6.2.0</pay_i:versioneOggetto><pay_i:dominio><pay_i:identificativoDominio>80005330420</pay_i:identificativoDominio><pay_i:identificativoStazioneRichiedente>80008630420_1</pay_i:identificativoStazioneRichiedente></pay_i:dominio><pay_i:identificativoMessaggioRicevuta>Imr5ms9fy33k1dmh0amj68q0749j99wu0</pay_i:identificativoMessaggioRicevuta><pay_i:dataOraMessaggioRicevuta>2019-01-16T12:12:06</pay_i:dataOraMessaggioRicevuta><pay_i:riferimentoMessaggioRichiesta>20190116121102</pay_i:riferimentoMessaggioRichiesta><pay_i:riferimentoDataRichiesta>2019-01-16</pay_i:riferimentoDataRichiesta><pay_i:istitutoAttestante><pay_i:identificativoUnivocoAttestante><pay_i:tipoIdentificativoUnivoco>B</pay_i:tipoIdentificativoUnivoco><pay_i:codiceIdentificativoUnivoco>BCITITMM</pay_i:codiceIdentificativoUnivoco></pay_i:identificativoUnivocoAttestante><pay_i:denominazioneAttestante>Intesa Sanpaolo</pay_i:denominazioneAttestante></pay_i:istitutoAttestante><pay_i:enteBeneficiario><pay_i:identificativoUnivocoBeneficiario><pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco><pay_i:codiceIdentificativoUnivoco>80005330420</pay_i:codiceIdentificativoUnivoco></pay_i:identificativoUnivocoBeneficiario><pay_i:denominazioneBeneficiario>Comune di San Benedetto del Tronto</pay_i:denominazioneBeneficiario></pay_i:enteBeneficiario><pay_i:soggettoVersante><pay_i:identificativoUnivocoVersante><pay_i:tipoIdentificativoUnivoco>F</pay_i:tipoIdentificativoUnivoco><pay_i:codiceIdentificativoUnivoco>QRSFRZ79L22E388S</pay_i:codiceIdentificativoUnivoco></pay_i:identificativoUnivocoVersante><pay_i:anagraficaVersante>Fabrizio Quaresima</pay_i:anagraficaVersante><pay_i:e-mailVersante>fabrizio.quaresima@e-sed.it</pay_i:e-mailVersante></pay_i:soggettoVersante><pay_i:soggettoPagatore><pay_i:identificativoUnivocoPagatore><pay_i:tipoIdentificativoUnivoco>F</pay_i:tipoIdentificativoUnivoco><pay_i:codiceIdentificativoUnivoco>QRSFRZ79L22E388S</pay_i:codiceIdentificativoUnivoco></pay_i:identificativoUnivocoPagatore><pay_i:anagraficaPagatore>QUARESIMA  FABRIZIO</pay_i:anagraficaPagatore></pay_i:soggettoPagatore><pay_i:datiPagamento><pay_i:codiceEsitoPagamento>0</pay_i:codiceEsitoPagamento><pay_i:importoTotalePagato>1.00</pay_i:importoTotalePagato><pay_i:identificativoUnivocoVersamento>RF316692</pay_i:identificativoUnivocoVersamento><pay_i:CodiceContestoPagamento>n/a</pay_i:CodiceContestoPagamento><pay_i:datiSingoloPagamento><pay_i:singoloImportoPagato>1.00</pay_i:singoloImportoPagato><pay_i:esitoSingoloPagamento>PAGATA</pay_i:esitoSingoloPagamento><pay_i:dataEsitoSingoloPagamento>2019-01-16</pay_i:dataEsitoSingoloPagamento><pay_i:identificativoUnivocoRiscossione>190160000083</pay_i:identificativoUnivocoRiscossione><pay_i:causaleVersamento>/RFS/RF316692/1.00</pay_i:causaleVersamento><pay_i:datiSpecificiRiscossione>9/123</pay_i:datiSpecificiRiscossione></pay_i:datiSingoloPagamento></pay_i:datiPagamento></pay_i:RT>";
//
//		EnteDto enteDto = new EnteDto();
//		enteDto.setCCFTIDPA("TEST_CCFTIDPA");
//		enteDto.setCCFTUSER("TEST_CCFTUSER");
//		enteDto.setCCFTPASW("testpassword");
//
//		RicevutaTelematicaDto ricevutaTelematicaDto = new RicevutaTelematicaDto();
//		ricevutaTelematicaDto.setCRPTRT(xml);
//
//		RtMetadataGenerator gen = new RtMetadataGenerator();
//		VersamentoMetadata vm = gen.generateMetadata(enteDto, ricevutaTelematicaDto, System.currentTimeMillis());
//		System.out.println(vm);
//		System.out.println(vm.getFileId());
//		System.out.println(vm.getLoginName());
//		System.out.println(vm.getPassword());
//		System.out.println(vm.getVersioneWS());
//		System.out.println(vm.getXmlSip());
//	}

}
