package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;


/**
 * The Interface CompletePipelineService.
 */
public interface CompletePipelineService {
	
	/**
	 * Calculate metrics and perform analysis if needed.
	 *
	 * @param calculateMetricsInputDTO the calculate metrics input DTO
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws OWLOntologyCreationException 
	 */
	File calculateMetricsAndPerformAnalysisIfNeeded(CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException, OWLOntologyCreationException;

	/**
	 * Calculate metrics and perform analysis if needed, and send results by email.
	 *
	 * @param calculateMetricsInputDTO the calculate metrics input DTO
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws OWLOntologyCreationException 
	 */
	void calculateMetricsAndPerformAnalysisIfNeededEmail(CalculateMetricsInputDTO calculateMetricsInputDTO)
			throws IOException, InterruptedException, OWLOntologyCreationException;
}
