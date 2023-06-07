package es.um.dis.tecnomod.huron.ws.dto.output;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Class MetricDescriptionDTO.
 */
public class MetricDescriptionDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1970687314766448828L;

	/** The iri. */
	private String iri;

	/** The name. */
	private String name;

	/** The acronym. */
	private String acronym;

	/** The short description. */
	private String shortDescription;

	/** The long description. */
	private String longDescription;

	/**
	 * Instantiates a new metric description DTO.
	 */
	public MetricDescriptionDTO() {
		super();
	}

	/**
	 * Instantiates a new metric description DTO.
	 *
	 * @param iri the iri
	 * @param name the name
	 * @param acronym the acronym
	 * @param shortDescription the short description
	 * @param longDescription the long description
	 */
	public MetricDescriptionDTO(String iri, String name, String acronym, String shortDescription,
			String longDescription) {
		super();
		this.iri = iri;
		this.name = name;
		this.acronym = acronym;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
	}

	/**
	 * Instantiates a new metric description DTO.
	 *
	 * @param name the name
	 * @param shortDescription the short description
	 * @param longDescription the long description
	 */
	public MetricDescriptionDTO(String name, String shortDescription, String longDescription) {
		this("", name, "", shortDescription, longDescription);
	}

	/**
	 * Gets the iri.
	 *
	 * @return the iri
	 */
	public String getIri() {
		return iri;
	}

	/**
	 * Sets the iri.
	 *
	 * @param iri the new iri
	 */
	public void setIri(String iri) {
		this.iri = iri;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the acronym.
	 *
	 * @return the acronym
	 */
	public String getAcronym() {
		return acronym;
	}

	/**
	 * Sets the acronym.
	 *
	 * @param acronym the new acronym
	 */
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	/**
	 * Gets the short description.
	 *
	 * @return the short description
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * Sets the short description.
	 *
	 * @param shortDescription the new short description
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	/**
	 * Gets the long description.
	 *
	 * @return the long description
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * Sets the long description.
	 *
	 * @param longDescription the new long description
	 */
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(acronym, iri, longDescription, name, shortDescription);
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
		MetricDescriptionDTO other = (MetricDescriptionDTO) obj;
		return Objects.equals(acronym, other.acronym) && Objects.equals(iri, other.iri)
				&& Objects.equals(longDescription, other.longDescription) && Objects.equals(name, other.name)
				&& Objects.equals(shortDescription, other.shortDescription);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetricDescriptionDTO [iri=");
		builder.append(iri);
		builder.append(", name=");
		builder.append(name);
		builder.append(", acronym=");
		builder.append(acronym);
		builder.append(", shortDescription=");
		builder.append(shortDescription);
		builder.append(", longDescription=");
		builder.append(longDescription);
		builder.append("]");
		return builder.toString();
	}

}
