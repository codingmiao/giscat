package org.wowtools.giscat.vector.pojo.converter;


import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.GeoJsonObject;
import org.wowtools.giscat.vector.pojo.util.SampleData;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GeoJsonFeatureConverterTest {

    //将geometry对象转为GeoJson
    @org.junit.Test
    public void testGeometry2GeoJson() {

        GeoJsonObject.Geometry geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.point);
        Assert.assertEquals(SampleData.strPoint
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.lineString);
        Assert.assertEquals(SampleData.strLineString
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.polygon1);
        Assert.assertEquals(SampleData.strPolygon1
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.polygon2);
        Assert.assertEquals(SampleData.strPolygon2
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.polygon3);
        Assert.assertEquals(SampleData.strPolygon3
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.multiPoint);
        Assert.assertEquals(SampleData.strMultiPoint
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.multiLineString);
        Assert.assertEquals(SampleData.strMultiLineString
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.multiPolygon1);
        Assert.assertEquals(SampleData.strMultiPolygon1
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.multiPolygon2);
        Assert.assertEquals(SampleData.strMultiPolygon2
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());

        geoJson = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.geometryCollection);
        Assert.assertEquals(SampleData.strGeometryCollection
                        .replace(" ", "")
                        .replace("\n", "")
                        .replace("\t", ""),
                geoJson.toGeoJsonString());
    }

    //属性测试
    @org.junit.Test
    public void testProperties2GeoJson() {
        Feature feature = new Feature();
        feature.setProperties(Map.of("name", "hello"));
        GeoJsonObject.Feature geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals("{\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}", geoJson.toGeoJsonString());

        feature.setProperties(null);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals("{\"type\":\"Feature\"}", geoJson.toGeoJsonString());

        feature.setProperties(Map.of());
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals("{\"type\":\"Feature\",\"properties\":{}}", geoJson.toGeoJsonString());
    }

    //属性测试
    @org.junit.Test
    public void testGeoJson2Properties() {
        Feature feature = GeoJsonFeatureConverter.fromGeoJsonFeature("{\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}", SampleData.geometryFactory);
        Map<String, String> assertMap = Map.of("name", "hello");
        Assert.assertEquals(assertMap, feature.getProperties());

        feature = GeoJsonFeatureConverter.fromGeoJsonFeature("{\"type\":\"Feature\"}", SampleData.geometryFactory);
        assertMap = null;
        Assert.assertEquals(assertMap, feature.getProperties());

        feature = GeoJsonFeatureConverter.fromGeoJsonFeature("{\"type\":\"Feature\",\"properties\":{}}", SampleData.geometryFactory);
        assertMap = Map.of();
        Assert.assertEquals(assertMap, feature.getProperties());
    }

    //将GeoJson对象转为Geometry
    @org.junit.Test
    public void testGeoJson2Geometry() {
        Geometry geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strPoint, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.point.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strLineString, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.lineString.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strPolygon1, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.polygon1.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strPolygon2, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.polygon2.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strMultiPoint, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiPoint.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strMultiLineString, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiLineString.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strMultiPolygon1, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiPolygon1.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strMultiPolygon2, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiPolygon2.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.strGeometryCollection, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.geometryCollection.toText(), geometry.toText());
    }

    //将featureCollection对象转为GeoJson
    @org.junit.Test
    public void testFeatureCollection2GeoJson() {
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(List.of(
                new Feature(SampleData.point, Map.of("id", 1)),
                new Feature(SampleData.lineString, Map.of("name", "hello"))
        ));
        GeoJsonObject.FeatureCollection geoJson = GeoJsonFeatureConverter.toGeoJson(featureCollection);
        Assert.assertEquals("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[30.0,10.0]},\"properties\":{\"id\":1}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[30.0,10.0],[10.0,30.0],[40.0,40.0]]},\"properties\":{\"name\":\"hello\"}}]}",
                geoJson.toGeoJsonString()
        );
    }

    @org.junit.Test
    public void testFeatureCollection2GeoJsonWithHeader() {
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setHeaders(Map.of("name", List.of(1, "2")));
        featureCollection.setFeatures(List.of(
                new Feature(SampleData.point, Map.of("id", 1)),
                new Feature(SampleData.lineString, Map.of("name", "hello"))
        ));
        GeoJsonObject.FeatureCollection geoJson = GeoJsonFeatureConverter.toGeoJson(featureCollection);
        Assert.assertEquals("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[30.0,10.0]},\"properties\":{\"id\":1}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[30.0,10.0],[10.0,30.0],[40.0,40.0]]},\"properties\":{\"name\":\"hello\"}}],\"headers\":{\"name\":[1,\"2\"]}}",
                geoJson.toGeoJsonString()
        );
    }

    //将GeoJson对象转为Feature
    @org.junit.Test
    public void testGeoJson2FeatureCollection() {
        FeatureCollection featureCollection = GeoJsonFeatureConverter.fromGeoJsonFeatureCollection("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[30.0,10.0]},\"properties\":{\"id\":1}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[30.0,10.0],[10.0,30.0],[40.0,40.0]]},\"properties\":{\"name\":\"hello\"}}]}", SampleData.geometryFactory);
        Iterator<Feature> iterator = featureCollection.getFeatures().iterator();

        Feature feature1 = iterator.next();
        Assert.assertEquals(SampleData.point.toText(), feature1.getGeometry().toText());
        Assert.assertEquals(Map.of("id", 1), feature1.getProperties());

        Feature feature2 = iterator.next();
        Assert.assertEquals(SampleData.lineString.toText(), feature2.getGeometry().toText());
        Assert.assertEquals(Map.of("name", "hello"), feature2.getProperties());

    }

}
