package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

@Service
public class ZipServiceImpl implements ZipService {
	private final static Logger LOGGER = Logger.getLogger(ZipServiceImpl.class.getName());
	private static final String ZIP_FILE_NAME = "metrics.zip";

	@Override
	public File generateZipFile(String workingDir, File... files) {
		File zipFile = Paths.get(workingDir, ZIP_FILE_NAME).toFile();
		
		try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));) {
			for (File file : files) {
				if (file.isFile()) {
					addFileToZip(zipOut, file);
				} else if (file.isDirectory()) {
					addDirectoryToZip(zipOut, file);
				}
			}
		} catch (IOException ioexception) {
			LOGGER.log(Level.SEVERE, "Error generating zip file", ioexception);
		}
		return zipFile;
	}

	private void addFileToZip(ZipOutputStream zipOut, File file, String zipEntryName) throws IOException {
		zipOut.putNextEntry(new ZipEntry(zipEntryName));
		zipOut.write(Files.readAllBytes(file.toPath()));
		zipOut.closeEntry();
	}

	private void addFileToZip(ZipOutputStream zipOut, File file) throws IOException {
		this.addFileToZip(zipOut, file, file.getName());
	}

	private void addDirectoryToZip(ZipOutputStream zipOut, File folder) throws IOException {
		for (File file : folder.listFiles()) {
			String entryName = Paths.get(folder.getName(), file.getName()).toString();
			this.addFileToZip(zipOut, file, entryName);
		}
	}

}
