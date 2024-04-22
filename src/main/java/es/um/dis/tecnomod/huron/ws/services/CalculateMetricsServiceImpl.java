package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.um.dis.tecnomod.huron.common.OutputFileFormats;
import es.um.dis.tecnomod.huron.common.OutputFileNames;
import es.um.dis.tecnomod.huron.main.Config;
import es.um.dis.tecnomod.huron.result_model.LongTSVResultModel;
import es.um.dis.tecnomod.huron.result_model.RDFResultModel;
import es.um.dis.tecnomod.huron.result_model.WideTSVResultModel;
import es.um.dis.tecnomod.huron.tasks.MetricCalculationTask;
import es.um.dis.tecnomod.huron.ws.dto.input.CalculateMetricsInputDTO;
import es.um.dis.tecnomod.huron.ws.dto.input.OntologyInputDTO;

@Service("calculateMetricsService")
public class CalculateMetricsServiceImpl extends CalculateMetricsService {

	private final static Logger LOGGER = Logger.getLogger(CalculateMetricsServiceImpl.class.getName());

	private static final int THREADS = 5;

	@Autowired
	private DownloadService downloadService;

	@Override
	public File calculateMetrics(CalculateMetricsInputDTO input) throws IOException, InterruptedException, OWLOntologyCreationException {
		String email = input.getEmail();
		Path workingPath = Files.createTempDirectory(email);
		File outputFile = this.getOutputMetricsFile(workingPath, input);
		Config config = this.createConfig(input, outputFile, workingPath);
		LOGGER.log(Level.INFO, "Calculating tasks");
		List<MetricCalculationTask> tasks = this.getMetricCalculationTasks(input, workingPath, config);
		LOGGER.log(Level.INFO, String.format("%d tasks to perform", tasks.size()));
		this.executeWithTaskExecutor(outputFile, tasks, config, THREADS);
		LOGGER.log(Level.INFO, "Tasks finished");
		return outputFile;
	}

	private Config createConfig(CalculateMetricsInputDTO input, File outputFile, Path workingPath) {
		Config config = new Config();
		config.setImports(Imports.fromBoolean(input.isIncludeImports()));
		
		if (OutputFileFormats.LONG_TABLE_OUTPUT_FORMAT.equals(input.getOutputFormat())) {
			config.addResultModel(new LongTSVResultModel(outputFile));
		} else if (OutputFileFormats.WIDE_TABLE_OUTPUT_FORMAT.equals(input.getOutputFormat())) {
			config.addResultModel(new WideTSVResultModel(outputFile));
		} else if (OutputFileFormats.RDF_OUTPUT_FORMAT.equals(input.getOutputFormat())) {
			config.addResultModel(new RDFResultModel(outputFile));
		} else {
			throw new IllegalArgumentException(String.format("Output format %s not recognised.", input.getOutputFormat()));
		}
		
		/* R script needs a long table as input to perform analysis. Include it in the results if needed */
		if (input.isPerformAnalysis() && !OutputFileFormats.LONG_TABLE_OUTPUT_FORMAT.equals(input.getOutputFormat())) {
			File longTableFile = new File(workingPath.toFile(), "metrics_long.tsv");
			config.addResultModel(new LongTSVResultModel(longTableFile));
		}
		return config;
	}
	
	private File getOutputMetricsFile (Path workingPath, CalculateMetricsInputDTO calculateMetricsInputDTO) {
		File outputFile;
		if (OutputFileFormats.LONG_TABLE_OUTPUT_FORMAT.equals(calculateMetricsInputDTO.getOutputFormat())) {
			outputFile = new File(workingPath.toFile(), OutputFileNames.LONG_TABLE_OUTPUT_FILE_NAME);
		} else if (OutputFileFormats.WIDE_TABLE_OUTPUT_FORMAT.equals(calculateMetricsInputDTO.getOutputFormat())) {
			outputFile = new File(workingPath.toFile(), OutputFileNames.WIDE_TABLE_OUTPUT_FILE_NAME);
		} else if (OutputFileFormats.RDF_OUTPUT_FORMAT.equals(calculateMetricsInputDTO.getOutputFormat())) {
			outputFile = new File(workingPath.toFile(), OutputFileNames.RDF_OUTPUT_FILE_NAME);
		} else {
			throw new IllegalArgumentException(String.format("Output format %s not recognised.", calculateMetricsInputDTO.getOutputFormat()));
		}
		return outputFile;
	}

	private List<MetricCalculationTask> getMetricCalculationTasks(CalculateMetricsInputDTO input, Path workingPath, Config config)
			throws MalformedURLException, IOException, OWLOntologyCreationException {
		List<MetricCalculationTask> tasks = new ArrayList<>();
		for (OntologyInputDTO ontologyDTO : input.getOntologies()) {
			String owlFileName = String.format("%s.owl", ontologyDTO.getName());
			File ontologyFile = downloadService.download(ontologyDTO.getIri(), workingPath, owlFileName);
			tasks.add(new MetricCalculationTask(this.getMetricsToApply(input.getMetrics(), config), ontologyFile));
		}
		return tasks;
	}


	
}
