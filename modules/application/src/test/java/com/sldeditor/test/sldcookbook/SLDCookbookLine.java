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
package com.sldeditor.test.sldcookbook;

import org.junit.Test;

import com.sldeditor.test.SLDTestRunner;

/**
 * The Class SLDCookbookLine runs the tests for line slds in the SLD Cookbook.
 * 
 * @author Robert Ward (SCISYS)
 */
public class SLDCookbookLine
{
    @Test
    public void line_simpleline()
    {
        SLDTestRunner.runTest("line", "line_simpleline.xml");
    }
    
    @Test
    public void line_attributebasedline()
    {
        SLDTestRunner.runTest("line", "line_attributebasedline.xml");
    }

    @Test
    public void line_dashdot()
    {
        SLDTestRunner.runTest("line", "line_dashdot.xml");
    }

    @Test
    public void line_dashedline()
    {
        SLDTestRunner.runTest("line", "line_dashedline.xml");
    }

    @Test
    public void line_dashspace()
    {
        SLDTestRunner.runTest("line", "line_dashspace.xml");
    }

    @Test
    public void line_labelfollowingline()
    {
        SLDTestRunner.runTest("line", "line_labelfollowingline.xml");
    }
    
    @Test
    public void line_linewithborder()
    {
        SLDTestRunner.runTest("line", "line_linewithborder.xml");
    }

    @Test
    public void line_linewithdefaultlabel()
    {
        SLDTestRunner.runTest("line", "line_linewithdefaultlabel.xml");
    }
    
    @Test
    public void line_optimizedlabel()
    {
        SLDTestRunner.runTest("line", "line_optimizedlabel.xml");
    }
    
    @Test
    public void line_optimizedstyledlabel()
    {
        SLDTestRunner.runTest("line", "line_optimizedstyledlabel.xml");
    }
    
    @Test
    public void line_railroad()
    {
        SLDTestRunner.runTest("line", "line_railroad.xml");
    }
    
    @Test
    public void line_zoombasedline()
    {
        SLDTestRunner.runTest("line", "line_zoombasedline.xml");
    }
}
