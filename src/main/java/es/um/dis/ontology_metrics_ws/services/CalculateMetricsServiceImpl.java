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
import metrics.NumberOfLexicalRegularitiesMetric;
import metrics.NumberOfLexicalRegularityClassesMetric;
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
		} else if ("Synonyms per class".equalsIgnoreCase(name)) {
			return new SynonymsPerClassMetric();
		} else if ("Descriptions per class".equalsIgnoreCase(name)) {
			return new DescriptionsPerClassMetric();
		} else if ("Names per object property".equalsIgnoreCase(name)) {
			return new NamesPerObjectPropertyMetric();
		} else if ("Synonyms per object property".equalsIgnoreCase(name)) {
			return new SynonymsPerObjectPropertyMetric();
		} else if ("Descriptions per object property".equalsIgnoreCase(name)) {
			return new DescriptionsPerObjectPropertyMetric();
		} else if ("Names per data property".equalsIgnoreCase(name)) {
			return new NamesPerDataPropertyMetric();
		} else if ("Synonyms per data property".equalsIgnoreCase(name)) {
			return new SynonymsPerDataPropertyMetric();
		} else if ("Descriptions per data property".equalsIgnoreCase(name)) {
			return new DescriptionsPerDataPropertyMetric();
		} else if ("Names per annotation property".equalsIgnoreCase(name)) {
			return new NamesPerAnnotationPropertyMetric();
		} else if ("Synonyms per annotation property".equalsIgnoreCase(name)) {
			return new SynonymsPerAnnotationPropertyMetric();
		} else if ("Descriptions per annotation property".equalsIgnoreCase(name)) {
			return new DescriptionsPerAnnotationPropertyMetric();
		} else if ("Lexically suggest logically define".equalsIgnoreCase(name)) {
			return new LexicallySuggestLogicallyDefineMetric();
		} else if ("LSLD".equalsIgnoreCase(name)) {
			return new LexicallySuggestLogicallyDefineMetric();
		} else if ("Systematic naming".equalsIgnoreCase(name)) {
			return new SystematicNamingMetric();
		} else if ("Number of lexical regularities".equalsIgnoreCase(name)) {
			return new NumberOfLexicalRegularitiesMetric();
		} else if ("Number of lexical regularity classes".equalsIgnoreCase(name)) {
			return new NumberOfLexicalRegularityClassesMetric();
		} else if ("Number of LRs".equalsIgnoreCase(name)) {
			return new NumberOfLexicalRegularitiesMetric();
		} else if ("Number of LR classes".equalsIgnoreCase(name)) {
			return new NumberOfLexicalRegularityClassesMetric();
		} else {
			throw new IllegalArgumentException(String.format("Metric '%s%' not found.", name));
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
				"The total number of class names over the total number of classes.",
				"This metric accounts for the number of names associated with classes, and uses the list of annotation properties used by the community for names (rdfs:label, skos:prefLabel, foaf:name, etc.). Then, this metric is calculated as the number of names associated with ontology classes divided by the total number of classes in the ontology. The range of the value of this metric is the set of real positive numbers. Values lower than one mean that there are classes without any name in the ontology. Contrariwise, a value greater than 1 indicates that there are classes with multiple names; possibly caused by the inclusion of multilingual names or by some design decision."));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per class",
				"The total number of class synonyms over the total number of classes.",
				"This metric accounts for the number of synonyms associated with classes, which can also be provided by using different annotation properties used by the community to include synonyms (oboInOwl:hasExactSynonym, skos:altLabel, iao:0000118, etc.). This metric is calculated as the number of synonyms associated with classes divided by the total number of classes in the ontology. The range of the value of this metric is the set of real positive numbers."));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per class",
				"The total number of class descriptions over the total number of classes.",
				"This metric accounts for the number of descriptions associated with classes, which can also be provided by using different annotation properties used by the community to include descriptions (rdfs:comment, skos:definition, dcterms:description, etc.). This metric is calculated as the total number of descriptions associated with ontology classes divided by the total number of classes in the ontology. The range of the value of this metric is the set of real positive numbers."));

		availableMetrics.add(new MetricDescriptionDTO("Names per object property",
				"The total number of object property names over the total number of object properties.",
				"This metric accounts for the number of names associated with object properties, and uses the list of annotation properties used by the community for names (rdfs:label, skos:prefLabel, foaf:name, etc.). Then, this metric is calculated as the number of names associated with ontology object properties divided by the total number of object properties in the ontology. The range of the value of this metric is the set of real positive numbers. Values lower than one mean that there are object properties without any name in the ontology. Contrariwise, a value greater than 1 indicates that there are object properties with multiple names; possibly caused by the inclusion of multilingual names or by some design decision."));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per object property",
				"The total number of object property synonyms over the total number of object properties.",
				"This metric accounts for the number of synonyms associated with object properties, which can also be provided by using different annotation properties used by the community to include synonyms (oboInOwl:hasExactSynonym, skos:altLabel, iao:0000118, etc.). This metric is calculated as the number of synonyms associated with object properties divided by the total number of object properties in the ontology. The range of the value of this metric is the set of real positive numbers."));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per object property",
				"The total number of object property descriptions over the total number of object properties.",
				"This metric accounts for the number of descriptions associated with object properties, which can also be provided by using different annotation properties used by the community to include descriptions (rdfs:comment, skos:definition, dcterms:description, etc.). This metric is calculated as the total number of descriptions associated with ontology object properties divided by the total number of object properties in the ontology. The range of the value of this metric is the set of real positive numbers."));

		availableMetrics.add(new MetricDescriptionDTO("Names per data property",
				"The total number of data property names over the total number of data properties.",
				"This metric accounts for the number of names associated with data type properties, and uses the list of annotation properties used by the community for names (rdfs:label, skos:prefLabel, foaf:name, etc.). Then, this metric is calculated as the number of names associated with ontology data type properties divided by the total number of data type properties in the ontology. The range of the value of this metric is the set of real positive numbers. Values lower than one mean that there are data type properties without any name in the ontology. Contrariwise, a value greater than 1 indicates that there are data type properties with multiple names; possibly caused by the inclusion of multilingual names or by some design decision."));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per data property",
				"The total number of data property synonyms over the total number of data properties.",
				"This metric accounts for the number of synonyms associated with data type properties, which can also be provided by using different annotation properties used by the community to include synonyms (oboInOwl:hasExactSynonym, skos:altLabel, iao:0000118, etc.). This metric is calculated as the number of synonyms associated with data type properties divided by the total number of data type properties in the ontology. The range of the value of this metric is the set of real positive numbers."));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per data property",
				"The total number of data property descriptions over the total number of data properties.",
				"This metric accounts for the number of descriptions associated with data type properties, which can also be provided by using different annotation properties used by the community to include descriptions (rdfs:comment, skos:definition, dcterms:description, etc.). This metric is calculated as the total number of descriptions associated with ontology data type properties divided by the total number of data type properties in the ontology. The range of the value of this metric is the set of real positive numbers."));

		availableMetrics.add(new MetricDescriptionDTO("Names per annotation property",
				"The total number of annotation property names over the total number of annotation properties.",
				"This metric accounts for the number of names associated with annotation properties, and uses the list of annotation properties used by the community for names (rdfs:label, skos:prefLabel, foaf:name, etc.). Then, this metric is calculated as the number of names associated with ontology annotation properties divided by the total number of annotation properties in the ontology. The range of the value of this metric is the set of real positive numbers. Values lower than one mean that there are annotation properties without any name in the ontology. Contrariwise, a value greater than 1 indicates that there are annotation properties with multiple names; possibly caused by the inclusion of multilingual names or by some design decision."));
		availableMetrics.add(new MetricDescriptionDTO("Synonyms per annotation property",
				"The total number of annotation property synonyms over the total number of annotation properties.",
				"This metric accounts for the number of synonyms associated with annotation properties, which can also be provided by using different annotation properties used by the community to include synonyms (oboInOwl:hasExactSynonym, skos:altLabel, iao:0000118, etc.). This metric is calculated as the number of synonyms associated with annotation properties divided by the total number of annotation properties in the ontology. The range of the value of this metric is the set of real positive numbers."));
		availableMetrics.add(new MetricDescriptionDTO("Descriptions per annotation property",
				"The total number of annotation property descriptions over the total number of annotation properties.",
				"This metric accounts for the number of descriptions associated with annotation properties, which can also be provided by using different annotation properties used by the community to include descriptions (rdfs:comment, skos:definition, dcterms:description, etc.). This metric is calculated as the total number of descriptions associated with ontology annotation properties divided by the total number of annotation properties in the ontology. The range of the value of this metric is the set of real positive numbers."));

		availableMetrics.add(new MetricDescriptionDTO("Number of lexical regularities",
				"The number of lexical regularities found in the ontology",
				"This metric examines the names of the ontology classes, stabished by using 'rdfs:label', and identifies the number of lexical regularities appearing in the ontology."));
		availableMetrics.add(new MetricDescriptionDTO("Number of lexical regularity classes",
				"The number of ontology classes whose name is a lexical regularity.",
				"This metric returns the number of ontology classes whose complete name, set by using 'rdfs:label', is a lexical regularity."));
		availableMetrics.add(new MetricDescriptionDTO("Systematic naming",
				"The degree in which hierarchies under LR classes exhibit the lexical regularity of the parent class.",
				"This metric is related to the ontology design principle that states that classes in the same taxonomy should share part of their name, since subclasses are specializations of the parent class. In other words, class names should follow a genus-differentia style. This metric is calculated as the ratio of subclasses of an LR class that exhibit the lexical regularity of the parent class. This requires to calculate how many subclasses of a given LR class exhibit the lexical regularity in their name (positive cases) and how many do not (negative cases). The value of the metric is calculated by dividing the positive cases by the total number of cases. It is calculated for each LR class. The final value of the metric for an ontology is calculated as the sum of all the positive cases divided by the sum of all the positive and negative cases obtained by each LR class in the ontology. The value is in the range [0, 1], the highest values representing the best values for the metric."));
		availableMetrics.add(new MetricDescriptionDTO("Lexically suggest logically define",
				"The degree in which LR classes are semantically linked to the classes exhibiting the corresponding lexical regularity.",
				"This metric is related to the design principle of the same name, which we interpret as follows: what is expressed in natural language for humans should be expressed as logical axioms for the machine. It is calculated as the ratio in which an LR class is semantically related to other classes exhibiting its lexical regularity. Here, two classes are semantically related if there exist a path of an arbitrary length between them through the axioms in the ontology (i.e. subclass of, equivalency, or domain/range property). For computational reasons, we limited the length of the path between classes up to 5 steps; thus, classes semantically related between them through longer paths will be considered as not semantically related. In this case, positive cases are classes exhibiting a lexical regularity and that are semantically linked with the corresponding \\textit{LR class}. Negative cases are classes exhibiting a lexical regularity and that are not semantically linked with the corresponding LR class. The value of the metric is calculated by dividing the positive cases by the total number of cases. This metric is calculated for each LR class. The final value of the metric for an ontology is calculated as the sum of all the positive cases divided by the sum of all the positive and negative cases obtained by each LR class in the ontology. The value is in the range [0, 1], the highest values representing the best values for the metric."));

		metricDescriptionList.setMetricDescriptionList(availableMetrics);
		return metricDescriptionList;
	}
}
