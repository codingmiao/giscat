package org.wowtools.giscat.vector.pojo.converter;

import org.locationtech.jts.geom.GeometryFactory;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.GeoJsonObject;

/**
 * @author liuyu
 * @date 2022/4/1
 */
public class Test1 {


    public static void main(String[] args) throws Exception {
        String strGeoJson = "{\"features\":[{\"geometry\":{\"coordinates\":[30,10],\"type\":\"Point\"},\"type\":\"Feature\",\"properties\":{\"id\":1}},{\"geometry\":{\"coordinates\":[[30,10],[10,30],[40,40]],\"type\":\"LineString\"},\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}],\"type\":\"FeatureCollection\"}";
        GeometryFactory geometryFactory = new GeometryFactory();// jts GeometryFactory
        FeatureCollection featureCollection = GeoJsonFeatureConverter.fromGeoJsonFeatureCollection(strGeoJson, geometryFactory);
        for (Feature feature : featureCollection.getFeatures()) {
            System.out.println(feature.getGeometry());//POINT (30 10)
            System.out.println(feature.getProperties());//{name=hello ...}
        }


        GeoJsonObject.FeatureCollection geoJson = GeoJsonFeatureConverter.toGeoJson(featureCollection);
        System.out.println(geoJson.toGeoJsonString());
    }
}
