package es.um.dis.tecnomod.huron.ws.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import es.um.dis.tecnomod.huron.metrics.Metric;
import es.um.dis.tecnomod.huron.services.OntologyUtils;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.output.MetricDescriptionDTO;
import es.um.dis.tecnomod.huron.ws.dto.output.MetricDescriptionListDTO;

/**
 * The Interface CalculateMetricsService.
 */
public abstract class CalculateMetricsService {
	private final static Logger LOGGER = Logger.getLogger(CalculateMetricsService.class.getName());
	
	private static final String METRICS_JAVA_PACKAGE = "es.um.dis.tecnomod.huron.metrics";
	private static final String OQUO_IRI = "https://purl.archive.org/oquo";
	private static final String OQUO_NS = OQUO_IRI + "#";
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
		OWLOntology oquo = ontologyManager.loadOntology(IRI.create(OQUO_IRI));
		MetricDescriptionListDTO metricDescriptionList = new MetricDescriptionListDTO();
		Set<Class> metricClasses = findAllClassesUsingClassLoader(METRICS_JAVA_PACKAGE);
		for(Class c : metricClasses) {
			if (Modifier.isAbstract(c.getModifiers())) {
				continue;
			}
			String metricIRI = OQUO_NS + c.getSimpleName();
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
		
		String shortDescription = ""; // TODO: Include short descriptions in ontology.
		
		MetricDescriptionDTO metricDescriptionDTO = new MetricDescriptionDTO(metricIRI, name, acronym, shortDescription, longDescription);
		return metricDescriptionDTO;
	}

	protected List<Metric> getMetricsToApply(List<String> metricIRIs) throws OWLOntologyCreationException {
		List<Metric> metrics = new ArrayList<>();
		if (metricIRIs != null && !metricIRIs.isEmpty()) {
			for (String metricIRI : metricIRIs) {
				Metric metric = this.getMetricByIRI(IRI.create(metricIRI));
				if (metric != null) {
					metrics.add(metric);
				}
			}
		} else {
			metrics = this.getAllMetrics();
		}
		return metrics;
	}

	protected List<Metric> getAllMetrics() throws OWLOntologyCreationException {
		List<Metric> metrics = new ArrayList<>();
		MetricDescriptionListDTO availableMetrics = this.getAvailableMetrics();
		for (MetricDescriptionDTO availableMetricDTO : availableMetrics.getMetricDescriptionList()) {
			Metric metric = this.getMetricByIRI(IRI.create(availableMetricDTO.getIri()));
			if (metric != null) {
				metrics.add(metric);
			}
		}
		return metrics;
	}

	protected Metric getMetricByIRI (IRI metricIRI) {
		String metricFragment = metricIRI.getFragment();
		Class metricClass = this.getClass(metricFragment + ".class", METRICS_JAVA_PACKAGE);
		try {
			return (Metric) metricClass.newInstance();
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, "Error creating metric instance", e);
			return null;
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, "Error creating metric instance", e);
			return null;
		}
	}

	private Set<Class> findAllClassesUsingClassLoader(String packageName) {
		InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		return reader.lines().filter(line -> line.endsWith(".class")).map(line -> getClass(line, packageName))
				.collect(Collectors.toSet());
	}

	private Class getClass(String className, String packageName) {
		try {
			return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			// handle the exception
		}
		return null;
	}
}
