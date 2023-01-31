package es.um.dis.tecnomod.huron.ws.dto.output;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Class MetricDescriptionDTO.
 */
public class MetricDescriptionDTO implements Serializable {
	
	public MetricDescriptionDTO() {
		super();
	}

	public MetricDescriptionDTO(String name, String shortDescription, String longDescription) {
		this();
		this.name = name;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1970687314766448828L;
	
	/** The name. */
	private String name;
	
	/** The short description. */
	private String shortDescription;
	
	/** The long description. */
	private String longDescription;
	
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
		return Objects.hash(longDescription, name, shortDescription);
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
		return Objects.equals(longDescription, other.longDescription) && Objects.equals(name, other.name)
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
		builder.append("MetricDescriptionDTO [name=");
		builder.append(name);
		builder.append(", shortDescription=");
		builder.append(shortDescription);
		builder.append(", longDescription=");
		builder.append(longDescription);
		builder.append("]");
		return builder.toString();
	}
	
	
}
