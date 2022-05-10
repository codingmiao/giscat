package org.wowtools.giscat.vector.mvt;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.Map;

public class MvtBuilderTest {
    @org.junit.Test
    public void test() {
        GeometryFactory gf = new GeometryFactory();
        int z = 12, x = 3223, y = 1774;
        MvtBuilder mvtBuilder = new MvtBuilder(z, x, y, gf);
        MvtLayer mvtLayer = mvtBuilder.getOrCreateLayer("testLayer");
        mvtLayer.addFeature(new Feature(
                gf.createPoint(new Coordinate(103.31, 23.35)),
                Map.of("name", "233")
        ));

        byte[] bytes = mvtBuilder.toBytes();

        MvtParser.MvtFeatureLayer[] mvtFeatureLayers = MvtParser.parse2Wgs84Coords(z, x, y, bytes, gf);
        System.out.println(mvtFeatureLayers[0].getFeatures()[0].getGeometry());
    }
}
