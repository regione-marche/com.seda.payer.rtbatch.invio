package com.seda.payer.rtbatch.invio;


class RiepilogoElaborazioneInvio {

	/**
	 * numero di RT lette correttamente dal db e per le quali è stato possibile
	 * creare i metadati per l'invio
	 */
	int totaleRtElaborate = 0;

	/**
	 * numero di RT elaborate e versate correttamente
	 */
	int inviiCorretti = 0;

	/**
	 * numero di RT elaborate e che hanno generato un errore in fase di
	 * versamento
	 */
	int inviiConErrore = 0;

	/**
	 * numero di RT per le quali non è stato possibile estrarre correttamente
	 * i dati dal db
	 */
	int ricevuteNonElaborate = 0;

	/**
	 * viene incrementato quando
	 * <ul>
	 * <li>non è possibile aggiornare lo stato di una ricevuta dopo il versamento
	 * (quale che ne sia l'esito)</li>
	 * <li>non si riesce ad ottenere un progressivo dal db</li>
	 * <li>fallice la crezione dei metadati per una RT</li>
	 * </ul>
	 */
	int erroriInterni = 0;

	/**
	 * viene incrementato quando non è possibile aggiornare lo stato di una
	 * ricevuta dopo il versamento (quale che ne sia l'esito)
	 */
	int erroriAggiornamentoStatoRT = 0;

}
