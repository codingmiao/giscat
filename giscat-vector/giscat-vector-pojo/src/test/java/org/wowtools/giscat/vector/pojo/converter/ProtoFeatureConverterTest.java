package org.wowtools.giscat.vector.pojo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.util.SampleData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProtoFeatureConverterTest {
    private static final Random random = new Random(233);

    @org.junit.Test
    public void testGeometrys() throws Exception {
        testGeometry("POINT (30 10)");
        testGeometry("LINESTRING (30 10, 10 30, 40 40)");
        testGeometry("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))");
        testGeometry("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10), (11 11, 12 11, 12 12, 11 11), (17 17, 18 17, 18 18, 17 17))");
        testGeometry("MULTIPOINT ((10 40), (40 30), (20 20), (30 10))");
        testGeometry("MULTIPOINT ((10 40))");
        testGeometry("MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10), (41 41, 31 31, 41 21, 31 11))");
        testGeometry("MULTILINESTRING ((10 10, 20 20, 10 40))");
        testGeometry("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), ((15 5, 40 10, 10 20, 5 10, 15 5)))");
        testGeometry("MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)))");
        testGeometry("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),((41 41, 21 46, 46 31, 41 41)), ((20 35, 10 30, 10 10, 30 5, 45 20, 20 35), (30 20, 20 15, 20 25, 30 20)))");
        testGeometry("GEOMETRYCOLLECTION (POINT (40 10), LINESTRING (10 10, 20 20, 10 40), POLYGON ((40 40, 20 45, 45 30, 40 40)))");
        testGeometry("GEOMETRYCOLLECTION (POINT (40 10))");
        testGeometry("GEOMETRYCOLLECTION (" +
                "POINT (40 10)," +
                "LINESTRING (30 10, 10 30, 40 40)," +
                "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))," +
                "MULTIPOINT ((10 40), (40 30), (20 20), (30 10))," +
                "MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10), (41 41, 31 31, 41 21, 31 11))," +
                "MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), ((15 5, 40 10, 10 20, 5 10, 15 5)))," +
                "GEOMETRYCOLLECTION (POINT (40 10))" +
                ")");

    }

    private void testGeometry(String wkt) throws Exception {
        Geometry geometry = new WKTReader().read(wkt);
        Geometry geometry1;
        System.out.println("--------------------testGeometry");
        System.out.println(geometry);
        System.out.println("------ 2d");
        byte[] bytes = ProtoFeatureConverter.geometry2Proto(geometry);
        System.out.println("ProtoFeature len:" + bytes.length);
        System.out.println("wkb len:" + new WKBWriter(2, false).write(geometry).length);
        geometry1 = ProtoFeatureConverter.proto2Geometry(bytes, SampleData.geometryFactory);
        System.out.println(geometry1);
        Assert.assertTrue(geometry.equalsNorm(geometry1));
        System.out.println("------ 3d");
        for (Coordinate coordinate : geometry.getCoordinates()) {
            coordinate.setZ(random.nextDouble() * 10);
        }
        bytes = ProtoFeatureConverter.geometry2Proto(geometry);
        System.out.println("ProtoFeature len:" + bytes.length);
        System.out.println("wkb len:" + new WKBWriter(3, false).write(geometry).length);
        geometry1 = ProtoFeatureConverter.proto2Geometry(bytes, SampleData.geometryFactory);
        System.out.println(geometry1);
        Assert.assertTrue(geometry.equalsNorm(geometry1));
    }

    @org.junit.Test
    public void testPropertiess() throws Exception {
        testProperties(Map.of("name", "hello", "id", "100"));
        testProperties(Map.of("name", "hello", "id", 1, "bytes", new byte[]{1, 2, 3}));
        testProperties(Map.of(
                "double", 1.5d,
                "floatKey", 3.5f,
                "sint32Key", 123,
                "sint64", 1234L,
                "boolKey", true,
                "stringKey", "hello",
                "bytesKey", new byte[]{1, 2, 3},
                "subKey", Map.of("subK1", "sub1")));
        testProperties(
                Map.of("k1", 1, "k2", "sss", "k3", 1L),
                Map.of("k1", "hello", "k2", 1, "k3", Map.of("subK1", "sub1")),
                Map.of(),
                Map.of(),
                Map.of("k1", Long.MAX_VALUE, "k2", "测试", "k4", Double.MAX_VALUE)
        );
        testProperties(Map.of("a", Map.of("a", Map.of("a", Map.of("a", Map.of("a", "a"))))));
        testProperties(Map.of("a", List.of("1", 2, true,"x",Map.of("xx",111)), "b", "xxx"));
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private void testProperties(Map<String, Object>... propertiesArr) {
        FeatureCollection featureCollection = new FeatureCollection();
        ArrayList<Feature> features = new ArrayList<>(propertiesArr.length);
        for (Map<String, Object> properties : propertiesArr) {
            Feature feature = new Feature();
            feature.setGeometry(null);
            feature.setProperties(properties);
            features.add(feature);
        }
        featureCollection.setFeatures(features);
        byte[] bytes = ProtoFeatureConverter.featureCollection2Proto(featureCollection);
        byte[] bytesJson;
        try {
            bytesJson = mapper.writeValueAsString(featureCollection).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("pbf size:" + bytes.length + "\t json size:" + bytesJson.length);
        FeatureCollection featureCollection1 = ProtoFeatureConverter.proto2featureCollection(bytes, SampleData.geometryFactory);

        for (int i = 0; i < features.size(); i++) {
            Map<String, Object> properties = features.get(i).getProperties();
            Map<String, Object> properties1 = featureCollection1.getFeatures().get(i).getProperties();
            for (String key : properties.keySet()) {
                testMapEquals(key, properties.get(key), properties1.get(key));
            }
        }
    }

    private static void testMapEquals(String key, Object p1, Object p2) {
        if (p1 instanceof Map) {
            Map<String, Object> map1 = (Map<String, Object>) p1;
            Map<String, Object> map2 = (Map<String, Object>) p2;
            for (String subKey : map2.keySet()) {
                testMapEquals(subKey, map1.get(subKey), map2.get(subKey));
            }
        } else if (p1 instanceof byte[]) {
            byte[] bts1 = (byte[]) p1;
            byte[] bts2 = (byte[]) p2;
            for (int i = 0; i < bts1.length; i++) {
                if (bts1[i] != bts2[i]) {
                    throw new RuntimeException(key + " not equals: " + p1 + "\t" + p2);
                }
            }
        } else if (p1 instanceof List) {
            List<Object> list1 = (List<Object>) p1;
            List<Object> list2 = (List<Object>) p2;
            for (int i = 0; i < list1.size(); i++) {
                testMapEquals("list", list1.get(i), list2.get(i));
            }
        } else {
            if (!p1.equals(p2)) {
                throw new RuntimeException(key + " not equals: " + p1 + "\t" + p2);
            }
        }
    }
}
