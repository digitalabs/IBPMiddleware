
package org.generationcp.middleware.domain.ontology;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.pojos.derived_variables.Formula;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.generationcp.middleware.util.Debug;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends {@link Term} The Variable with term, property, scale, method.
 *
 */
public class Variable extends Term {

	/**
	 *
	 */
	private static final long serialVersionUID = -9210220473228370658L;

	private String alias;

	/**
	 * Variable types are used to determine where in the system a variable is intended to be used. Variable types are used to restrict the
	 * variable's display to only locations in the BMS that are relevant to that variable type. A variable may have multiple variable types,
	 * and if there is no variable type selected, then the variable will show up in all locations where variables are used in the BMS.
	 * Variable types replace the older concept of roles (the stored_in relationship).
	 */
	private final Set<VariableType> variableTypes = new HashSet<>();

	private Method method;
	private Property property;
	private Scale scale;
	private FormulaDto formula;
	private boolean allowsFormula;
	private Boolean isFavorite;
	private String minValue;
	private String maxValue;
	private Integer observations;
	private Integer datasets;
	private Integer studies;
	private Boolean hasPair;
  	private Boolean hasUsage;
	private Integer germplasm;
	private Integer breedingMethods;

	public Variable() {
		this.setVocabularyId(CvId.VARIABLES.getId());
	}

	public Variable(org.generationcp.middleware.domain.oms.Term term) {
		super(term);
		this.setVocabularyId(CvId.VARIABLES.getId());
	}

	public Variable(final int id, final String name, final String definition, final String alias) {
		this.setId(id);
		this.setName(name);
		this.setDefinition(definition);
		this.setAlias(alias);
		this.setVocabularyId(CvId.VARIABLES.getId());

	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Set<VariableType> getVariableTypes() {
		return this.variableTypes;
	}

	public void addVariableType(VariableType type) {
		this.variableTypes.add(type);
	}

	public Method getMethod() {
		return this.method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Property getProperty() {
		return this.property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public Scale getScale() {
		return this.scale;
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	public FormulaDto getFormula() {
		return formula;
	}

	public void setFormula(FormulaDto formula) {
		this.formula = formula;
	}

	public boolean isAllowsFormula() {
		return allowsFormula;
	}

	public void setAllowsFormula(final boolean allowsFormula) {
		this.allowsFormula = allowsFormula;
	}

	/**
	 * @return negative value if unknown else the actual number of studies that have used the variable.
	 */
	public Integer getObservations() {
		return this.observations;
	}

	public void setObservations(Integer observations) {
		this.observations = observations;
	}

	public String getMinValue() {
		return this.minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return this.maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	public Boolean getIsFavorite() {
		return this.isFavorite;
	}

	public void setIsFavorite(Boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	/**
	 * @return negative value if unknown else the actual number of studies that have used the variable.
	 */
	public Integer getStudies() {
		return this.studies;
	}

	public void setStudies(Integer studies) {
		this.studies = studies;
	}

	public Boolean getHasPair() {
		return this.hasPair;
	}

	public void setHasPair(Boolean hasPair) {
		this.hasPair = hasPair;
	}

  	public Boolean getHasUsage() {
		return hasUsage;
  	}

  	public void setHasUsage(Boolean hasUsage) {
		this.hasUsage = hasUsage;
  	}

	public Integer getDatasets() {
		return datasets;
	}

	public void setDatasets(final Integer datasets) {
		this.datasets = datasets;
	}

	public Integer getGermplasm() {
		return this.germplasm;
	}

	public void setGermplasm(final Integer germplasm) {
		this.germplasm = germplasm;
	}

	public Integer getBreedingMethods() {
		return this.breedingMethods;
	}

	public void setBreedingMethods(final Integer breedingMethods) {
		this.breedingMethods = breedingMethods;
	}

	@Override
	public String toString() {
		return "Variable{" + "alias='" + this.alias + '\'' + ", variableTypes=" + this.variableTypes + ", property=" + this.property
				+ ", method=" + this.method + ", scale=" + this.scale + ", isFavorite=" + this.isFavorite + ", minValue='" + this.minValue
				+ '\'' + ", maxValue='" + this.maxValue + '\'' + ", observations=" + this.observations + ", studies=" + this.studies +
				", datasets=" + this.datasets + ", germplasm=" + this.germplasm + ", breedingMethods=" + this.breedingMethods + "} "
				+ super.toString();
	}

	@Override
	public void print(int indent) {
		Debug.println(indent, "Variable: ");
		super.print(indent + 3);

		if (this.alias != null) {
			Debug.println(indent + 3, "alias:" + this.alias);
		}

		if (this.variableTypes != null) {
			Debug.println(indent + 3, "Variable Types:" + this.variableTypes);
		}

		if (this.property != null) {
			Debug.println(indent + 3, "property:" + this.property);
		}

		if (this.method != null) {
			Debug.println(indent + 3, "method:" + this.method);
		}

		if (this.scale != null) {
			Debug.println(indent + 3, "scale:" + this.scale);
		}

		if (this.isFavorite != null) {
			Debug.println(indent + 3, "isFavorite:" + this.isFavorite);
		}

		if (this.minValue != null) {
			Debug.println(indent + 3, "minValue:" + this.minValue);
		}

		if (this.maxValue != null) {
			Debug.println(indent + 3, "Variable Types:" + this.maxValue);
		}

		if (this.observations != null) {
			Debug.println(indent + 3, "observations:" + this.observations);
		}

		if (this.datasets != null) {
			Debug.println(indent + 3, "datasets:" + this.datasets);
		}

		if (this.formula != null) {
			Debug.println(indent + 3, "formula:" + this.formula);
		}

		if (this.germplasm != null) {
			Debug.println(indent + 3, "germplasm:" + this.germplasm);
		}

		if (this.breedingMethods != null) {
			Debug.println(indent + 3, "breedingMethods:" + this.breedingMethods);
		}
	}

}
