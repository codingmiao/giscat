package org.wowtools.giscat.vector.pojo;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

/**
 * 要素 包含properties和geometry
 *
 * @author liuyu
 * @date 2022/3/15
 */
public class Feature {
    private Geometry geometry;
    private Map<String, Object> properties;

    public Feature(Geometry geometry, Map<String, Object> properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    public Feature(Geometry geometry) {
        this.geometry = geometry;
    }

    public Feature() {
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
