package org.wowtools.giscat.vector.pojo.converter;

import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.wowtools.giscat.vector.pojo.util.SampleData;

public class ProtoFeatureConverterTest {
    @org.junit.Test
    public void testGeometrys() throws Exception {

        testGeometry("POINT (30 10)");
        testGeometry("LINESTRING (30 10, 10 30, 40 40)");
        testGeometry("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))");
        testGeometry("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10), (11 11, 12 11, 12 12, 11 11), (17 17, 18 17, 18 18, 17 17))");
        testGeometry("MULTIPOINT ((10 40), (40 30), (20 20), (30 10))");
        testGeometry("MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10), (41 41, 31 31, 41 21, 31 11))");
        testGeometry("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), ((15 5, 40 10, 10 20, 5 10, 15 5)))");
        testGeometry("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),((41 41, 21 46, 46 31, 41 41)), ((20 35, 10 30, 10 10, 30 5, 45 20, 20 35), (30 20, 20 15, 20 25, 30 20)))");
        testGeometry("GEOMETRYCOLLECTION (POINT (40 10), LINESTRING (10 10, 20 20, 10 40), POLYGON ((40 40, 20 45, 45 30, 40 40)))");

    }

    private void testGeometry(String wkt) throws Exception{
        Geometry geometry = new WKTReader().read(wkt);
        System.out.println("--------------------testGeometry \n" + geometry);
        byte[] bytes = ProtoFeatureConverter.geometry2Proto(geometry);
        System.out.println("ProtoFeature len:" + bytes.length);

        System.out.println("wkb len:" + new WKBWriter(2, false).write(geometry).length);

        Geometry geometry1 = ProtoFeatureConverter.proto2Geometry(bytes, SampleData.geometryFactory);
        System.out.println(geometry1);
        Assert.assertTrue(geometry.equalsNorm(geometry1));
    }
}
