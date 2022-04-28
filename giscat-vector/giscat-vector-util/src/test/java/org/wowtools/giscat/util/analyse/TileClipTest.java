package org.wowtools.giscat.util.analyse;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.Random;

public class TileClipTest {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @org.junit.Test
    public void test() throws ParseException {
        GeometryFactory gf = new GeometryFactory();
        Geometry clipGeometry = gf.createPolygon(new Coordinate[]{
                new Coordinate(0.2, 0.2),
                new Coordinate(0.8, 0.2),
                new Coordinate(0.8, 0.8),
                new Coordinate(0.2, 0.8),
                new Coordinate(0.2, 0.2)
        });
        TileClip tileClip = new TileClip(clipGeometry, geometryFactory);
        testGeo(clipGeometry, tileClip,
                (LineString) new WKTReader().read("LINESTRING (0.3148974120930301 0.0370910468762162, 0.4638855977254692 0.1006166810782042, 0.755127868609657 0.9085917793308416, 0.4545110586050796 0.4253592058070585)")
        );

        Random random = new Random(233);
        int n = 1000;
        for (int i = 0; i < n; i++) {
            Coordinate[] coords = new Coordinate[2 + random.nextInt(50)];

            for (int i1 = 0; i1 < coords.length; i1++) {
                coords[i1] = new Coordinate(random.nextDouble(), random.nextDouble());
            }
            LineString line = gf.createLineString(coords);
            try {
                testGeo(clipGeometry, tileClip, line);
            } catch (Exception e) {
                System.out.println(line.toText());
                throw new RuntimeException(e);
            }
        }
    }

    private void testGeo(Geometry clipGeometry, TileClip tileClip, LineString line) {
        Geometry c1 = clipGeometry.intersection(line);
        Geometry c2 = tileClip.intersection(line);
        int len1 = (int) (100000 * c1.getLength());
        int len2 = c2 == null ? 0 : (int) (100000 * c2.getLength());
        if (len1 != len2) {
            throw new RuntimeException();
        }
    }

}
