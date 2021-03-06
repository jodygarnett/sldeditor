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
package com.sldeditor.test.unit.datasource.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import com.sldeditor.common.DataSourceFieldInterface;
import com.sldeditor.datasource.DataSourceField;
import com.sldeditor.datasource.DataSourceUpdatedInterface;
import com.sldeditor.datasource.attribute.DataSourceAttributeData;
import com.sldeditor.datasource.attribute.DataSourceAttributeList;
import com.sldeditor.datasource.impl.CreateDataSourceInterface;
import com.sldeditor.datasource.impl.CreateExternalDataSource;
import com.sldeditor.datasource.impl.CreateInternalDataSource;
import com.sldeditor.datasource.impl.DataSourceImpl;
import com.sldeditor.datasource.impl.GeometryTypeEnum;

/**
 * Unit test for DataSourceImpl.
 * <p>{@link com.sldeditor.datasource.impl.DataSourceImpl()}
 * 
 * @author Robert Ward (SCISYS)
 *
 */
public class DataSourceImplTest {

    class DummyDataSourceUpdate implements DataSourceUpdatedInterface
    {
        public GeometryTypeEnum geometryType = GeometryTypeEnum.UNKNOWN;
        public boolean isConnectedToDataSourceFlag = false;

        @Override
        public void dataSourceLoaded(GeometryTypeEnum geometryType,
                boolean isConnectedToDataSourceFlag) {
            this.geometryType = geometryType;
            this.isConnectedToDataSourceFlag = isConnectedToDataSourceFlag;
        }

    }

    /**
     * Test method for {@link com.sldeditor.datasource.impl.DataSourceImpl#connect()}.
     * Test method for {@link com.sldeditor.datasource.impl.DataSourceImpl#addListener(com.sldeditor.datasource.DataSourceUpdatedInterface)}.
     */
    @Test
    public void testConnectToInternalDataSource() {
        DataSourceImpl ds = new DataSourceImpl();

        DummyInternalSLDEditorFile editorFile = new DummyInternalSLDEditorFile();
        DummyDataSourceUpdate dataSourceUpdateListener = new DummyDataSourceUpdate();
        ds.addListener(dataSourceUpdateListener);

        CreateDataSourceInterface internalDataSource = new CreateInternalDataSource();
        CreateDataSourceInterface externalDataSource = new DummyCreateDataSource();

        ds.setDataSourceCreation(internalDataSource, externalDataSource);
        ds.connect(editorFile);

        assertEquals(GeometryTypeEnum.POINT, dataSourceUpdateListener.geometryType);
        assertFalse(dataSourceUpdateListener.isConnectedToDataSourceFlag);

        Collection<PropertyDescriptor> fieldList = ds.getPropertyDescriptorList();
        assertTrue(fieldList != null);

        List<String> actualFieldnameList = new ArrayList<String>();
        for(PropertyDescriptor field : fieldList)
        {
            actualFieldnameList.add(field.getName().getLocalPart());
        }

        // Check fields extracted ok
        List<String> expectedFieldList = editorFile.getExpectedFieldList();
        assertTrue(expectedFieldList.size() == actualFieldnameList.size());

        // Not assuming fields are in the same order
        int count = 0;
        for(String fieldName : actualFieldnameList)
        {
            if(expectedFieldList.contains(fieldName))
            {
                count ++;
            }
        }
        assertTrue(expectedFieldList.size() == count);

        // Check for fields of certain types
        assertTrue(ds.getAttributes(Integer.class).isEmpty());
        assertTrue(ds.getAttributes(Double.class).isEmpty());

        // Number of fields should be all fields minus the geometry field
        assertEquals(expectedFieldList.size() - 1, ds.getAttributes(String.class).size());

        // Add new field
        DataSourceFieldInterface dataSourceField = new DataSourceField("bearing", Double.class);
        ds.addField(dataSourceField);
        assertTrue(ds.getAttributes(Double.class).size() == 1);

        // Update field
        DataSourceAttributeList attributeData = new DataSourceAttributeList();
        ds.readAttributes(attributeData);

        assertTrue(ds.getPropertyDescriptorList().size() == attributeData.getData().size());

        List<DataSourceAttributeData> attributeDataList = attributeData.getData();

        DataSourceAttributeData data = attributeDataList.remove(2);
        data.setType(Integer.class);
        attributeDataList.add(2, data);

        ds.updateFields(attributeData);
        assertTrue(ds.getAttributes(Integer.class).size() == 1);

        FeatureSource<SimpleFeatureType, SimpleFeature> features = ds.getFeatureSource();
        try {
            assertEquals(1, features.getFeatures().size());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertFalse(dataSourceUpdateListener.isConnectedToDataSourceFlag);
    }

    /**
     * Test method for {@link com.sldeditor.datasource.impl.DataSourceImpl#getAvailableDataStoreList()}.
     */
    @Test
    public void testGetAvailableDataStoreList() {
        DataSourceImpl ds = new DataSourceImpl();
        assertTrue(ds.getAvailableDataStoreList().size() != 0);

        System.out.println(ds.getAvailableDataStoreList());
    }

    @Test
    public void testConnectToExternalDataSource() {
        DataSourceImpl ds = new DataSourceImpl();

        DummyExternalSLDEditorFile editorFile = new DummyExternalSLDEditorFile();
        DummyDataSourceUpdate dataSourceUpdateListener = new DummyDataSourceUpdate();
        ds.addListener(dataSourceUpdateListener);

        CreateDataSourceInterface internalDataSource = new DummyCreateDataSource();
        CreateDataSourceInterface externalDataSource = new CreateExternalDataSource();

        ds.setDataSourceCreation(internalDataSource, externalDataSource);
        ds.connect(editorFile);

        assertEquals(GeometryTypeEnum.POINT, dataSourceUpdateListener.geometryType);
        assertTrue(dataSourceUpdateListener.isConnectedToDataSourceFlag);

        Collection<PropertyDescriptor> fieldList = ds.getPropertyDescriptorList();
        assertTrue(fieldList != null);

        List<String> actualFieldnameList = new ArrayList<String>();
        for(PropertyDescriptor field : fieldList)
        {
            actualFieldnameList.add(field.getName().getLocalPart());
        }

        // Check fields extracted ok
        List<String> expectedFieldList = editorFile.getExpectedFieldList();
        assertTrue(expectedFieldList.size() == actualFieldnameList.size());

        // Not assuming fields are in the same order
        int count = 0;
        for(String fieldName : actualFieldnameList)
        {
            if(expectedFieldList.contains(fieldName))
            {
                count ++;
            }
        }
        assertTrue(expectedFieldList.size() == count);

        // Check for fields of certain types
        assertEquals(1, ds.getAttributes(Integer.class).size());
        assertEquals(1, ds.getAttributes(Long.class).size());
        assertEquals(1, ds.getAttributes(Double.class).size());
        assertEquals(1, ds.getAttributes(String.class).size());

        // Add new field - shouldn't work because connections to external data sources are fixed
        DataSourceFieldInterface dataSourceField = new DataSourceField("bearing", Double.class);
        ds.addField(dataSourceField);
        assertTrue(ds.getAttributes(Double.class).size() == 1);

        // Update field
        DataSourceAttributeList attributeData = new DataSourceAttributeList();
        ds.readAttributes(attributeData);

        assertTrue(ds.getPropertyDescriptorList().size() == attributeData.getData().size());

        List<DataSourceAttributeData> attributeDataList = attributeData.getData();

        DataSourceAttributeData data = attributeDataList.remove(2);
        data.setType(Integer.class);
        attributeDataList.add(2, data);

        // Update fields - shouldn't work because connections to external data sources are fixed
        ds.updateFields(attributeData);
        assertTrue(ds.getAttributes(Integer.class).size() == 1);

        FeatureSource<SimpleFeatureType, SimpleFeature> features = ds.getFeatureSource();
        try {
            assertTrue(features.getFeatures().size() > 1);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(dataSourceUpdateListener.isConnectedToDataSourceFlag);
    }
}
