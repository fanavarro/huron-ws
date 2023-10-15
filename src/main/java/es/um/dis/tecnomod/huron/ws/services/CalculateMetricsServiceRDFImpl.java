package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.um.dis.tecnomod.huron.tasks.MetricCalculationTask;
import es.um.dis.tecnomod.huron.tasks.MetricCalculationTaskResult;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.input.OntologyInputDTO;

@Service("calculateMetricsServiceRDF")
public class CalculateMetricsServiceRDFImpl extends CalculateMetricsService {

	private final static Logger LOGGER = Logger.getLogger(CalculateMetricsServiceRDFImpl.class.getName());

	private static final int THREADS = 5;

	@Autowired
	private DownloadService downloadService;

	@Override
	public File calculateMetrics(CalculateMetricsInputDTO input) throws IOException, InterruptedException, OWLOntologyCreationException {
		String email = input.getEmail();
		Path workingPath = Files.createTempDirectory(email);
		File outputFile = new File(workingPath.toFile(), "metrics.ttl");
		LOGGER.log(Level.INFO, "Calculating tasks");
		List<MetricCalculationTask> tasks = this.getMetricCalculationTasks(input, workingPath);
		LOGGER.log(Level.INFO, String.format("%d tasks to perform", tasks.size()));
		this.executeWithTaskExecutor(outputFile, tasks, THREADS);
		LOGGER.log(Level.INFO, "Tasks finished");
		return outputFile;
	}

	private List<MetricCalculationTask> getMetricCalculationTasks(CalculateMetricsInputDTO input, Path workingPath)
			throws MalformedURLException, IOException, OWLOntologyCreationException {
		List<MetricCalculationTask> tasks = new ArrayList<>();
		for (OntologyInputDTO ontologyDTO : input.getOntologies()) {
			String owlFileName = String.format("%s.owl", ontologyDTO.getName());
			File ontologyFile = downloadService.download(ontologyDTO.getIri(), workingPath, owlFileName);
			tasks.add(new MetricCalculationTask(this.getMetricsToApply(input.getMetrics()), ontologyFile, false,
					null));
		}
		return tasks;
	}



	/**
	 * Execute with task executor.
	 *
	 * @param outputFile the output file
	 * @param tasks      the tasks
	 * @param threads    the threads
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException          Signals that an I/O exception has occurred.
	 */
	private void executeWithTaskExecutor(File outputFile, List<MetricCalculationTask> tasks, int threads)
			throws InterruptedException, IOException {
		Model rdfModel = ModelFactory.createDefaultModel();
		// TODO: Change to oquo purl when the main branch of oquo is merged with the dev one.
		rdfModel.add(rdfModel.createResource(), OWL.imports, rdfModel.createResource("https://raw.githubusercontent.com/tecnomod-um/oquo/individuals/ontology/oquo.owl"));
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		List<Future<List<MetricCalculationTaskResult>>> futureResults = executor.invokeAll(tasks);
		for (Future<List<MetricCalculationTaskResult>> futureResult : futureResults) {
			try {
				List<MetricCalculationTaskResult> results = futureResult.get();
				for (MetricCalculationTaskResult result : results) {
					rdfModel.add(result.getRdf());
				}
			} catch (ExecutionException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);
		rdfModel.write(new FileOutputStream(outputFile), "N-TRIPLES");
	}

	
}
