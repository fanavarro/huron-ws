package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import es.um.dis.tecnomod.huron.common.OutputFileNames;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;

@Service
public class CompletePipelineServiceImpl implements CompletePipelineService {
	private final static Logger LOGGER = Logger.getLogger(CompletePipelineServiceImpl.class.getName());
	
	private final static String ADMIN_MAIL = "francisco.abad@um.es";
	
	@Autowired
	private CalculateMetricsService calculateMetricsService;
	
	@Autowired
	private AnalysisService analysisService;
	
	@Autowired
	private ZipService zipService;
	
	@Autowired
	private MailService mailService;
	
	@Override
	public File calculateMetricsAndPerformAnalysisIfNeeded(CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException, OWLOntologyCreationException {
		File zipFile = null;
    	File metricsFile = calculateMetricsService.calculateMetrics(calculateMetricsInputDTO);
    	String workingDir = metricsFile.getParentFile().getAbsolutePath();
    	if (calculateMetricsInputDTO.isPerformAnalysis()) {
    		try {
    			/* The long format file should have been created if corpus analysis was required. */
    			File metricsFileLongTable = new File(workingDir, OutputFileNames.LONG_TABLE_OUTPUT_FILE_NAME);
    			File analysisFolder = analysisService.performAnalysis(metricsFileLongTable);
    			zipFile = zipService.generateZipFile(workingDir, metricsFile, analysisFolder);
    		} catch (ExecutionException e) {
    			LOGGER.log(Level.SEVERE, "Error performing analysis", e);
    			/* If there is an error performing the analysis, do not include it in the final zip. */
    			zipFile = zipService.generateZipFile(workingDir, metricsFile);
    		}
    	} else {
    		zipFile = zipService.generateZipFile(workingDir, metricsFile);
    	}
		return zipFile;
	}
	
	@Override
	@Async
	public void calculateMetricsAndPerformAnalysisIfNeededEmail(CalculateMetricsInputDTO calculateMetricsInputDTO) throws OWLOntologyCreationException {
		File zipFile;
		String to = calculateMetricsInputDTO.getEmail();
    	String subject = "Results of ontology metrics";
    	String body = "";
		try {
			zipFile = this.calculateMetricsAndPerformAnalysisIfNeeded(calculateMetricsInputDTO);
	    	body = "Dear user, the results of your request can be found in the attached zip file.";
	    	mailService.send(to, subject, body, zipFile);
		} catch (IOException | InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Error performing analysis", e);
			/* Send error mail to user */
			body = "Your request could not be processed due to an internal error. This will be reviewed by the developers to find a fix.";
			mailService.send(to, subject, body);
			
			/* Send log file to admin */
			String exceptionStackTrace = ExceptionUtils.getStackTrace(e);
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			
			try {
				String requestJSON = ow.writeValueAsString(calculateMetricsInputDTO);
				mailService.send(ADMIN_MAIL, "Error in ontology-metrics-ws", String.format("Request: %s\n\n\n Exception: %s", requestJSON, exceptionStackTrace));
			} catch (JsonProcessingException e1) {
				mailService.send(ADMIN_MAIL, "Error in ontology-metrics-ws", String.format("Request: %s\n\n\n Exception: %s", calculateMetricsInputDTO, exceptionStackTrace));
			}
			
		}
		
	}

}
