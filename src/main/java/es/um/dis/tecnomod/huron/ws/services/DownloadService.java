package es.um.dis.tecnomod.huron.ws.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

import org.semanticweb.owlapi.model.IRI;

public interface DownloadService {

	File download(IRI iri, Path downloadFolder, String fileName) throws MalformedURLException, IOException;
}
