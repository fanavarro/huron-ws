package es.um.dis.ontology_metrics_ws.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

@Service
public class DownloadServiceImpl implements DownloadService{

	private final static Logger LOGGER = Logger.getLogger(DownloadServiceImpl.class.getName());
	
	@Override
	public File download(IRI iri, Path downloadFolder, String fileName) throws MalformedURLException, IOException {
		File downloadedFile = new File(downloadFolder.toFile(), fileName);
		LOGGER.log(Level.INFO, String.format("Downloading %s into %s", iri.toString(), downloadedFile.getAbsolutePath()));

		
		try (CloseableHttpClient httpclient = HttpClients.custom()
				.setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods 
				.build()){
			HttpGet get = new HttpGet(iri.toURI());
			downloadedFile = httpclient.execute(get, new FileDownloadResponseHandler(downloadedFile));
			LOGGER.log(Level.INFO, String.format("%s downloaded", downloadedFile.getAbsolutePath()));
			return downloadedFile;
		}
		
		
	}
	
	static class FileDownloadResponseHandler implements ResponseHandler<File> {

		private final File target;

		public FileDownloadResponseHandler(File target) {
			this.target = target;
		}

		@Override
		public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			InputStream source = response.getEntity().getContent();
			FileUtils.copyInputStreamToFile(source, this.target);
			return this.target;
		}
		
	}

}
