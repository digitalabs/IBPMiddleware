/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package org.generationcp.middleware.domain.oms;

import org.generationcp.middleware.util.Debug;

import java.util.HashMap;
import java.util.Map;

public class Scale {

    private Term term;
    private DataType dataType;
    private Map<String, String> categories;

    private String minValue;
    private String maxValue;

    public static enum DataType {

        NUMERIC_VARIABLE(1110)
        , DATE_TIME_VARIABLE(1117)
        , CHARACTER_VARIABLE(1120)
        , CATEGORICAL_VARIABLE(1130);

        private final int id;

        private DataType(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static DataType getById(int id) {
            for (DataType term : values()) {
                if (term.getId() == id) {
                    return term;
                }
            }
            return null;
        }
    }

    public Scale() {
        this.term = new Term();
        this.term.setVocabularyId(CvId.SCALES.getId());
    }

    public Scale(Term term) {
        this.term = term;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public int getId() {
        return term.getId();
    }

    public void setId(int id) {
        term.setId(id);
    }

    public String getName() {
        return term.getName();
    }

    public void setName(String name) {
        term.setName(name);
    }

    public String getDefinition() {
        return term.getDefinition();
    }

    public void setDefinition(String definition) {
        term.setDefinition(definition);
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, String> categories) {
        this.categories = categories;
    }

    public void addCategory(String name, String description){
        if(this.categories == null){
            this.categories = new HashMap<>();
        }
        this.categories.put(name, description);
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String toString() {

        if (term == null){
            return "";
        }

        return "Scale [id=" + term.getId() + ", name=" + term.getName() + ", definition=" + term.getDefinition() + "]";
    }

    public void print(int indent) {
        Debug.println(indent, "Scale: ");
        if (term != null){
            term.print(indent + 3);
        } else {
            Debug.println(indent + 3, "null");
        }
    }

}