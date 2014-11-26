package org.generationcp.middleware.operation.transformer.etl;

import org.generationcp.middleware.domain.dms.*;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;

import java.util.ArrayList;
import java.util.List;

public class MeasurementVariableTransformer extends Transformer {
	
    public MeasurementVariableTransformer(HibernateSessionProvider sessionProviderForLocal) {
    	super(sessionProviderForLocal);
	}
	
	public List<MeasurementVariable> transform(VariableTypeList variableTypeList, boolean isFactor) {
		
	    List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
	    
	    if (variableTypeList != null && variableTypeList.size() > 0) {
	        for (VariableType variableType : variableTypeList.getVariableTypes()) {
	            StandardVariable stdVariable = variableType.getStandardVariable();
	            String label = getLabelOfStoredIn(stdVariable.getStoredIn().getId());
	            
	            MeasurementVariable measurementVariable = new MeasurementVariable(stdVariable.getId(), variableType.getLocalName(), 
	                    stdVariable.getDescription(), stdVariable.getScale().getName(), stdVariable.getMethod().getName(),
	                    stdVariable.getProperty().getName(), stdVariable.getDataType().getName(), "", 
	                    label);
	            measurementVariable.setStoredIn(stdVariable.getStoredIn().getId());
	            measurementVariable.setFactor(isFactor);
	            measurementVariable.setDataTypeId(stdVariable.getDataType().getId());
	            measurementVariable.setPossibleValues(transformPossibleValues(stdVariable.getEnumerations()));
	            if (stdVariable.getConstraints() != null) {
	            	measurementVariable.setMinRange(stdVariable.getConstraints().getMinValue());
	            	measurementVariable.setMaxRange(stdVariable.getConstraints().getMaxValue());
	            }
	            if (variableType.getTreatmentLabel() != null && !"".equals(variableType.getTreatmentLabel())) {
	            	measurementVariable.setTreatmentLabel(variableType.getTreatmentLabel());
	            }
	            measurementVariables.add(measurementVariable);
	        }
	    }
	    
	    return measurementVariables;
	}
	
	public List<MeasurementVariable> transform(VariableList variableList, boolean isFactor, boolean isStudy) {
		
	    List<MeasurementVariable> measurementVariables = new ArrayList<MeasurementVariable>();
	    
	    if (variableList != null && variableList.size() > 0) {
	        for (Variable variable : variableList.getVariables()) {
	        	VariableType variableType = variable.getVariableType();
	            StandardVariable stdVariable = variableType.getStandardVariable();
	            String label = getLabelOfStoredIn(stdVariable.getStoredIn().getId());
	            if (!isFactor && !isStudy) {  //for trial constants
	            	label = PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0);
	            }
	            
	            MeasurementVariable measurementVariable = new MeasurementVariable(stdVariable.getId(), variableType.getLocalName(), 
	                    stdVariable.getDescription(), stdVariable.getScale().getName(), stdVariable.getMethod().getName(),
	                    stdVariable.getProperty().getName(), stdVariable.getDataType().getName(), "", 
	                    label);
	            measurementVariable.setStoredIn(stdVariable.getStoredIn().getId());
	            measurementVariable.setFactor(isFactor);
	            measurementVariable.setValue(variable.getDisplayValue());
	            measurementVariable.setDataTypeId(stdVariable.getDataType().getId());
	            measurementVariable.setPossibleValues(transformPossibleValues(stdVariable.getEnumerations()));
	            if (stdVariable.getConstraints() != null) {
	            	measurementVariable.setMinRange(stdVariable.getConstraints().getMinValue());
	            	measurementVariable.setMaxRange(stdVariable.getConstraints().getMaxValue());
	            }
	            if (variableType.getTreatmentLabel() != null && !"".equals(variableType.getTreatmentLabel())) {
	            	measurementVariable.setTreatmentLabel(variableType.getTreatmentLabel());
	            }
	            measurementVariables.add(measurementVariable);
	        }
	    }
	    
	    return measurementVariables;
	}

	private String getLabelOfStoredIn(int storedIn) {
            return PhenotypicType.getPhenotypicTypeById(storedIn).getLabelList().get(0);
        }
	
	public List<ValueReference> transformPossibleValues(List<Enumeration> enumerations) {
		List<ValueReference> list = new ArrayList<ValueReference>();
		
		if (enumerations != null) {
			for (Enumeration enumeration : enumerations) {
				list.add(new ValueReference(enumeration.getId(), enumeration.getName(), enumeration.getDescription()));
			}
		}
		
		return list;
	}

	public MeasurementVariable transform(StandardVariable stdVariable, boolean isFactor) {
		MeasurementVariable measurementVariable = null;
		
	    if (stdVariable != null) {
            String label = getLabelOfStoredIn(stdVariable.getStoredIn().getId());
            
            measurementVariable = new MeasurementVariable(stdVariable.getId(), stdVariable.getName(), 
                    stdVariable.getDescription(), stdVariable.getScale().getName(), stdVariable.getMethod().getName(),
                    stdVariable.getProperty().getName(), stdVariable.getDataType().getName(), "", 
                    label);
            measurementVariable.setStoredIn(stdVariable.getStoredIn().getId());
            measurementVariable.setFactor(isFactor);
            measurementVariable.setDataTypeId(stdVariable.getDataType().getId());
            measurementVariable.setPossibleValues(transformPossibleValues(stdVariable.getEnumerations()));
            if (stdVariable.getConstraints() != null) {
            	measurementVariable.setMinRange(stdVariable.getConstraints().getMinValue());
            	measurementVariable.setMaxRange(stdVariable.getConstraints().getMaxValue());
            }
	    }
	    
	    return measurementVariable;
	}
}
