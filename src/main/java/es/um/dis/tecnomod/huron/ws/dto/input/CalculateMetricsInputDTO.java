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
	
	/**  Consider imports?. */
	private boolean includeImports;
	
	/**  Output format (wide_table, long_table, or rdf). */
	private String outputFormat;

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

	/**
	 * Checks if is include imports.
	 *
	 * @return true, if is include imports
	 */
	public boolean isIncludeImports() {
		return includeImports;
	}

	/**
	 * Sets the include imports.
	 *
	 * @param includeImports the new include imports
	 */
	public void setIncludeImports(boolean includeImports) {
		this.includeImports = includeImports;
	}

	/**
	 * Gets the output format.
	 *
	 * @return the output format
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Sets the output format.
	 *
	 * @param outputFormat the new output format
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(email, includeImports, metrics, ontologies, outputFormat, performAnalysis);
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
		CalculateMetricsInputDTO other = (CalculateMetricsInputDTO) obj;
		return Objects.equals(email, other.email) && includeImports == other.includeImports
				&& Objects.equals(metrics, other.metrics) && Objects.equals(ontologies, other.ontologies)
				&& Objects.equals(outputFormat, other.outputFormat) && performAnalysis == other.performAnalysis;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
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
		builder.append(", outputFormat=");
		builder.append(outputFormat);
		builder.append("]");
		return builder.toString();
	}

}
