package org.wowtools.giscat.util.analyse;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.Random;

public class TileClipTest {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @org.junit.Test
    public void test() throws ParseException {
        GeometryFactory gf = new GeometryFactory();
        Geometry clipGeometry = gf.createPolygon(new Coordinate[]{
                new Coordinate(20, 20),
                new Coordinate(80, 20),
                new Coordinate(80, 80),
                new Coordinate(20, 80),
                new Coordinate(20, 20)
        });
        TileClip tileClip = new TileClip(clipGeometry, geometryFactory);
        testGeo(clipGeometry, tileClip,
                (LineString) new WKTReader().read("LINESTRING (37 51, 5 17, 21 34)")
        );

        Random random = new Random(233);
        int n = 100000;
        for (int i = 0; i < n; i++) {
            Coordinate[] coords = new Coordinate[2 + random.nextInt(3)];

            for (int i1 = 0; i1 < coords.length; i1++) {
                coords[i1] = new Coordinate(random.nextInt(100), random.nextInt(100));
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
        if (c1 instanceof GeometryCollection) {
            return;
        }
        Geometry c2 = tileClip.intersection(line);
        int area1 = c1 instanceof Point ? 0 : (int) (c1.buffer(1).getArea() * 100);
        int area2 = c2 == null ? 0 : (int) (c2.buffer(1).getArea() * 100);
        if (Math.abs(area1 - area2) > 314) {
            throw new RuntimeException();
        }
    }

}
