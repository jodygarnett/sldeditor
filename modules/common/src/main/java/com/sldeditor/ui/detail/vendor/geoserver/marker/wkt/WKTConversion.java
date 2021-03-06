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
package com.sldeditor.ui.detail.vendor.geoserver.marker.wkt;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.factory.Hints;
import org.geotools.geometry.GeometryFactoryFinder;
import org.geotools.geometry.iso.aggregate.MultiPrimitiveImpl;
import org.geotools.geometry.iso.primitive.CurveImpl;
import org.geotools.geometry.iso.primitive.PointImpl;
import org.geotools.geometry.iso.primitive.SurfaceImpl;
import org.geotools.geometry.text.WKTParser;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.Geometry;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.aggregate.AggregateFactory;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.coordinate.LineSegment;
import org.opengis.geometry.primitive.Primitive;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.opengis.geometry.primitive.Ring;

/**
 * Converts a WKT string to WKTxxx objects.
 * 
 * @author Robert Ward (SCISYS)
 */
public class WKTConversion {

    private static final String WKT_MULTIPOINT = "MULTIPOINT";
    private static final String WKT_POINT = "POINT";
    private static final String WKT_LINESTRING = "LINESTRING";
    private static final String WKT_POLYGON = "POLYGON";
    private static final String WKT_MULTILINESTRING = "MULTILINESTRING";
    private static final String WKT_MULTIPOLYGON = "MULTIPOLYGON";

    /** The Constant WKT_PREFIX. */
    public static final String WKT_PREFIX = "wkt://";

    /** The wkt parser. */
    private static WKTParser wktParser = null;

    /** The wkt type list. */
    private static List<WktType> wktTypeList = new ArrayList<WktType>();

    /** The wkt type map. */
    private static Map<String, WktType> wktTypeMap = new HashMap<String, WktType>();

    /**
     * Gets the WKT type data.
     *
     * @return the WKT type data
     */
    public static List<WktType> getWKTTypeData()
    {
        if(wktTypeList.isEmpty())
        {
            wktTypeList.add(new WktType(WKT_POINT, false, 1, "Point"));
            wktTypeList.add(new WktType(WKT_MULTIPOINT, true, 1, "Point"));
            wktTypeList.add(new WktType(WKT_LINESTRING, false, 2, "Line"));
            wktTypeList.add(new WktType("LINEARRING", false, 2, "Line"));
            wktTypeList.add(new WktType(WKT_MULTILINESTRING, true, 2, "Line"));
            wktTypeList.add(new WktType(WKT_POLYGON, false, -1, "Polygon"));
            wktTypeList.add(new WktType(WKT_MULTIPOLYGON, true, -1, "Polygon", true));

            for(WktType wkyType : wktTypeList)
            {
                wktTypeMap.put(wkyType.getName(), wkyType);
            }
        }

        return wktTypeList;
    }

    /**
     * Gets the WKTType object for the given name
     *
     * @param geometryType the geometry type
     * @return the WKT type
     */
    public static WktType getWKTType(String geometryType) {
        return wktTypeMap.get(geometryType);
    }

    /**
     * Parses the wkt string.
     *
     * @param wktString the wkt string
     * @return the WKT geometry
     */
    public static WKTGeometry parseWKTString(String wktString) {
        if(wktParser == null)
        {
            initialise();
        }

        WKTGeometry wktGeometry = new WKTGeometry();

        // Remove wkt:// prefix
        if(wktString.startsWith(WKT_PREFIX))
        {
            wktString = wktString.substring(WKT_PREFIX.length());
        }

        try {
            Geometry geometry = wktParser.parse(wktString);

            if(geometry instanceof MultiPrimitiveImpl)
            {
                int index = 0;
                MultiPrimitiveImpl multiLine = (MultiPrimitiveImpl) geometry;
                for(Primitive primitive : multiLine.getElements())
                {
                    if(primitive instanceof CurveImpl)
                    {
                        WKTSegmentList ptList = new WKTSegmentList();
                        wktGeometry.setGeometryType(getWKTType(WKT_MULTILINESTRING));
                        CurveImpl curve = (CurveImpl)primitive;

                        extractLineSegments(curve, ptList);

                        wktGeometry.addSegmentList(index, ptList);
                    }
                    else if(primitive instanceof PointImpl)
                    {
                        WKTSegmentList ptList = new WKTSegmentList();
                        wktGeometry.setGeometryType(getWKTType(WKT_MULTIPOINT));

                        PointImpl point = (PointImpl)primitive;

                        WKTPoint wktPoint = new WKTPoint(point.getDirectPosition());
                        ptList.addPoint(wktPoint);
                        wktGeometry.addSegmentList(index, ptList);
                    }
                    else if(primitive instanceof SurfaceImpl)
                    {
                        wktGeometry.setGeometryType(getWKTType(WKT_MULTIPOLYGON));
                        SurfaceImpl surfaceImpl = (SurfaceImpl) primitive;

                        for(Ring ring : surfaceImpl.getBoundaryRings())
                        {
                            WKTSegmentList ptList = new WKTSegmentList();
                            for(Primitive ringPrimitive : ring.getElements())
                            {
                                if(ringPrimitive instanceof CurveImpl)
                                {
                                    CurveImpl curve = (CurveImpl)ringPrimitive;
                                    extractLineSegments(curve, ptList);
                                }
                            }
                            wktGeometry.addSegmentList(index, ptList);
                        }
                    }
                    index ++;
                }
            }
            else if(geometry instanceof PointImpl)
            {
                wktGeometry.setGeometryType(getWKTType(WKT_POINT));
                PointImpl pointImpl = (PointImpl) geometry;

                WKTSegmentList ptList = new WKTSegmentList();
                wktGeometry.addSegmentList(0, ptList);

                WKTPoint point = new WKTPoint(pointImpl.getDirectPosition());

                ptList.addPoint(point);
            }
            else if(geometry instanceof CurveImpl)
            {
                wktGeometry.setGeometryType(getWKTType(WKT_LINESTRING));
                CurveImpl curveImpl = (CurveImpl) geometry;

                WKTSegmentList ptList = new WKTSegmentList();
                wktGeometry.addSegmentList(0, ptList);

                extractLineSegments(curveImpl, ptList);
            }
            else if(geometry instanceof SurfaceImpl)
            {
                wktGeometry.setGeometryType(getWKTType(WKT_POLYGON));
                SurfaceImpl surfaceImpl = (SurfaceImpl) geometry;

                for(Ring ring : surfaceImpl.getBoundaryRings())
                {
                    WKTSegmentList ptList = new WKTSegmentList();
                    wktGeometry.addSegmentList(0, ptList);

                    for(Primitive primitive : ring.getElements())
                    {
                        if(primitive instanceof CurveImpl)
                        {
                            CurveImpl curve = (CurveImpl)primitive;
                            extractLineSegments(curve, ptList);
                        }
                    }
                }
            }
        } catch (ParseException e) {

            wktGeometry.setValid(false);

            for(WktType wktType : wktTypeList)
            {
                if(wktString.startsWith(wktType.getName()))
                {
                    wktGeometry.setGeometryType(wktType);
                }
            }
        }

        return wktGeometry;
    }

    /**
     * Extract line segments.
     *
     * @param curveImpl the curve impl
     * @param ptList the pt list
     */
    private static void extractLineSegments(CurveImpl curveImpl, WKTSegmentList ptList) {
        for(LineSegment lineSegment : curveImpl.asLineSegments())
        {
            WKTPoint startPoint = new WKTPoint(lineSegment.getStartPoint());
            WKTPoint endPoint = new WKTPoint(lineSegment.getEndPoint());

            ptList.addPoint(startPoint);
            ptList.addPoint(endPoint);
        }
    }

    /**
     * Initialise the WKTParser object
     */
    private static void initialise() {
        Hints hints = new Hints( Hints.CRS, DefaultGeographicCRS.WGS84 );

        PositionFactory positionFactory = GeometryFactoryFinder.getPositionFactory(hints);
        GeometryFactory geometryFactory = GeometryFactoryFinder.getGeometryFactory(hints);
        PrimitiveFactory primitiveFactory = GeometryFactoryFinder.getPrimitiveFactory(hints);
        AggregateFactory aggregateFactory = GeometryFactoryFinder.getAggregateFactory(hints);

        wktParser = new WKTParser(geometryFactory, primitiveFactory, positionFactory, aggregateFactory);
    }

    /**
     * Generate wkt string.
     *
     * @param wktGeometry the wkt geometry
     * @param formatText the format text flag
     * @return the string
     */
    public static String generateWKTString(WKTGeometry wktGeometry, boolean formatText) {
        StringBuilder sb = new StringBuilder();
        sb.append(WKT_PREFIX);
        if(wktGeometry.getGeometryType() != null)
        {
            sb.append(wktGeometry.getGeometryType());

            if((wktGeometry.getGeometryType().getName().compareTo(WKT_MULTILINESTRING) == 0) ||
                    (wktGeometry.getGeometryType().getName().compareTo(WKT_POLYGON) == 0) ||
                    (wktGeometry.getGeometryType().getName().compareTo(WKT_MULTIPOINT) == 0))
            {
                if(formatText)
                {
                    sb.append("(\n");
                }

                int index = 0;
                List<WKTSegmentList> segmentList = wktGeometry.getSegmentList(0);
                if(segmentList != null)
                {
                    for(WKTSegmentList pointList :segmentList)
                    {
                        if(index > 0)
                        {
                            if(formatText)
                            {
                                sb.append(",\n ");
                            }
                            else
                            {
                                sb.append(", ");
                            }
                        }
                        sb.append("\t");
                        sb.append(pointList.getWKTString());
                        index ++;
                    }
                }
                if(formatText)
                {
                    sb.append("\n");
                }
                sb.append(")");
            }
            else if(wktGeometry.getGeometryType().getName().compareTo(WKT_MULTIPOLYGON) == 0)
            {
                if(formatText)
                {
                    sb.append("(\n");
                }
                for(int multiIndex = 0; multiIndex < wktGeometry.getNoOfSegments(); multiIndex ++)
                {
                    if(multiIndex > 0)
                    {
                        if(formatText)
                        {
                            sb.append(",\n ");
                        }
                        else
                        {
                            sb.append(", ");
                        }
                    }
                    sb.append("\t");
                    List<WKTSegmentList> segmentList = wktGeometry.getSegmentList(multiIndex);
                    if(segmentList != null)
                    {
                        sb.append("(");
                        int index = 0;
                        for(WKTSegmentList pointList : segmentList)
                        {
                            if(index > 0)
                            {
                                sb.append(", ");
                            }
                            sb.append(pointList.getWKTString());
                            index ++;
                        }
                        sb.append(")");
                    }
                }
                if(formatText)
                {
                    sb.append("\n");
                }
                sb.append(")");
            }
            else {
                List<WKTSegmentList> segmentList = wktGeometry.getSegmentList(0);
                if(segmentList != null)
                {
                    if(wktGeometry.getGeometryType().getName().compareTo(WKT_POINT) == 0)
                    {
                        for(WKTSegmentList pointList : segmentList)
                        {
                            sb.append(pointList.getWKTString());
                        }            
                    }
                    else if(wktGeometry.getGeometryType().getName().compareTo(WKT_LINESTRING) == 0)
                    {
                        for(WKTSegmentList pointList : segmentList)
                        {
                            sb.append(pointList.getWKTString());
                        }
                    }
                }
            }
        }

        return sb.toString();
    }

    /**
     * Creates the empty WKT geometry
     *
     * @param wktType the wkt type
     * @return the WKT geometry
     */
    public static WKTGeometry createEmpty(WktType wktType) {
        WKTGeometry wktGeometry = new WKTGeometry();
        wktGeometry.setGeometryType(wktType);

        return wktGeometry;
    }

}
