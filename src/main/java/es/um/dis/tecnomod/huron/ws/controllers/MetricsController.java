package es.um.dis.tecnomod.huron.ws.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.output.MetricDescriptionListDTO;
import es.um.dis.tecnomod.huron.ws.services.CalculateMetricsService;
import es.um.dis.tecnomod.huron.ws.services.CompletePipelineService;

/**
 *
 * A sample greetings controller to return greeting text
 */
@RestController
@CrossOrigin
public class MetricsController {
	private final static Logger LOGGER = Logger.getLogger(MetricsController.class.getName());
	
	@Autowired
	private CalculateMetricsService calculateMetricsService;
	
	@Autowired
	private CompletePipelineService completePipelineService;
	
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
    	File zipFile = completePipelineService.calculateMetricsAndPerformAnalysisIfNeeded(calculateMetricsInputDTO);

    	Resource resource = new FileSystemResource(zipFile);
    	HttpHeaders headers = new HttpHeaders();
    	
    	MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    	headers.setContentType(mediaType);
    	
    	ContentDisposition disposition = ContentDisposition
                .attachment() 
                .filename(zipFile.getName())
                .build();
    	headers.setContentDisposition(disposition);
    	
    	return new ResponseEntity<Resource>(new FileSystemResource(zipFile), headers, HttpStatus.OK);
    }
    
    @RequestMapping(value="/calculateMetricsEmail", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> calculateMetricsEmail(@RequestBody CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException {
    	completePipelineService.calculateMetricsAndPerformAnalysisIfNeededEmail(calculateMetricsInputDTO);
		return Collections.singletonMap("message", String.format(
				"Your analysis is queued. The result of your request will be sent to '%s' when it is finished.", calculateMetricsInputDTO.getEmail()));
    }
}
