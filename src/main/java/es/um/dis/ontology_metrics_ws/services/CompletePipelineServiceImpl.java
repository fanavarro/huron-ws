package es.um.dis.ontology_metrics_ws.services;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import es.um.dis.ontology_metrics_ws.dto.input.CalculateMetricsInputDTO;

@Service
public class CompletePipelineServiceImpl implements CompletePipelineService {
	private final static Logger LOGGER = Logger.getLogger(CompletePipelineServiceImpl.class.getName());
	
	@Autowired
	private CalculateMetricsService calculateMetricsService;
	
	@Autowired
	private AnalysisService analysisService;
	
	@Autowired
	private ZipService zipService;
	
	@Autowired
	private MailService mailService;
	
	@Override
	public File calculateMetricsAndPerformAnalysisIfNeeded(CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException {
		File zipFile = null;
    	File metricsFile = calculateMetricsService.calculateMetrics(calculateMetricsInputDTO);
    	String workingDir = metricsFile.getParentFile().getAbsolutePath();
    	if (calculateMetricsInputDTO.isPerformAnalysis()) {
    		try {
    			File analysisFolder = analysisService.performAnalysis(metricsFile);
    			zipFile = zipService.generateZipFile(workingDir, metricsFile, analysisFolder);
    		} catch (ExecutionException e) {
    			LOGGER.log(Level.SEVERE, "Error performing analysis", e);
    			/* If there is an error permorming the analysis, do not include it in the final zip. */
    			zipFile = zipService.generateZipFile(workingDir, metricsFile);
    		}
    	} else {
    		zipFile = zipService.generateZipFile(workingDir, metricsFile);
    	}
		return zipFile;
	}
	
	@Override
	@Async
	public void calculateMetricsAndPerformAnalysisIfNeededEmail(CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException {
		File zipFile = this.calculateMetricsAndPerformAnalysisIfNeeded(calculateMetricsInputDTO);
		String to = calculateMetricsInputDTO.getEmail();
    	String subject = "Results of ontology metrics";
    	String body = "Dear user, the results of your request can be found in the attached zip file.";
    	mailService.send(to, subject, body, zipFile);
	}

}
