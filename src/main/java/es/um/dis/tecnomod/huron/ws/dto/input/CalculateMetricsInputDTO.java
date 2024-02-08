package es.um.dis.tecnomod.huron.ws.dto.input;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


/**
 * The Class CalculateMetricsInputDTO.
 */
public class CalculateMetricsInputDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7752142537553124585L;
	
	/** The ontologies. */
	private List<OntologyInputDTO> ontologies;
	
	/** The email. */
	private String email;
	
	/** The metrics. */
	private List<String> metrics;
	
	/** The perform analysis. */
	private boolean performAnalysis;
	
	/** Consider imports? */
	private boolean includeImports;

	/**
	 * Gets the ontologies.
	 *
	 * @return the ontologies
	 */
	public List<OntologyInputDTO> getOntologies() {
		return ontologies;
	}

	/**
	 * Sets the ontologies.
	 *
	 * @param ontologies the new ontologies
	 */
	public void setOntologies(List<OntologyInputDTO> ontologies) {
		this.ontologies = ontologies;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email.
	 *
	 * @param email the new email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the metrics.
	 *
	 * @return the metrics
	 */
	public List<String> getMetrics() {
		return metrics;
	}

	/**
	 * Sets the metrics.
	 *
	 * @param metrics the new metrics
	 */
	public void setMetrics(List<String> metrics) {
		this.metrics = metrics;
	}

	/**
	 * Checks if is perform analysis.
	 *
	 * @return true, if is perform analysis
	 */
	public boolean isPerformAnalysis() {
		return performAnalysis;
	}

	/**
	 * Sets the perform analysis.
	 *
	 * @param performAnalysis the new perform analysis
	 */
	public void setPerformAnalysis(boolean performAnalysis) {
		this.performAnalysis = performAnalysis;
	}

	public boolean isIncludeImports() {
		return includeImports;
	}

	public void setIncludeImports(boolean includeImports) {
		this.includeImports = includeImports;
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, includeImports, metrics, ontologies, performAnalysis);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CalculateMetricsInputDTO other = (CalculateMetricsInputDTO) obj;
		return Objects.equals(email, other.email) && includeImports == other.includeImports
				&& Objects.equals(metrics, other.metrics) && Objects.equals(ontologies, other.ontologies)
				&& performAnalysis == other.performAnalysis;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CalculateMetricsInputDTO [ontologies=");
		builder.append(ontologies);
		builder.append(", email=");
		builder.append(email);
		builder.append(", metrics=");
		builder.append(metrics);
		builder.append(", performAnalysis=");
		builder.append(performAnalysis);
		builder.append(", includeImports=");
		builder.append(includeImports);
		builder.append("]");
		return builder.toString();
	}

	

}
