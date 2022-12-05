package es.um.dis.ontology_metrics_ws.dto.input;

import java.io.Serializable;
import java.util.Objects;

import org.semanticweb.owlapi.model.IRI;


/**
 * The Class OntologyInputDTO.
 */
public class OntologyInputDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 376143934725148261L;
	
	/** The name of the ontology. */
	private String name;
	
	/** The IRI of the ontology. */
	private IRI iri;

	/**
	 * Gets the ontology name.
	 *
	 * @return the ontology name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the ontology name.
	 *
	 * @param ontologyName the new ontology name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the iri.
	 *
	 * @return the iri
	 */
	public IRI getIri() {
		return iri;
	}

	/**
	 * Sets the iri.
	 *
	 * @param iri the new iri
	 */
	public void setIri(IRI iri) {
		this.iri = iri;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(iri, name);
	}

	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OntologyInputDTO other = (OntologyInputDTO) obj;
		return Objects.equals(iri, other.iri) && Objects.equals(name, other.name);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OntologyInputDTO [ontologyName=");
		builder.append(name);
		builder.append(", iri=");
		builder.append(iri);
		builder.append("]");
		return builder.toString();
	}
	
	

}
