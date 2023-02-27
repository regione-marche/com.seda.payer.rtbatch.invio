package com.seda.payer.rtbatch.invio;

import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_INVIO;

import com.seda.payer.rtbatch.base.commons.EnteDto;
import com.seda.payer.rtbatch.base.datalayer.DaoException;
import com.seda.payer.rtbatch.base.datalayer.RtRepositoryDao;
import org.apache.log4j.Logger;

import java.util.List;

class BatchExecutor {

	private static final Logger log = Logger.getLogger(LOGGER_CATEGORY_INVIO);

	private String siglaProvincia;
	private RtRepositoryDao dao;

	BatchExecutor(String siglaProvincia, RtRepositoryDao dao) {
		this.siglaProvincia = siglaProvincia;
		this.dao = dao;
	}

	private List<EnteDto> getEntiPerProvincia(String siglaProvincia) throws BatchExecutionException {
		try {
			return dao.findEnti(siglaProvincia);
		} catch (DaoException e) {
			e.printStackTrace();
			String errorMessage = String.format("Errore nel recupero degli enti per la provincia [%s]", siglaProvincia);
			log.error(errorMessage, e);
			throw new BatchExecutionException(errorMessage, e);
		}
	}

	void processProvincia() throws BatchExecutionException {
		List<EnteDto> listaEnti = getEntiPerProvincia(siglaProvincia);
		if (listaEnti.size() == 0) {
			log.warn("Nessun ente per la provincia specificata");
		}
		BatchExecutorEnte executorEnte;
		for (EnteDto ente : listaEnti) {
			executorEnte = new BatchExecutorEnte();
			try {
				executorEnte.processEnte(ente);
			} finally {
				executorEnte.shutdown();
			}
		}
	}
}
