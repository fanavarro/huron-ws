package es.um.dis.tecnomod.huron.ws.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.output.MetricDescriptionDTO;
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
	@Qualifier("calculateMetricsService")
	private CalculateMetricsService calculateMetricsService;
	
	@Autowired
	@Qualifier("calculateMetricsServiceRDF")
	private CalculateMetricsService calculateMetricsServiceRDF;
	
	@Autowired
	private CompletePipelineService completePipelineService;
	
	@GetMapping("/hello/{name}")
	public String hello(@PathVariable String name) {
		return "Hello " + name + "!!";
	}
	
	@GetMapping(value="/getAvailableMetrics", produces = MediaType.APPLICATION_JSON_VALUE)
	public MetricDescriptionListDTO getAvailableMetrics() throws OWLOntologyCreationException {
		MetricDescriptionListDTO availableMetrics = this.calculateMetricsService.getAvailableMetrics();
		
		Collections.sort(availableMetrics.getMetricDescriptionList(), new Comparator<MetricDescriptionDTO>() {
			@Override
			public int compare(MetricDescriptionDTO o1, MetricDescriptionDTO o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return availableMetrics;
	}
	
    @PostMapping(value="/calculateMetrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/zip")
    public ResponseEntity<Resource> calculateMetrics(@RequestBody CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException, OWLOntologyCreationException {
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
    
    @PostMapping(value="/calculateMetrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "text/turtle")
    public ResponseEntity<Resource> calculateMetricsRDF(@RequestBody CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException, OWLOntologyCreationException {
    	File rdfFile = calculateMetricsServiceRDF.calculateMetrics(calculateMetricsInputDTO);

    	Resource resource = new FileSystemResource(rdfFile);
    	HttpHeaders headers = new HttpHeaders();
    	
    	MediaType mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    	headers.setContentType(mediaType);
    	
    	ContentDisposition disposition = ContentDisposition
                .attachment() 
                .filename(rdfFile.getName())
                .build();
    	headers.setContentDisposition(disposition);
    	
    	return new ResponseEntity<Resource>(new FileSystemResource(rdfFile), headers, HttpStatus.OK);
    }
    
    @PostMapping(value="/calculateMetricsEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> calculateMetricsEmail(@RequestBody CalculateMetricsInputDTO calculateMetricsInputDTO) throws IOException, InterruptedException, OWLOntologyCreationException {
    	completePipelineService.calculateMetricsAndPerformAnalysisIfNeededEmail(calculateMetricsInputDTO);
		return Collections.singletonMap("message", String.format(
				"Your analysis is queued. The result of your request will be sent to '%s' when it is finished.", calculateMetricsInputDTO.getEmail()));
    }
}
