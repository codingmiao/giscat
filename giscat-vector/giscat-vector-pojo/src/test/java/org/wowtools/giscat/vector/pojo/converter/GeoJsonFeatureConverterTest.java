package org.wowtools.giscat.vector.pojo.converter;


import org.json.JSONObject;
import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.proto.ProtoFeature;
import org.wowtools.giscat.vector.pojo.util.SampleData;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GeoJsonFeatureConverterTest {


    @org.junit.Test
    public void testProperties2GeoJson() {
        Feature feature = new Feature();
        feature.setProperties(Map.of("name", "hello"));
        JSONObject geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        JSONObject assertGeoJson = new JSONObject("{\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}");
        Assert.assertEquals(geoJson.toString(), assertGeoJson.toString());

        feature.setProperties(null);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        assertGeoJson = new JSONObject("{\"type\":\"Feature\"}");
        Assert.assertEquals(geoJson.toString(), assertGeoJson.toString());
    }

    @org.junit.Test
    public void testGeoJson2Properties() {
        JSONObject geoJson = new JSONObject("{\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}");
        Feature feature = GeoJsonFeatureConverter.fromGeoJsonFeature(geoJson, SampleData.geometryFactory);
        Map<String, String> assertMap = Map.of("name", "hello");
        Assert.assertEquals(assertMap, feature.getProperties());

        geoJson = new JSONObject("{\"type\":\"Feature\"}");
        feature = GeoJsonFeatureConverter.fromGeoJsonFeature(geoJson, SampleData.geometryFactory);
        assertMap = null;
        Assert.assertEquals(assertMap, feature.getProperties());
    }

    @org.junit.Test
    public void testGeometry2GeoJson() {
        Feature feature = new Feature();

        feature.setGeometry(SampleData.point);
        JSONObject geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.pointGeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.lineString);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.lineStringGeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.polygon1);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.polygon1GeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.polygon2);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.polygon2GeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.multiPoint);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.multiPointGeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.multiLineString);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.multiLineStringGeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.multiPolygon1);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.multiPolygon1GeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.multiPolygon2);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.multiPolygon2GeoJson.toString(), geoJson.getJSONObject("geometry").toString());

        feature.setGeometry(SampleData.geometryCollection);
        geoJson = GeoJsonFeatureConverter.toGeoJson(feature);
        Assert.assertEquals(SampleData.geometryCollectionGeoJson.toString(), geoJson.getJSONObject("geometry").toString());
    }

    @org.junit.Test
    public void testGeoJson2Geometry() {
        Geometry geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.pointGeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.point.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.lineStringGeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.lineString.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.polygon1GeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.polygon1.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.polygon2GeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.polygon2.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.multiPointGeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiPoint.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.multiLineStringGeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiLineString.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.multiPolygon1GeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiPolygon1.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.multiPolygon2GeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.multiPolygon2.toText(), geometry.toText());

        geometry = GeoJsonFeatureConverter.geoJson2Geometry(SampleData.geometryCollectionGeoJson, SampleData.geometryFactory);
        Assert.assertEquals(SampleData.geometryCollection.toText(), geometry.toText());
    }

    @org.junit.Test
    public void testFeatureCollection2GeoJson() {
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(List.of(
                new Feature(SampleData.point, Map.of("id", 1)),
                new Feature(SampleData.lineString, Map.of("name", "hello"))
        ));
        JSONObject geoJson = GeoJsonFeatureConverter.toGeoJson(featureCollection);
        Assert.assertEquals("{\"features\":[{\"geometry\":{\"coordinates\":[30,10],\"type\":\"Point\"},\"type\":\"Feature\",\"properties\":{\"id\":1}},{\"geometry\":{\"coordinates\":[[30,10],[10,30],[40,40]],\"type\":\"LineString\"},\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}],\"type\":\"FeatureCollection\"}",
                geoJson.toString()
        );
    }

    @org.junit.Test
    public void testGeoJson2FeatureCollection(){
        JSONObject geoJson = new JSONObject("{\"features\":[{\"geometry\":{\"coordinates\":[30,10],\"type\":\"Point\"},\"type\":\"Feature\",\"properties\":{\"id\":1}},{\"geometry\":{\"coordinates\":[[30,10],[10,30],[40,40]],\"type\":\"LineString\"},\"type\":\"Feature\",\"properties\":{\"name\":\"hello\"}}],\"type\":\"FeatureCollection\"}");
        FeatureCollection featureCollection = GeoJsonFeatureConverter.fromGeoJsonFeatureCollection(geoJson, SampleData.geometryFactory);
        Iterator<Feature> iterator = featureCollection.getFeatures().iterator();

        Feature feature1 = iterator.next();
        Assert.assertEquals(SampleData.point.toText(),feature1.getGeometry().toText());
        Assert.assertEquals(Map.of("id", 1),feature1.getProperties());

        Feature feature2 = iterator.next();
        Assert.assertEquals(SampleData.lineString.toText(),feature2.getGeometry().toText());
        Assert.assertEquals(Map.of("name", "hello"),feature2.getProperties());

    }

}
