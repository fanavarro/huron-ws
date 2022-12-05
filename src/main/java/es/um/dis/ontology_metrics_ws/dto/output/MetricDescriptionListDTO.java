package es.um.dis.ontology_metrics_ws.dto.output;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The Class MetricDescriptionListDTO.
 */
public class MetricDescriptionListDTO implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2465288988923154926L;
	
	/** The metric description list. */
	private List<MetricDescriptionDTO> metricDescriptionList;

	/**
	 * Gets the metric description list.
	 *
	 * @return the metric description list
	 */
	public List<MetricDescriptionDTO> getMetricDescriptionList() {
		return metricDescriptionList;
	}

	/**
	 * Sets the metric description list.
	 *
	 * @param metricDescriptionList the new metric description list
	 */
	public void setMetricDescriptionList(List<MetricDescriptionDTO> metricDescriptionList) {
		this.metricDescriptionList = metricDescriptionList;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(metricDescriptionList);
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
		MetricDescriptionListDTO other = (MetricDescriptionListDTO) obj;
		return Objects.equals(metricDescriptionList, other.metricDescriptionList);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetricDescriptionListDTO [metricDescriptionList=");
		builder.append(metricDescriptionList);
		builder.append("]");
		return builder.toString();
	}
	
	

}
