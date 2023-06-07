package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.um.dis.tecnomod.huron.tasks.MetricCalculationTask;
import es.um.dis.tecnomod.huron.tasks.MetricCalculationTaskResult;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.input.OntologyInputDTO;

@Service("calculateMetricsService")
public class CalculateMetricsServiceImpl extends CalculateMetricsService {

	private final static Logger LOGGER = Logger.getLogger(CalculateMetricsServiceImpl.class.getName());

	private static final int THREADS = 5;

	@Autowired
	private DownloadService downloadService;

	@Override
	public File calculateMetrics(CalculateMetricsInputDTO input) throws IOException, InterruptedException, OWLOntologyCreationException {
		String email = input.getEmail();
		Path workingPath = Files.createTempDirectory(email);
		File outputFile = new File(workingPath.toFile(), "metrics.tsv");
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
		File detailedFilesFolder = Paths.get(workingPath.toAbsolutePath().toString(), "detailed_files").toFile();
		for (OntologyInputDTO ontologyDTO : input.getOntologies()) {
			String owlFileName = String.format("%s.owl", ontologyDTO.getName());
			File ontologyFile = downloadService.download(ontologyDTO.getIri(), workingPath, owlFileName);
			tasks.add(new MetricCalculationTask(this.getMetricsToApply(input.getMetrics()), ontologyFile, true,
					detailedFilesFolder));
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
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		List<Future<List<MetricCalculationTaskResult>>> futureResults = executor.invokeAll(tasks);
		FileWriter fileWriter = new FileWriter(outputFile);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.print("Ontology\tMetric\tValue\n");
		for (Future<List<MetricCalculationTaskResult>> futureResult : futureResults) {
			try {
				List<MetricCalculationTaskResult> results = futureResult.get();
				for (MetricCalculationTaskResult result : results) {
					printWriter.printf(Locale.ROOT, "%s\t%s\t%.3f\n", result.getOwlFile(), result.getMetricName(),
							result.getResult());
				}
			} catch (ExecutionException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);

		fileWriter.close();
		printWriter.close();
	}
}
