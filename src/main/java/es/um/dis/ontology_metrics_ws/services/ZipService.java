package es.um.dis.ontology_metrics_ws.services;

import java.io.File;

public interface ZipService {
	/**
	 * Generates a zip file by including all the files passed as argument.
	 * @param workingDir Folder in which the zip file will be created.
	 * @param files Files to be compressed
	 * @return File with the zip file
	 */
	File generateZipFile(String workingDir, File... files);
}
