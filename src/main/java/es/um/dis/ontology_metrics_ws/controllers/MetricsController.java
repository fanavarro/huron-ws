package es.um.dis.ontology_metrics_ws.controllers;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.um.dis.ontology_metrics_ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.ontology_metrics_ws.dto.output.MetricDescriptionListDTO;
import es.um.dis.ontology_metrics_ws.services.AnalysisService;
import es.um.dis.ontology_metrics_ws.services.CalculateMetricsService;
import es.um.dis.ontology_metrics_ws.services.ZipService;

/**
 *
 * A sample greetings controller to return greeting text
 */
@RestController
public class MetricsController {
	private final static Logger LOGGER = Logger.getLogger(MetricsController.class.getName());
	
	@Autowired
	private CalculateMetricsService calculateMetricsService;
	
	@Autowired
	private AnalysisService analysisService;
	
	@Autowired
	private ZipService zipService;
    
	@RequestMapping(value="/hello/{name}", method = RequestMethod.GET)
	public String hello(@PathVariable String name) {
		return "Hello " + name + "!!";
	}
	
	@RequestMapping(value="/getAvailableMetrics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public MetricDescriptionListDTO getAvailableMetrics() {
		MetricDescriptionListDTO availableMetrics = this.calculateMetricsService.getAvailableMetrics();
		return availableMetrics;
	}
	
    @RequestMapping(value="/calculateMetrics", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> calculateMetrics(@RequestBody CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException {
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

    	Resource resource = new FileSystemResource(zipFile);
    	HttpHeaders headers = new HttpHeaders();
    	
    	MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    	headers.setContentType(mediaType);
    	
    	ContentDisposition disposition = ContentDisposition
                .attachment() 
                .filename(resource.getFilename())
                .build();
    	headers.setContentDisposition(disposition);
    	
    	return new ResponseEntity<Resource>(new FileSystemResource(zipFile), headers, HttpStatus.OK);
    }
}
