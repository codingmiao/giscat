package org.wowtools.giscat.vector.pojo;

import java.util.List;

/**
 * FeatureCollection
 * @author liuyu
 * @date 2022/3/15
 */
public class FeatureCollection {
    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
