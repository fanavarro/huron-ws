package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import es.um.dis.tecnomod.huron.main.Config;
import es.um.dis.tecnomod.huron.metrics.Metric;
import es.um.dis.tecnomod.huron.result_model.ResultModelInterface;
import es.um.dis.tecnomod.huron.services.OntologyUtils;
import es.um.dis.tecnomod.huron.tasks.MetricCalculationTask;
import es.um.dis.tecnomod.huron.tasks.MetricCalculationTaskResult;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.output.MetricDescriptionDTO;
import es.um.dis.tecnomod.huron.ws.dto.output.MetricDescriptionListDTO;
import es.um.dis.tecnomod.oquo.utils.Namespaces;

/**
 * The Interface CalculateMetricsService.
 */
public abstract class CalculateMetricsService {
	private final static Logger LOGGER = Logger.getLogger(CalculateMetricsService.class.getName());
	
	private static final String METRICS_JAVA_PACKAGE = "es.um.dis.tecnomod.huron.metrics";
	private static final String OQUO_HURON_IRI = "https://purl.org/oquo-huron";
	private static final String IAO_DEFINITION = "http://purl.obolibrary.org/obo/IAO_0000115";
	private static final String CROP_ACRONYM = "http://www.cropontology.org/rdf/acronym";
	
	/**
	 * Calculate metrics.
	 *
	 * @param input the input
	 * @return the file
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OWLOntologyCreationException 
	 */
	public abstract File calculateMetrics(CalculateMetricsInputDTO input) throws IOException, InterruptedException, OWLOntologyCreationException;

	/**
	 * Get a list of metric available to be calculated.
	 * 
	 * @return A list of metrics
	 */
	public MetricDescriptionListDTO getAvailableMetrics() throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology oquo = ontologyManager.loadOntology(IRI.create(OQUO_HURON_IRI));
		MetricDescriptionListDTO metricDescriptionList = new MetricDescriptionListDTO();
		Set<Class <? extends Object>> metricClasses = findAllClassesUsingClassLoader(METRICS_JAVA_PACKAGE);
		for(Class <? extends Object> c : metricClasses) {
			if (Modifier.isAbstract(c.getModifiers())) {
				continue;
			}
			String metricIRI = Namespaces.OQUO_NS + c.getSimpleName();
			if (oquo.containsClassInSignature(IRI.create(metricIRI))) {
				metricDescriptionList.getMetricDescriptionList().add(this.createMetricDescriptionDTO(metricIRI, oquo));
			}
		}
		return metricDescriptionList;
	}

	private MetricDescriptionDTO createMetricDescriptionDTO(String metricIRI, OWLOntology oquo) {
		String name = "";
		List<String> labels = OntologyUtils.getAnnotationValues(IRI.create(metricIRI), IRI.create(RDFS.LABEL.stringValue()), oquo, false);
		if (!labels.isEmpty()) {
			name = labels.get(0);
		}
		
		String acronym = "";
		List<String> acronyms = OntologyUtils.getAnnotationValues(IRI.create(metricIRI), IRI.create(CROP_ACRONYM), oquo, false);
		if (!acronyms.isEmpty()) {
			acronym = acronyms.get(0);
		}
		
		String longDescription = "";
		List<String> longDescriptions = OntologyUtils.getAnnotationValues(IRI.create(metricIRI), IRI.create(IAO_DEFINITION), oquo, false);
		if (!longDescriptions.isEmpty()) {
			longDescription = longDescriptions.get(0);
		}
		
		String shortDescription = "";
		List<String> shortDescriptions = OntologyUtils.getAnnotationValues(IRI.create(metricIRI), IRI.create(RDFS.COMMENT.stringValue()), oquo, false);
		if (!shortDescriptions.isEmpty()) {
			shortDescription = shortDescriptions.get(0);
		}
		
		MetricDescriptionDTO metricDescriptionDTO = new MetricDescriptionDTO(metricIRI, name, acronym, shortDescription, longDescription);
		return metricDescriptionDTO;
	}

	protected List<Metric> getMetricsToApply(List<String> metricIRIs, Config config) throws OWLOntologyCreationException {
		List<Metric> metrics = new ArrayList<>();
		if (metricIRIs != null && !metricIRIs.isEmpty()) {
			for (String metricIRI : metricIRIs) {
				Metric metric = this.getMetricByIRI(IRI.create(metricIRI));
				if (metric != null) {
					metric.setConfig(config);
					metrics.add(metric);
				}
			}
		} else {
			metrics = this.getAllMetrics(config);
		}
		return metrics;
	}

	protected List<Metric> getAllMetrics(Config config) throws OWLOntologyCreationException {
		List<Metric> metrics = new ArrayList<>();
		MetricDescriptionListDTO availableMetrics = this.getAvailableMetrics();
		for (MetricDescriptionDTO availableMetricDTO : availableMetrics.getMetricDescriptionList()) {
			Metric metric = this.getMetricByIRI(IRI.create(availableMetricDTO.getIri()));
			if (metric != null) {
				metric.setConfig(config);
				metrics.add(metric);
			}
		}
		return metrics;
	}

	protected Metric getMetricByIRI (IRI metricIRI) {
		String metricFragment = metricIRI.getFragment();
		Class <? extends Object> metricClass = this.getClass(metricFragment + ".class", METRICS_JAVA_PACKAGE);
		try {
			if (metricClass == null) {
				LOGGER.log(Level.SEVERE, String.format("Error creating metric instance from IRI %s: Metric class not found.", metricIRI));
				return null;
			}
			return (Metric) metricClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, "Error creating metric instance", e);
			return null;
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, "Error creating metric instance", e);
			return null;
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
	protected void executeWithTaskExecutor(File outputFile, List<MetricCalculationTask> tasks, Config config, int threads)
			throws InterruptedException, IOException {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		List<Future<List<MetricCalculationTaskResult>>> futureResults = executor.invokeAll(tasks);
		for (Future<List<MetricCalculationTaskResult>> futureResult : futureResults) {
			try {
				futureResult.get();
			} catch (ExecutionException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);
		
		/* Write results to files */
		for(ResultModelInterface outputModel : config.getResultModels()) {
			outputModel.export();
		}
	}
	
	private Set<Class <? extends Object>> findAllClassesUsingClassLoader(String packageName) {
		Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
		return reflections.getSubTypesOf(Object.class);
	}

	private Class<? extends Object> getClass(String className, String packageName) {
		try {
			return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			// handle the exception
		}
		return null;
	}
}
