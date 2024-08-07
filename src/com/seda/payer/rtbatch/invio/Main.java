package com.seda.payer.rtbatch.invio;

import static com.seda.payer.rtbatch.RtBatchEnvironment.LOGGER_CATEGORY_INVIO;
import static com.seda.payer.rtbatch.RtBatchEnvironment.ARG_DRY_RUN;
import static com.seda.payer.rtbatch.RtBatchEnvironment.ARG_SIGLA_PROVINCIA;
import static com.seda.payer.rtbatch.RtBatchEnvironment.ENV_INVIO_CFG_FILE_LOCATION;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.seda.payer.rtbatch.base.datalayer.DaoCreationException;
import com.seda.payer.rtbatch.base.datalayer.DaoFactory;
import com.seda.payer.rtbatch.base.datalayer.FactoryConfigurationException;
import com.seda.payer.rtbatch.base.datalayer.RtRepositoryDao;

public class Main {

	public static final Logger log = Logger.getLogger(LOGGER_CATEGORY_INVIO);

	private static Map<String, String> argumentsMap = new HashMap<String, String>();

	static {
		argumentsMap.put(ARG_SIGLA_PROVINCIA, "");
		argumentsMap.put(ARG_DRY_RUN, Boolean.FALSE.toString());
	}

	static boolean isDryRun() {
		return Boolean.valueOf(argumentsMap.get(ARG_DRY_RUN));
	}

	private static Properties generalProperties = new Properties();

	private static void fillArgumentsMap(String[] args) {
		int paramIndex = 0;
		try {
			while (paramIndex < args.length) {
				String commandLineParameter = args[paramIndex].split("=")[0];
				if (argumentsMap.containsKey(commandLineParameter)) {
					argumentsMap.put(commandLineParameter, args[paramIndex].split("=")[1]);
				}
				paramIndex++;
			}
			log.info(argumentsMap);
			log.info("dry run? " + isDryRun());
		} catch (ArrayIndexOutOfBoundsException e) {
			log.error("Sintassi della riga di comando errata", e);
			System.exit(1);
		} catch (Exception e) {
			log.error("Errore generale di analisi della riga di comando", e);
			System.exit(1);
		}
	}

	private static void checkProgramArguments() {
		if (argumentsMap.get(ARG_SIGLA_PROVINCIA).isEmpty()) {
			System.err.println("Sigla di provincia non specificata");
			log.error("Sigla di provincia non specificata");
			System.exit(2);
		}
		argumentsMap.put(ARG_SIGLA_PROVINCIA, argumentsMap.get(ARG_SIGLA_PROVINCIA).toUpperCase());
	}

	private static void readConfiguration() {
		String optionsFileLocation = System.getenv(ENV_INVIO_CFG_FILE_LOCATION);
		if (optionsFileLocation == null || optionsFileLocation.isEmpty()) {
			String errorMessage = "Impossibile determinare il file di configurazione. Deve esistere una variabile di ambiente denominata "
					+ ENV_INVIO_CFG_FILE_LOCATION + " contenente il percorso completo del file di configurazione.";
			System.err.println(errorMessage);
			log.error(errorMessage);
			log.error("file di configurazione non definito");
			System.exit(3);
		}

		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(optionsFileLocation);
			generalProperties.load(fileIn);
		} catch (FileNotFoundException e) {
			String errorMessage = String.format("il file di configurazione specificato <%s> non esiste", optionsFileLocation);
			System.err.println(errorMessage);
			e.printStackTrace();
			log.error(errorMessage, e);
			System.exit(3);
		} catch (IOException e) {
			String errorMessage = String.format("impossibile leggere il file di configurazione <%s>", optionsFileLocation);
			System.err.println(errorMessage);
			e.printStackTrace();
			log.error(errorMessage, e);
			System.exit(3);
		} finally {
			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log.debug(generalProperties);
		System.out.println(generalProperties);
	}

	private static void reconfigureLogging() {
		String log4jCfgLocation = generalProperties.getProperty("log4j.properties");
		Properties log4jProperties = new Properties();
		try {
			log4jProperties.load(new FileInputStream(log4jCfgLocation));
		} catch (IOException ex) {
			String errorMessage = String.format(
					"Reimpostazione sistema di log fallita. Errore nella lettura del file <%s>", log4jCfgLocation);
			System.err.println(errorMessage);
			log.warn(errorMessage, ex);
		}
		if (!log4jProperties.isEmpty()) {
			log.info("Reimpostazione sistema di logging");
			LogManager.resetConfiguration();
			PropertyConfigurator.configure(log4jProperties);
			log.info("Log4j Correttamente configurato");
		}
	}

	private static void configureDataLayer() {
		try {
			DaoFactory.configure(generalProperties);
		} catch (FactoryConfigurationException e) {
			System.err.println("Errore durante la configurazione dell'accesso ai dati");
			log.error("Errore durante la configurazione dell'accesso ai dati", e);
			log.error("configurazione usata: " + generalProperties);
			System.exit(6);
		} catch (Exception e) {
			System.err.println("Errore generico in configurazione del data layer: " + e);
			log.error("Errore generico in configurazione del data layer", e);
			System.exit(4);
		}
	}

	private static void executeBatch() {
		try {
			Security.insertProviderAt(new BouncyCastleProvider(), 1);
			log.info("Accesso ai dati configurato. Connessione in corso.");
			RtRepositoryDao dao = DaoFactory.getInstance().createDao();

			log.info("Inizio elaborazione per provincia [" + argumentsMap.get(ARG_SIGLA_PROVINCIA) + "]");
			BatchExecutor batchExecutor = new BatchExecutor(argumentsMap.get(ARG_SIGLA_PROVINCIA), dao);
			batchExecutor.processProvincia();
		} catch (DaoCreationException e) {
			System.err.println("Errore nella creazione dell'oggetto di accesso ai dati: " + e);
			log.error("Errore nella creazione dell'oggetto di accesso ai dati", e);
			System.exit(5);
		} catch (BatchExecutionException e) {
			log.error("Errore di esecuzione della procedura per la provincia " + argumentsMap.get(ARG_SIGLA_PROVINCIA), e);
		} catch (SecurityException e) {
			log.error("Errore durante l'impostazione del provider JCE", e);
			log.warn("I provider JCE attualmente in uso sono i seguenti:");
			for (Provider p : Security.getProviders()) {
				log.warn(p.getName() + " -- " + p.getProperty("Provider.id className"));
			}
		} finally {
			DaoFactory.getInstance().cleanUp();
		}
	}

	/**
	 * Esegue un test di connessione al servizio remoto
	 */
	@SuppressWarnings("unused")
	private static void testConnection(String targetUrl) {

		Security.insertProviderAt(new BouncyCastleProvider(), 1);

		/*
		 * l'url qui sotto è un url per fare i test messo a disposizione
		 * pubblicamente dagli sviluppatori di postman
		 */
		/*
		 * curl --location --request POST "https://postman-echo.com/post" \
		 * --data "This is expected to be sent back as part of response body."
		 */
		// l'oggetto per il verbo post
		// HttpPost httpPost = new HttpPost("https://postman-echo.com/post");
		HttpPost httpPost = new HttpPost(targetUrl);

		// entità di richiesta: multipart, dovendo contenere i parametri
		// testuali e la forma binaria della RT
		// ma in questo caso ci sono solo informazioni fuffa
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

	public static void main(String[] args) {
		BasicConfigurator.configure();
		/*
		 * TEST
		 */
		// testConnection("https://stage-poloconservazione.regione.marche.it");
		// System.exit(0);
		/*
		 * 
		 */
		fillArgumentsMap(args);
		checkProgramArguments();
		readConfiguration();
		reconfigureLogging();
		configureDataLayer();
		executeBatch();
	}

}
