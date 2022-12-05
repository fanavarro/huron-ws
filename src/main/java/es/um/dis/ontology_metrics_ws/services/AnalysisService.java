package es.um.dis.ontology_metrics_ws.services;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface AnalysisService {
	/**
	 * Perform a clustering analysis by using evaluome
	 * @param metricsTSVFile Input file with the ontologies and their metrics.
	 * @return output folder with the generated plots
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	File performAnalysis(File metricsTSVFile) throws IOException, InterruptedException, ExecutionException;
}
