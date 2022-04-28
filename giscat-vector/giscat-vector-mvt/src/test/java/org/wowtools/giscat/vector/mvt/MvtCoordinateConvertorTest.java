package org.wowtools.giscat.vector.mvt;

import static org.junit.Assert.*;

public class MvtCoordinateConvertorTest {

    @org.junit.Test
    public void testWgs842mvt() {
        int z = 12, x = 3223, y = 1774;
        MvtCoordinateConvertor mvtCoordinateConvertor = new MvtCoordinateConvertor(z, x, y);
        double wgs84X = 102.7;
        int mvtX = mvtCoordinateConvertor.wgs84X2mvt(wgs84X);
        assertEquals(-26633, mvtX);
        double wgs84Y = 25.4;
        int mvtY = mvtCoordinateConvertor.wgs84Y2mvt(wgs84Y);
        assertEquals(-102215, mvtY);

    }

}
