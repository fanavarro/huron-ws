package es.um.dis.ontology_metrics_ws.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.um.dis.ontology_metrics_ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.ontology_metrics_ws.dto.input.OntologyInputDTO;
import es.um.dis.ontology_metrics_ws.dto.output.MetricDescriptionDTO;
import es.um.dis.ontology_metrics_ws.dto.output.MetricDescriptionListDTO;
import metrics.DescriptionsPerAnnotationPropertyMetric;
import metrics.DescriptionsPerClassMetric;
import metrics.DescriptionsPerDataPropertyMetric;
import metrics.DescriptionsPerObjectPropertyMetric;
import metrics.LexicallySuggestLogicallyDefineMetric;
import metrics.Metric;
import metrics.NamesPerAnnotationPropertyMetric;
import metrics.NamesPerClassMetric;
import metrics.NamesPerDataPropertyMetric;
import metrics.NamesPerObjectPropertyMetric;
import metrics.SynonymsPerAnnotationPropertyMetric;
import metrics.SynonymsPerClassMetric;
import metrics.SynonymsPerDataPropertyMetric;
import metrics.SynonymsPerObjectPropertyMetric;
import metrics.SystematicNamingMetric;
import tasks.MetricCalculationTask;
import tasks.MetricCalculationTaskResult;

@Service
public class CalculateMetricsServiceImpl implements CalculateMetricsService {

	private final static Logger LOGGER = Logger.getLogger(CalculateMetricsServiceImpl.class.getName());

	private static final int THREADS = 5;

	@Autowired
	private DownloadService downloadService;

	@Override
	public File calculateMetrics(CalculateMetricsInputDTO input) throws IOException, InterruptedException {
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
			throws MalformedURLException, IOException {
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

	private List<Metric> getMetricsToApply(List<String> metricNames) {
		List<Metric> metrics = new ArrayList<>();
		if (metricNames != null && !metricNames.isEmpty()) {
			for (String metricName : metricNames) {
				Metric metric = this.getMetricByName(metricName);
				if (metric != null) {
					metrics.add(metric);
				}
			}
		} else {
			metrics = this.getAllMetrics();
		}
		return metrics;
	}

	private List<Metric> getAllMetrics() {
		return Arrays.asList(new NamesPerClassMetric(), new SynonymsPerClassMetric(), new DescriptionsPerClassMetric(),
				new NamesPerObjectPropertyMetric(), new SynonymsPerObjectPropertyMetric(),
				new DescriptionsPerObjectPropertyMetric(), new NamesPerDataPropertyMetric(),
				new SynonymsPerDataPropertyMetric(), new DescriptionsPerDataPropertyMetric(),
				new NamesPerAnnotationPropertyMetric(), new SynonymsPerAnnotationPropertyMetric(),
				new DescriptionsPerAnnotationPropertyMetric(), new LexicallySuggestLogicallyDefineMetric(),
				new SystematicNamingMetric());
	}

	private Metric getMetricByName(String name) {
		if ("Names per class".equals(name)) {
			return new NamesPerClassMetric();
		} else if ("Synonyms per class".equals(name)) {
			return new SynonymsPerClassMetric();
		} else if ("Descriptions per class".equals(name)) {
			return new DescriptionsPerClassMetric();
		} else if ("Names per object property".equals(name)) {
			return new NamesPerObjectPropertyMetric();
		} else if ("Synonyms per object property".equals(name)) {
			return new SynonymsPerObjectPropertyMetric();
		} else if ("Descriptions per object property".equals(name)) {
			return new DescriptionsPerObjectPropertyMetric();
		} else if ("Names per data property".equals(name)) {
			return new NamesPerDataPropertyMetric();
		} else if ("Synonyms per data property".equals(name)) {
			return new SynonymsPerDataPropertyMetric();
		} else if ("Descriptions per data property".equals(name)) {
			return new DescriptionsPerDataPropertyMetric();
		} else if ("Names per annotation property".equals(name)) {
			return new NamesPerAnnotationPropertyMetric();
		} else if ("Synonyms per annotation property".equals(name)) {
			return new SynonymsPerAnnotationPropertyMetric();
		} else if ("Descriptions per annotation property".equals(name)) {
			return new DescriptionsPerAnnotationPropertyMetric();
		} else if ("Lexically suggest logically define".equals(name)) {
			return new LexicallySuggestLogicallyDefineMetric();
		} else if ("LSLD".equals(name)) {
			return new LexicallySuggestLogicallyDefineMetric();
		} else if ("Systematic naming".equals(name)) {
			return new SystematicNamingMetric();
		} else {
			return null; // TODO: Throw exception bad metric name.
		}
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
		// ExecutorService executor = Executors.newSingleThreadExecutor();
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

	@Override
	public MetricDescriptionListDTO getAvailableMetrics() {
		MetricDescriptionListDTO metricDescriptionList = new MetricDescriptionListDTO();
		List<MetricDescriptionDTO> availableMetrics = new ArrayList<MetricDescriptionDTO>();

		availableMetrics.add(new MetricDescriptionDTO("Names per class",
				"The total number of class names over the total number of classes.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per class",
				"The total number of class synonyms over the total number of classes.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per class",
				"The total number of class descriptions over the total number of classes.", ""));

		availableMetrics.add(new MetricDescriptionDTO("Names per object property",
				"The total number of object property names over the total number of object properties.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per object property",
				"The total number of object property synonyms over the total number of object properties.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per object property",
				"The total number of object property descriptions over the total number of object properties.", ""));

		availableMetrics.add(new MetricDescriptionDTO("Names per data property",
				"The total number of data property names over the total number of data properties.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per data property",
				"The total number of data property synonyms over the total number of data properties.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per data property",
				"The total number of data property descriptions over the total number of data properties.", ""));

		availableMetrics.add(new MetricDescriptionDTO("Names per annotation property",
				"The total number of annotation property names over the total number of annotation properties.", ""));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per annotation property",
				"The total number of annotation property synonyms over the total number of annotation properties.",
				""));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per annotation property",
				"The total number of annotation property descriptions over the total number of annotation properties.",
				""));

		availableMetrics.add(new MetricDescriptionDTO("Systematic naming",
				"The degree in which hierarchies under LR classes exhibit the lexical regularity of the parent class.",
				""));
		availableMetrics.add(new MetricDescriptionDTO("Lexically suggest logically define",
				"The degree in which LR classes are semantically linked to the classes exhibiting the corresponding lexical regularity.",
				""));
		
		metricDescriptionList.setMetricDescriptionList(availableMetrics);
		return metricDescriptionList;
	}
}
