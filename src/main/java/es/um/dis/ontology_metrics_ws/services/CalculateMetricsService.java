package es.um.dis.ontology_metrics_ws.services;

import java.io.File;
import java.io.IOException;

import es.um.dis.ontology_metrics_ws.dto.input.CalculateMetricsInputDTO;


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
}
