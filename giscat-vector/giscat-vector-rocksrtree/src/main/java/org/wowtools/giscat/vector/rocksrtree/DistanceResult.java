package org.wowtools.giscat.vector.rocksrtree;


import org.wowtools.giscat.vector.pojo.Feature;

/**
 * 距离查询结果
 *
 * @author liuyu
 * @date 2020/6/12
 */
public class DistanceResult {
    protected final double dist;
    protected final Feature feature;

    public DistanceResult(double dist, Feature feature) {
        this.dist = dist;
        this.feature = feature;
    }

    /**
     * 获得输入点到feature的距离
     * @return dist
     */
    public double getDist() {
        return dist;
    }

    /**
     * 获得feature
     * @return feature
     */
    public Feature getFeature() {
        return feature;
    }
}
