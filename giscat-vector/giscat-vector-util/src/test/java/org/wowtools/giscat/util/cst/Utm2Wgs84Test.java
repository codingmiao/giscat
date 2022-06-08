package org.wowtools.giscat.util.cst;


import org.junit.Assert;

public class Utm2Wgs84Test {
    @org.junit.Test
    public void testUtm2wgs84() {
        String strUtm = "18 G 615471.66 4789269.78";
        LonLat lonlat = Utm2Wgs84.utm2wgs84(strUtm);
        Assert.assertEquals(-73.48, lonlat.getLongitude(), 0.00001);
        Assert.assertEquals(-47.04, lonlat.getLatitude(), 0.00001);

        strUtm = "18G 615471.66 4789269.78";
        lonlat = Utm2Wgs84.utm2wgs84(strUtm);
        Assert.assertEquals(-73.48, lonlat.getLongitude(), 0.00001);
        Assert.assertEquals(-47.04, lonlat.getLatitude(), 0.00001);

        strUtm = "18g 615471.66 4789269.78";
        lonlat = Utm2Wgs84.utm2wgs84(strUtm);
        Assert.assertEquals(-73.48, lonlat.getLongitude(), 0.00001);
        Assert.assertEquals(-47.04, lonlat.getLatitude(), 0.00001);
    }

    @org.junit.Test
    public void testTestUtm2wgs84() {
        Utm2Wgs84.UtmCoord utm = Utm2Wgs84.wgs842utm(-73.48, -47.04);
        Assert.assertEquals(18, utm.getZone());
        Assert.assertEquals('G', utm.getLetter());
        Assert.assertEquals(615471.66, utm.getEasting(), 0.00001);
        Assert.assertEquals(4789269.78, utm.getNorthing(), 0.00001);
    }

}
