/*
 * SLD Editor - The Open Source Java SLD Editor
 *
 * Copyright (C) 2016, SCISYS UK Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sldeditor.rendertransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Geometry;

import net.opengis.ows11.impl.ValueTypeImpl;
import net.opengis.wps10.InputDescriptionType;
import net.opengis.wps10.LiteralInputType;
import net.opengis.wps10.ProcessDescriptionType;
import net.opengis.wps10.SupportedCRSsType;
import net.opengis.wps10.SupportedComplexDataType;

/**
 * Class that provides the process functions read from a GeoServer instance.
 *
 * @author Robert Ward (SCISYS)
 */
public class CustomProcessFunction {

    /** The Constant ENUMERATION_NAME. */
    private static final String ENUMERATION_NAME = "Enumeration";

    /** The Constant BBOX_NAME. */
    private static final String BBOX_NAME = "BBOX";

    /** The Constant GEOMETRY_NAME. */
    private static final String GEOMETRY_NAME = "Geometry";

    /** The data type map. */
    private static Map<String, Class<?> > dataTypeMap = new HashMap<String, Class<?> >();

    /** The filter factory. */
    private static FilterFactory ff = CommonFactoryFinder.getFilterFactory();

    /**
     * Extract parameters.
     *
     * @param selectedCustomFunction the selected custom function
     * @return the list
     */
    public List<ProcessFunctionParameterValue> extractParameters(
            ProcessDescriptionType selectedCustomFunction) {
        List<ProcessFunctionParameterValue> valueList = new ArrayList<ProcessFunctionParameterValue>();

        if(dataTypeMap.isEmpty())
        {
            populateDataMap();
        }

        for(int index = 0; index < selectedCustomFunction.getDataInputs().getInput().size(); index ++)
        {
            InputDescriptionType input = (InputDescriptionType) selectedCustomFunction.getDataInputs().getInput().get(index);

            ProcessFunctionParameterValue value = new ProcessFunctionParameterValue();

            value.name = input.getIdentifier().getValue();
            getType(input, value);
            value.optional = isOptional(input);
            value.type = dataTypeMap.get(value.dataType);

            value.minOccurences = input.getMinOccurs().intValue();
            value.maxOccurences = input.getMaxOccurs().intValue();

            valueList.add(value);
        }
         return valueList;
    }

    /**
     * Populate data map.
     */
    private void populateDataMap() {
        dataTypeMap.put("xs:int", Integer.class);
        dataTypeMap.put("xs:double", Double.class);
        dataTypeMap.put("xs:boolean", Boolean.class);
        dataTypeMap.put("xs:float", Float.class);
        dataTypeMap.put("xs:double", Double.class);
        dataTypeMap.put("xs:long", Long.class);
        dataTypeMap.put(GEOMETRY_NAME, Geometry.class);
        dataTypeMap.put(BBOX_NAME, ReferencedEnvelope.class);
        dataTypeMap.put(ENUMERATION_NAME, StringBuilder.class);
    }

    /**
     * Checks if field is optional.
     *
     * @param input the input
     * @return the boolean
     */
    private Boolean isOptional(InputDescriptionType input) {
        int minOccurs = input.getMinOccurs().intValue();
        int maxOccurs = input.getMaxOccurs().intValue();

        if((minOccurs == 0) && (maxOccurs == 1))
        {
            return true;
        }
        return false;
    }

    /**
     * Gets the type.
     *
     * @param inputDescription the input description
     * @param value the value
     * @return the type
     */
    private void getType(InputDescriptionType inputDescription, ProcessFunctionParameterValue value) {
        LiteralInputType literal = inputDescription.getLiteralData();
        if(literal != null)
        {
            if(literal.getDataType() != null)
            {
                value.dataType = literal.getDataType().getValue();
                String defaultValue = literal.getDefaultValue();
                if(defaultValue != null)
                {
                    if(value.dataType.compareTo("xs:int") == 0)
                    {
                        value.value = ff.literal(Integer.valueOf(defaultValue).intValue());
                    }
                    else if(value.dataType.compareTo("xs:boolean") == 0)
                    {
                        value.value = ff.literal(Boolean.valueOf(defaultValue).booleanValue());
                    }
                    else if(value.dataType.compareTo("xs:double") == 0)
                    {
                        value.value = ff.literal(Double.valueOf(defaultValue).doubleValue());
                    }
                    else if(value.dataType.compareTo("xs:float") == 0)
                    {
                        value.value = ff.literal(Float.valueOf(defaultValue).floatValue());
                    }
                    else if(value.dataType.compareTo("xs:long") == 0)
                    {
                        value.value = ff.literal(Long.valueOf(defaultValue).longValue());
                    }
                    else
                    {
                        value.type = String.class;
                    }
                }
            }
            else if(literal.getAllowedValues() != null)
            {
                value.dataType = ENUMERATION_NAME;
                value.value = ff.literal(literal.getDefaultValue());
                value.enumValueList = new ArrayList<String>();

                for(Object valueTypeObj : literal.getAllowedValues().getValue())
                {
                    ValueTypeImpl valueType = (ValueTypeImpl) valueTypeObj;
                    value.enumValueList.add(valueType.getValue());
                }
            }
        }
        else
        {
            SupportedCRSsType bbox = inputDescription.getBoundingBoxData();
            if(bbox != null)
            {
                value.dataType = BBOX_NAME;
            }
            else
            {
                SupportedComplexDataType complex = inputDescription.getComplexData();
                if(complex != null)
                {
                    // ComplexDataCombinationsType parameterDataType = complex.getSupported();

                    value.dataType = GEOMETRY_NAME;
                }
            }
        }
    }
}
