package org.wowtools.giscat.vector.pojo;

import java.util.Collection;

/**
 * FeatureCollection
 * @author liuyu
 * @date 2022/3/15
 */
public class FeatureCollection {
    private Collection<Feature> features;

    public Collection<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(Collection<Feature> features) {
        this.features = features;
    }
}
