package org.wowtools.giscat.vector.mvt;

import org.wowtools.giscat.util.cst.Tile2Wgs84;

import static org.junit.Assert.*;

public class MvtCoordinateConvertorTest {

    @org.junit.Test
    public void testWgs842mvt() {
        int z = 12, x = 3223, y = 1774;
        MvtCoordinateConvertor mvtCoordinateConvertor = new MvtCoordinateConvertor(z, x, y);
        System.out.println("瓦片wgs84范围:[" +
                Tile2Wgs84.tileX2lon(x, z) + ", " +
                Tile2Wgs84.tileY2lat(y + 1, z) + ", " +
                Tile2Wgs84.tileX2lon(x + 1, z) + ", " +
                Tile2Wgs84.tileY2lat(y, z) +
                "]");
        double wgs84X = 103.31;
        int mvtX = mvtCoordinateConvertor.wgs84X2mvt(wgs84X);
        assertEquals(1795, mvtX);
        double wgs84Y = 23.35;
        int mvtY = mvtCoordinateConvertor.wgs84Y2mvt(wgs84Y);
        assertEquals(2679, mvtY);

        wgs84X = 103.41;
        mvtX = mvtCoordinateConvertor.wgs84X2mvt(wgs84X);
        assertEquals(6455, mvtX);
        wgs84Y = 23.41;
        mvtY = mvtCoordinateConvertor.wgs84Y2mvt(wgs84Y);
        assertEquals(-366, mvtY);

    }

    @org.junit.Test
    public void testMvt2Wgs84(){
        System.out.println(Math.log(2.3));
        System.out.println(Math.exp(0.8329091229351039));


        int z = 12, x = 3223, y = 1774;
        MvtCoordinateConvertor mvtCoordinateConvertor = new MvtCoordinateConvertor(z, x, y);

        assertEquals(103.31, mvtCoordinateConvertor.mvtX2wgs84(1795),0.0001);
        assertEquals(103.41, mvtCoordinateConvertor.mvtX2wgs84(6455),0.0001);

        assertEquals(23.35, mvtCoordinateConvertor.mvtY2wgs84(2679),0.0001);
        assertEquals(23.41, mvtCoordinateConvertor.mvtY2wgs84(-366),0.0001);

    }

}
