package org.wowtools.giscat.vector.pojo.converter;

import junit.framework.TestCase;
import org.junit.Assert;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

public class FoolStyleFeatureConverterTest extends TestCase {

    public void testXy2Point() {
        Point point = FoolStyleFeatureConverter.xy2Point(20, 30);
        Assert.assertEquals("POINT (20 30)", point.toText());
    }

    public void testList2Point() {
        Point point = FoolStyleFeatureConverter.list2Point(List.of(20d, 30d));
        Assert.assertEquals("POINT (20 30)", point.toText());
    }

    public void testArray2Point() {
        Point point = FoolStyleFeatureConverter.array2Point(new double[]{20, 30});
        Assert.assertEquals("POINT (20 30)", point.toText());
    }

    public void testStr2Point() {
        Point point = FoolStyleFeatureConverter.str2Point("20 30", " ");
        Assert.assertEquals("POINT (20 30)", point.toText());
    }

    public void testList2Line() {
        LineString line = FoolStyleFeatureConverter.list2Line(List.of(1d, 2d, 3d, 4d));
        Assert.assertEquals("LINESTRING (1 2, 3 4)", line.toText());
    }

    public void testArray2Line() {
        LineString line = FoolStyleFeatureConverter.array2Line(new double[]{1, 2, 3, 4});
        Assert.assertEquals("LINESTRING (1 2, 3 4)", line.toText());
    }

    public void testLists2Line() {
        LineString line = FoolStyleFeatureConverter.lists2Line(List.of(new double[]{1, 2}, new double[]{3, 4}));
        Assert.assertEquals("LINESTRING (1 2, 3 4)", line.toText());
    }

    public void testArrays2Line() {
        LineString line = FoolStyleFeatureConverter.arrays2Line(new double[][]{new double[]{1, 2}, new double[]{3, 4}});
        Assert.assertEquals("LINESTRING (1 2, 3 4)", line.toText());
    }

    public void testStr2Line() {
        LineString line = FoolStyleFeatureConverter.str2Line("1,2;3,4",",",";");
        Assert.assertEquals("LINESTRING (1 2, 3 4)", line.toText());
    }

    public void testList2Polygon() {
        Polygon polygon = FoolStyleFeatureConverter.list2Polygon(List.of(1d, 2d, 3d, 4d, 6d, 6d));
        Assert.assertEquals("POLYGON ((1 2, 3 4, 6 6, 1 2))", polygon.toText());
    }

    public void testArray2Polygon() {
        Polygon polygon = FoolStyleFeatureConverter.array2Polygon(new double[]{1d, 2d, 3d, 4d, 6d, 6d});
        Assert.assertEquals("POLYGON ((1 2, 3 4, 6 6, 1 2))", polygon.toText());
    }

    public void testLists2Polygon() {
        Polygon polygon = FoolStyleFeatureConverter.lists2Polygon(List.of(new double[]{1,2},new double[]{3,4},new double[]{6,6}));
        Assert.assertEquals("POLYGON ((1 2, 3 4, 6 6, 1 2))", polygon.toText());
    }

    public void testArrays2Polygon() {
        Polygon polygon = FoolStyleFeatureConverter.arrays2Polygon(new double[][]{new double[]{1,2},new double[]{3,4},new double[]{6,6}});
        Assert.assertEquals("POLYGON ((1 2, 3 4, 6 6, 1 2))", polygon.toText());
    }

    public void testStr2Polygon() {
        Polygon polygon = FoolStyleFeatureConverter.str2Polygon("1 2,3 4,6 6, 1 2"," ",",");
        Assert.assertEquals("POLYGON ((1 2, 3 4, 6 6, 1 2))", polygon.toText());
    }
}
