package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnalysisServiceImpl implements AnalysisService {
	@Value("${analysis.r.script.path}")
	private String rscriptPath;

	@Override
	public File performAnalysis(File metricsTSVFile) throws IOException, InterruptedException, ExecutionException {
		Path plotsFolder = Paths.get(metricsTSVFile.getParentFile().getAbsolutePath(), "plots");
				
		String command = String.format("Rscript %s -i %s -o %s", rscriptPath, metricsTSVFile.getAbsolutePath(), plotsFolder.toString());
		ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
		processBuilder.inheritIO();
		Process process = processBuilder.start();

		int resCode = process.waitFor();
		if (resCode == 0) {
			return plotsFolder.toFile();
		} else {
			String message = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
			throw new ExecutionException(new Exception(message));
		}
	}
}
