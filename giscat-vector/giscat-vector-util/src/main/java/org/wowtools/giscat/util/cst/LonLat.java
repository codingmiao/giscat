package org.wowtools.giscat.util.cst;

/**
 * 经纬度对象
 *
 * @author liuyu
 * @date 2022/6/7
 */
public class LonLat {
    private double latitude;
    private double longitude;

    public LonLat(double longitude,double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LonLat() {

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
