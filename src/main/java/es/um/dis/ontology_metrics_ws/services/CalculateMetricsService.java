package es.um.dis.ontology_metrics_ws.services;

import java.io.File;
import java.io.IOException;

import es.um.dis.ontology_metrics_ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.ontology_metrics_ws.dto.output.MetricDescriptionListDTO;


/**
 * The Interface CalculateMetricsService.
 */
public interface CalculateMetricsService {
	
	/**
	 * Calculate metrics.
	 *
	 * @param input the input
	 * @return the file
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	File calculateMetrics(CalculateMetricsInputDTO input) throws IOException, InterruptedException;
	
	/**
	 * Get a list of metric available to be calculated.
	 * @return A list of metrics
	 */
	MetricDescriptionListDTO getAvailableMetrics();
}
