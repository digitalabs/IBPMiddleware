package org.generationcp.middleware.operation.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.helper.VariableInfo;
import org.generationcp.middleware.pojos.dms.ProjectProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VariableInfoBuilderTest {

    private VariableInfoBuilder variableInfoBuilder;

    @Before
    public void setUp() throws Exception {
        variableInfoBuilder = new VariableInfoBuilder();
    }

	/**
	 * Test to verify create method of VariableInfoBuilder.
	 */

    @Test
    public void testCreate(){

        List<ProjectProperty> projectProperties = new ArrayList<>();

		final String standardVariableProp = "8007";
		final String variableTypeProp = VariableType.TRAIT.getName();
		final String studyInformationProp = "STUDY Info Type";
		final String variableDescriptionProp = "STUDY Info Type Description";
		final String multiFactorialInformationProp = "Multi Factorial Info";


        projectProperties.add(this.createProjectProperty(1, 2, TermId.STANDARD_VARIABLE.getId(), standardVariableProp));
		projectProperties.add(this.createProjectProperty(2, 2, TermId.STUDY_INFORMATION.getId(), studyInformationProp));
		projectProperties.add(this.createProjectProperty(3, 2, TermId.VARIABLE_DESCRIPTION.getId(), variableDescriptionProp));
        projectProperties.add(this.createProjectProperty(4, 2, VariableType.TRAIT.getId(), variableTypeProp));
        projectProperties.add(this.createProjectProperty(5, 2, TermId.MULTIFACTORIAL_INFO.getId(), multiFactorialInformationProp));

        Set<VariableInfo> variableInfoSet = variableInfoBuilder.create(projectProperties);

	  	// Note: Here we only get 1 variable as all the properties will be used to form single variable instance.
        Assert.assertEquals(1, variableInfoSet.size());

        if(variableInfoSet.size() > 0){
            VariableInfo variableInfo = variableInfoSet.iterator().next();
			Assert.assertEquals(variableInfo.getLocalName(), studyInformationProp);
			Assert.assertEquals(variableInfo.getLocalDescription(), variableDescriptionProp);
			Assert.assertEquals(variableInfo.getTreatmentLabel(), multiFactorialInformationProp);
			Assert.assertEquals(variableInfo.getVariableType().getName(), variableTypeProp);
        }

    }

	/**
	 * method to create instance of ProjectProperty based on given raw data.
	 * @param projectPropId project property id
	 * @param rank rank
	 * @param typeId type of property
	 * @param value value of property
	 * @return newly created instance of project property.
	 */
	private ProjectProperty createProjectProperty(final Integer projectPropId, final Integer rank, final Integer typeId, final String value){
		ProjectProperty projectProperty = new ProjectProperty();

		projectProperty.setProjectPropertyId(projectPropId);
		projectProperty.setRank(rank);
		projectProperty.setTypeId(typeId);
		projectProperty.setValue(value);

		return projectProperty;
	}
}
