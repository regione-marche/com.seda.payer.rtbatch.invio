package com.seda.payer.rtbatch.invio;

import static com.seda.payer.rtbatch.RtBatchEnvironment.ENV_INVIO_CFG_FILE_LOCATION;
import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_INVIO;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.seda.data.dao.DAOHelper;
import com.seda.payer.rtbatch.base.commons.EnteDto;
import com.seda.payer.rtbatch.base.commons.MailClient;
import com.seda.payer.rtbatch.base.commons.RicevutaTelematicaDto;
import com.seda.payer.rtbatch.base.datalayer.DaoCreationException;
import com.seda.payer.rtbatch.base.datalayer.DaoException;
import com.seda.payer.rtbatch.base.datalayer.DaoFactory;
import com.seda.payer.rtbatch.base.datalayer.RtRepositoryDao;

/**
 * Esegue l'eleborazione batch per un ente specifico.
 * <p>
 * L'utilizzo tipico prevede la creazione, che inizializza le chiamate jdbc, e
 * successivamente l'uso del metodo {@link #processEnte(EnteDto)}.
 * <p>
 * Una volta terminato l'uso di questo oggetto, chiamare il metodo
 * {@link #shutdown()} per rilasciare le risorse jdbc impegnate.
 */
class BatchExecutorEnte {

	private static final Logger log = Logger.getLogger(LOGGER_CATEGORY_INVIO);

	//private Connection connection;
	//private String schema; //LP 20240801

	//inizio LP 20241004 - PGNTBRTB-1
	//private CallableStatement pycftsp_lstrt_batch;
	//private CallableStatement pycftsp_upd_batch;
	//private CallableStatement pyrsqsp_nxt_batch;
	//fine LP 20241004 - PGNTBRTB-1

	private RiepilogoElaborazioneInvio riepilogoElaborazione;

	private Date startTime;

	//inizio LP 2024104
	RtRepositoryDao rtRepositoryDAO = null;

	BatchExecutorEnte(RtRepositoryDao dao) {
		rtRepositoryDAO = dao;
	}
	//fine LP 2024104

//inizio LP 202041004
//	/**
//	 * Inizializzazione.
//	 * <p>
//	 * Crea gli oggetti per le chiamate jdbc alle stored procedures e altri
//	 * oggetti di uso interno.
//	 * </p>
//	 * 
//	 * @throws EnteProcessingException
//	 *             se non vengono inizializzate correttamente le chiamate alle
//	 *             stored procedures
//	 */
//	BatchExecutorEnte() throws EnteProcessingException {
//		riepilogoElaborazione = new RiepilogoElaborazioneInvio();
//		startTime = new Date();
//		try {
//			//inizio LP 20240801 PGNTBRTCN-1
//			//connection = DaoFactory.getInstance().getConnection();
//			//pycftsp_lstrt_batch = connection.prepareCall("{call PYCFTSP_LSTRT_BATCH(?, ?)}");			
//			//pycftsp_lstrt_batch = MetaProcedure.prepareCall(connection, schema, "PYCFTSP_LSTRT_BATCH");
//			//pycftsp_lstrt_batch.setString(2, "N");
//			//pycftsp_upd_batch = connection.prepareCall("{call PYCFTSP_UPD_BATCH(?, ?, ?, ?, ?, ?, ?)}");
//			//pyrsqsp_nxt_batch = connection.prepareCall("{call PYKEYSP_BIGINT(?,?)}");
//			RtRepositoryDao appo = DaoFactory.getInstance().createDao();
//			pycftsp_lstrt_batch = appo.getPycftsp_lstrt_batch();
//			pycftsp_lstrt_batch.setString(2, "Y");
//			pycftsp_upd_batch = appo.getPycftsp_upd_batch();
//			pyrsqsp_nxt_batch = appo.getPykeysp_bigint();
//			//fine LP 20240801 PGNTBRTCN-1
//		} catch (SQLException e) {
//			shutdown();
//			String errorMessage = "Eccezione nella preparazione delle procedure di accesso ai dati";
//			log.error(errorMessage, e);
//			throw new EnteProcessingException(errorMessage, e);
//		//inizio LP 20240801 PGNTBRTCN-1
//		} catch (DaoCreationException e) {
//			shutdown();
//			String errorMessage = "Eccezione nella preparazione delle procedure di accesso ai dati";
//			log.error(errorMessage, e);
//			throw new EnteProcessingException(errorMessage, e);
//		} catch (DaoException e) {
//			shutdown();
//			String errorMessage = "Eccezione nella preparazione delle procedure di accesso ai dati";
//			log.error(errorMessage, e);
//			throw new EnteProcessingException(errorMessage, e);
//		}
//		//fine LP 20240801 PGNTBRTCN-1
//	}
//fine LP 202041004

	/**
	 * Aggiorna i contatori globali sulla base del risultato dell'invio
	 * 
	 * @param versamentoResult
	 *            risultato dell'invio di una RT
	 */
	private void aggiornaContatoriPostVersamento(VersamentoResult versamentoResult) {
		// incrementa il totale di RT elaborate
		riepilogoElaborazione.totaleRtElaborate++;

		// incrementa il numero di invii corretti se l'invio è avvenuto
		// correttamente sia a livello di trasporto che a livello semantico
		if (versamentoResult.getTransportOutcome().equals(VersamentoResult.RESULT_OK)
				&& versamentoResult.isGloballyCorrect()) {
			riepilogoElaborazione.inviiCorretti++;
		}
		// altrimenti incrementa il numero di invii falliti
		else {
			riepilogoElaborazione.inviiConErrore++;
		}
	}

	/**
	 * Aggiorna nel db la RT di cui alla chiave specificata con il risultato
	 * dell'operazione di versamento.
	 * 
	 * @param rtKey
	 *            chiave della RT
	 * @param versamentoResult
	 *            oggetto {@link VersamentoResult}
	 */
	private void aggiornaStatoInvioRicevuta(Long rtKey, VersamentoResult versamentoResult) {
		//inizio LP 20241004 - PGNTBRTB-1
		CallableStatement pycftsp_upd_batch = null;
		//fine LP 20241004 - PGNTBRTB-1

		try {
			//inizio LP 20241004 - PGNTBRTB-1
			if(rtRepositoryDAO == null) {
				riepilogoElaborazione.erroriInterni++;
				riepilogoElaborazione.erroriAggiornamentoStatoRT++;
				log.error("rtRepositoryDAO == null");
				return;
			}
			pycftsp_upd_batch = rtRepositoryDAO.getPycftsp_upd_batch();
			//fine LP 20241004 - PGNTBRTB-1
			/*
			 * IN I_RPT_PRPTKEY BIGINT, IN I_RPT_CRPTPROT VARCHAR(30), IN
			 * I_RPT_CRPTERRC VARCHAR(100), IN I_RPT_CRPTESIT VARCHAR(2), IN
			 * I_RPT_CRPTISIP VARCHAR(50), IN I_RPT_CRPTNPRT VARCHAR(70), OUT
			 * O_TOTROWS INTEGER
			 */
			// imposta i parametri per la SP di aggiornamento
			pycftsp_upd_batch.setLong(1, rtKey);
			pycftsp_upd_batch.setNull(2, Types.VARCHAR);
			pycftsp_upd_batch.setString(3, versamentoResult.getErrorCodes());
			pycftsp_upd_batch.setString(4, versamentoResult.isGloballyCorrect() ? "OK" : "KO");
			pycftsp_upd_batch.setString(5, versamentoResult.getIdSip());
			pycftsp_upd_batch.setNull(6, Types.VARCHAR);
			pycftsp_upd_batch.registerOutParameter(7, Types.INTEGER);
		//inizio LP 20241004 - PGNTBRTB-1
		//} catch (SQLException e) {
		} catch (Exception e) {
			DAOHelper.closeIgnoringException(pycftsp_upd_batch);
		//fine LP 20241004 - PGNTBRTB-1
			riepilogoElaborazione.erroriInterni++;
			riepilogoElaborazione.erroriAggiornamentoStatoRT++;
			String errorMessage = String.format("errore in impostazione parametri per aggiornamento di stato invio RT <%s>", rtKey);
			log.error(errorMessage, e);
			log.error(versamentoResult);
			return;
		}
		int updatedRows = -1;
		try {
			pycftsp_upd_batch.executeQuery();
			updatedRows = pycftsp_upd_batch.getInt(7);
		} catch (SQLException e) {
			riepilogoElaborazione.erroriInterni++;
			riepilogoElaborazione.erroriAggiornamentoStatoRT++;
			log.error("Errore in aggiornamento o in recupero delle righe aggiornate");
			String errorLogLine = String.format("chiave della riga da aggiornare <%s> | risultato versamento <%s>", rtKey, versamentoResult);
			log.error(errorLogLine, e);
			return;
		//inizio LP 20241004 - PGNTBRTB-1
		} finally {
			DAOHelper.closeIgnoringException(pycftsp_upd_batch);
		//fine LP 20241004 - PGNTBRTB-1
		}
		if (updatedRows == 0) {
			riepilogoElaborazione.erroriInterni++;
			riepilogoElaborazione.erroriAggiornamentoStatoRT++;
			log.warn("Condizione anomala: stored procedure di aggiornamento stato riga eseguita correttamente, ma questa non riporta alcuna riga aggiornata");
			String warningLine = String.format("chiave della riga da aggiornare <%s> | risultato versamento <%s>", rtKey, versamentoResult);
			log.warn(warningLine);
			return;
		}
		if (updatedRows == -1) {
			riepilogoElaborazione.erroriInterni++;
			riepilogoElaborazione.erroriAggiornamentoStatoRT++;
			log.warn("Condizione anomala: valore delle righe aggiornate NON modificato");
			String warningLine = String.format("chiave della riga da aggiornare <%s> | risultato versamento <%s>", rtKey, versamentoResult);
			log.warn(warningLine);
		}
	}

	/**
	 * Elaborazione principale di un Ente.
	 * 
	 * @param ente
	 *            un {@link EnteDto} che contiene le informazioni sull'ente da
	 *            elaborare
	 */
	void processEnte(EnteDto ente) throws EnteProcessingException {

		// genera i metadati per la RT da inviare
		RtMetadataGenerator metadataGenerator = new RtMetadataGenerator();

		// inizializza gli oggetti di supporto jdbc
		ResultSet rsListaRicevute = null;

		// inizializza l'oggetto che invia le RT al servizio remoto
		RtVersamentoExecutor ricevutaTelematicaVersamento = new RtVersamentoExecutor(ente.getCCFTURLI());
		//inizio LP 20241004 - PGNTBRTB-1
		CallableStatement pycftsp_lstrt_batch = null;
		//fine LP 20241004 - PGNTBRTB-1
		try {
			// ottiene l'elenco di RT da inviare per l'ente corrente
			//inizio LP 20241004 - PGNTBRTB-1
			if(rtRepositoryDAO == null) {
				throw new EnteProcessingException("RtRepositoryDAO == null");
			}
			pycftsp_lstrt_batch = rtRepositoryDAO.getPycftsp_lstrt_batch();
			//fine LP 20241004 - PGNTBRTB-1
			
			pycftsp_lstrt_batch.setString(1, ente.getCCFTIDPA());
			//inizio LP 20240801
			//rsListaRicevute = pycftsp_lstrt_batch.executeQuery();
			if(pycftsp_lstrt_batch.execute()) {
				rsListaRicevute = pycftsp_lstrt_batch.getResultSet();
				if(rsListaRicevute != null) {
			//fine LP 20240801
					// elabora la lista delle RT
					processResultSetRicevute(ente, rsListaRicevute, metadataGenerator, ricevutaTelematicaVersamento);
					// questo si commenta da solo
					inviaEmailRiepilogativa(ente.getCCFTEMAI());
			//inizio LP 20240801
				}
			}
			//fine LP 20240801
			//inizio LP 20241004 - PGNTBRTB-1
			//} catch (SQLException e) {
		} catch (Exception e) {
			if(e instanceof SQLException) {
		//fine LP 20241004 - PGNTBRTB-1
				String errorMessageFmt = "Errore nell'accesso ai dati o al recupero di informazioni dalle procedure - SQLSTATE %s - SQLCODE %d";
				String errorMessage = String.format(errorMessageFmt, ((SQLException) e).getSQLState(), ((SQLException) e).getErrorCode());
				log.error(errorMessage, e);
				throw new EnteProcessingException(errorMessage, e);
				//inizio LP 20241004 - PGNTBRTB-1
			}
			//fine LP 20241004 - PGNTBRTB-1
			throw new EnteProcessingException(e.getMessage(), e);
		} finally {
			//inizio LP 20241004 - PGNTBRTB-1
			DAOHelper.closeIgnoringException(pycftsp_lstrt_batch);
			//fine LP 20241004 - PGNTBRTB-1
			closeRs(rsListaRicevute);
		}
	}

	/**
	 * Esegue le elaborazioni sul resultset che contiene le ricevute
	 * telematiche.
	 * <p>
	 * Trasforma la riga del resultset in un dto (classe
	 * {@link RicevutaTelematicaDto}), re-interroga il db per ottenere un
	 * progressivo unico, crea i metadati per inviare la RT al servizio di
	 * versamento, esegue il versamento della RT, aggiorna i contatori globali
	 * sulla base del risultato dell'invio e aggiorna il db con il risultato di
	 * invio della ricevuta.
	 * </p>
	 * <p>
	 * Le operazioni sono eseguite in sequenza; il fallimento di una singola
	 * operazione provoca l'interruzione dell'elaborazione per la RT corrente,
	 * passando pertanto alla successiva. In caso di fallimento, tipicamente i
	 * contatori sono aggiornati concordemente.
	 * </p>
	 * 
	 * @param ente
	 *            ente corrente in elaborazione
	 * @param rsListaRicevute
	 *            {@link ResultSet} con le RT da elaborare per l'ente corrente
	 * @param metadataGenerator
	 *            oggetto di supporto per creare i metadati specifici per una RT
	 * @param versamentoExecutor
	 *            oggetto di supporto per eseguire il versamento della RT
	 */
	private void processResultSetRicevute(EnteDto ente, ResultSet rsListaRicevute,
			RtMetadataGenerator metadataGenerator, RtVersamentoExecutor versamentoExecutor) {
		// loop sulla lista delle ricevute
		try {
			while (rsListaRicevute.next()) {
				// inizializza un DTO con i dati sulla ricevuta
				RicevutaTelematicaDto ricevutaDto = extractDataFromResultSet(rsListaRicevute);
				if (ricevutaDto == null) {
					log.error("impossibile creare il dto per una ricevuta telematica - nessun dato disponibile da inviare");
					riepilogoElaborazione.ricevuteNonElaborate++;
					continue;
				}
				//inizio LP PG22XX06 - 20220701
				if(ricevutaDto.getCRPTRT() == null || ricevutaDto.getCRPTRT().trim().length() == 0) {
					//inizio LP PG22XX06 - 20220705
					//log.error("nella dto non e' presente una ricevuta telematica - nessun dato disponibile da inviare");
					//fine LP PG22XX06 - 20220705
					riepilogoElaborazione.ricevuteNonElaborate++;
					continue;
				}
				//fine LP PG22XX06 - 20220701
				// ottiene un progressivo dal db
				Long sequenceNumber = getNextSequenceNumber();
				if (sequenceNumber == null) {
					log.error("impossibile ottenere il prossimo progressivo dal db - impossibile continuare con l'elaborazione della presente RT");
					riepilogoElaborazione.erroriInterni++;
					continue;
				}
				// crea i metadati per la ricevuta corrente
				VersamentoMetadata versamentoMetadata = null;
				try {
					versamentoMetadata = metadataGenerator.generateMetadata(ente, ricevutaDto, sequenceNumber);
				} catch (EnteProcessingException e) {
					String errorMessage = String
							.format(
									"impossibile inviare RT - errore nella generazione dei metadati per la ricevuta con id [%s]",
									ricevutaDto.getPRPTPKEY());
					log.error(errorMessage, e);
					riepilogoElaborazione.erroriInterni++;
					continue;
				}

				// invio effettivo
				VersamentoResult versamentoResult = versamentoExecutor.eseguiVersamento(versamentoMetadata);

				// aggiorna i contatori globali per l'ente corrente sulla base
				// dell'ultimo risultato di invio
				aggiornaContatoriPostVersamento(versamentoResult);

				// aggiorna lo stato della ricevuta nel db
				aggiornaStatoInvioRicevuta(ricevutaDto.getPRPTPKEY(), versamentoResult);
			}
		} catch (SQLException e) {
			/* lanciata da ResultSet.next() */
			log.error("errore nell'attraversamento dell'elenco delle RT");
		}
	}

	/**
	 * Estrae i dati di una RT dal {@link ResultSet} specificato
	 * 
	 * @param rs
	 *            il {@link ResultSet} da cui estrarre i dati
	 * @return un {@link RicevutaTelematicaDto} con i dati sulla RT da trattare,
	 *         oppure <code>null</code> se l'estrazione dei dati fallisce.
	 */
	private RicevutaTelematicaDto extractDataFromResultSet(ResultSet rs) {
		try {
			String CRPTRT = rs.getString(1);
			Long PRPTPKEY = rs.getLong(2);
			RicevutaTelematicaDto dto = new RicevutaTelematicaDto();
			dto.setCRPTRT(CRPTRT);
			dto.setPRPTPKEY(PRPTPKEY);
			return dto;
		} catch (SQLException e) {
			log.warn("getString() o  getLong() nel resultset hanno generato un'eccezione", e);
			return null;
		}
	}

	/**
	 * Ottiene un identificativo univoco dal db per associare una certa RT ad
	 * una operazione di versamento.
	 * <p>
	 * In caso di errore viene restituito <code>null</code>.
	 * </p>
	 * 
	 * @return un {@link Long} con il progressivo, oppure <code>null</code>.
	 * @throws com.seda.payer.rtbatch.invio.EnteProcessingException 
	 * @throws DaoException 
	 */
	private Long getNextSequenceNumber() {
		//inizio LP 20241004 - PGNTBRTB-1
		CallableStatement pyrsqsp_nxt_batch = null;
		//fine LP 20241004 - PGNTBRTB-1
		
		try {
			//inizio LP 20241004 - PGNTBRTB-1
			if(rtRepositoryDAO == null) {
				throw new EnteProcessingException("RtRepositoryDAO == null");
			}
			pyrsqsp_nxt_batch = rtRepositoryDAO.getPykeysp_bigint();
			//fine LP 20241004 - PGNTBRTB-1
			
			pyrsqsp_nxt_batch.setString(1, "RSQ");
			pyrsqsp_nxt_batch.registerOutParameter(2, Types.BIGINT);
			pyrsqsp_nxt_batch.executeQuery();
			return pyrsqsp_nxt_batch.getLong(2);
		//inizio LP 20241004 - PGNTBRTB-1
		//} catch (SQLException e) {
		} catch (Exception e) {
		//fine LP 20241004 - PGNTBRTB-1
			log.error("errore nell'ottenimento del progressivo per l'identificazione dell'invio", e);
			return null;
		//inizio LP 20241004 - PGNTBRTB-1
		} finally {
			DAOHelper.closeIgnoringException(pyrsqsp_nxt_batch);
		//fine LP 20241004 - PGNTBRTB-1
		}
	}

	/**
	 * Recupera le informazioni generali dal riepilogo dell'elaborazione
	 * dell'ente e le invia all'email specifica dell'ente.
	 * 
	 * @param enteEmailAddress
	 *            indirizzo email di destinazione
	 */
	private void inviaEmailRiepilogativa(String enteEmailAddress) {
		StringBuilder sbEmailContent = new StringBuilder();
		sbEmailContent.append("Resoconto invio ricevute iniziato in data ");
		sbEmailContent.append(SimpleDateFormat.getDateTimeInstance().format(startTime));
		sbEmailContent.append("\n");
		sbEmailContent.append(String.format("Totale ricevute inviate : %d%n", riepilogoElaborazione.totaleRtElaborate));
		sbEmailContent.append(String.format("Ricevute inviate correttamente : %d%n", riepilogoElaborazione.inviiCorretti));
		sbEmailContent.append(String.format("Ricevute con invio erroneo : %d%n", riepilogoElaborazione.inviiConErrore));

		if (!Main.isDryRun()) {
			try {
				MailClient mailClient = new MailClient(ENV_INVIO_CFG_FILE_LOCATION, LOGGER_CATEGORY_INVIO);
				mailClient.sendMail(enteEmailAddress, "Resoconto di invio ricevute telematiche", sbEmailContent.toString());
			} catch (Exception e) {
				String errorMessage = String.format("Errore nel processo di invio email all'ente <%s>", enteEmailAddress);
				log.error(errorMessage, e);
			}
		}
	}

	private void closeRs(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				// ignorata
			}
		}
	}

//inizio LP 20241004 - PGNTBRTB-1
//	void shutdown() {
//		//closeJdbcStatement(pycftsp_lstrt_batch);
//		//closeJdbcStatement(pycftsp_upd_batch);
//		//closeJdbcStatement(pyrsqsp_nxt_batch);
//	}
//
//	private void closeJdbcStatement(Statement stmt) {
//		if (stmt != null) {
//			try {
//				stmt.close();
//			} catch (SQLException e) {
//				log.warn("eccezione lanciata in chiusura dei JdbcStatement", e);
//			} catch (Exception e) {
//				log.warn("eccezione lanciata in chiusura dei JdbcStatement", e);
//			}
//		}
//	}
//fine LP 20241004 - PGNTBRTB-1

}
