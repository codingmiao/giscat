package org.wowtools.giscat.vector.util.cst;

import lombok.Getter;
import lombok.Setter;

/**
 * 经纬度对象
 *
 * @author liuyu
 * @date 2022/6/7
 */
@Setter
@Getter
public class LonLat {
    private double latitude;
    private double longitude;

    public LonLat(double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LonLat() {

    }
}
