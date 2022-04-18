package org.wowtools.giscat.vector.pojo.converter;


import org.locationtech.jts.geom.Geometry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.GeoJsonObject;
import org.wowtools.giscat.vector.pojo.util.SampleData;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 与https://github.com/bjornharrtell/jts2geojson的jmh性能对比测试
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 3)
@Threads(1)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GeoJsonFeatureConverterJmhTest {

    GeoJSONWriter writer = new GeoJSONWriter();
    GeoJSONReader reader = new GeoJSONReader();

    /////////////to json
    @Benchmark
    public void bjornharrtellParseJson(Blackhole blackhole) throws Exception {
        org.wololo.geojson.FeatureCollection geoJsonFeatureCollection = (org.wololo.geojson.FeatureCollection) GeoJSONFactory.create(SampleData.strFeatureCollection1);
        for (Feature geoJsonFeature : geoJsonFeatureCollection.getFeatures()) {
            Geometry geometry = reader.read(geoJsonFeature.getGeometry());
            blackhole.consume(geometry);
            blackhole.consume(geoJsonFeature.getProperties());
        }
    }

    @Benchmark
    public void geoJsonFeatureParseJson(Blackhole blackhole) throws Exception {
        FeatureCollection featureCollection = GeoJsonFeatureConverter.fromGeoJsonFeatureCollection(SampleData.strFeatureCollection1, SampleData.geometryFactory);
        for (org.wowtools.giscat.vector.pojo.Feature feature : featureCollection.getFeatures()) {
            Geometry geometry = feature.getGeometry();
            blackhole.consume(geometry);
            blackhole.consume(feature.getProperties());
        }
    }

    //////////////parse json
    private final Geometry[] geometryArr = new Geometry[]{
            SampleData.point,
            SampleData.lineString,
//            SampleData.polygon1,//bjornharrtell 解析报错
            SampleData.multiPoint,
    };

    private final Map<String, Object>[] propertiesArr = new Map[]{
            Map.of("sada", 1, "saidoje", true, "oisejoi", "saoidj"),
            Map.of("sada", 1, "saidoje", true, "oisejoi", "saoidj"),
            Map.of("sada", 1, "saidoje", true, "oisejoi", "saoidj"),
            Map.of("sada", 1, "saidoje", true, "oisejoi", "saoidj"),
    };

    @Benchmark
    public void bjornharrtellToJson(Blackhole blackhole) throws Exception {
        ArrayList<Feature> features = new ArrayList<>(geometryArr.length);
        for (int i = 0; i < geometryArr.length; i++) {
            org.wololo.geojson.Geometry jsonGeometry = writer.write(geometryArr[i]);
            Map<String, Object> properties = propertiesArr[i];
            features.add(new Feature(jsonGeometry, properties));
        }
        GeoJSON json = writer.write(features);
        blackhole.consume(json.toString());
    }

    @Benchmark
    public void geoJsonFeatureToJson(Blackhole blackhole) throws Exception {
        GeoJsonObject.Feature[] features = new GeoJsonObject.Feature[geometryArr.length];
        for (int i = 0; i < geometryArr.length; i++) {
            GeoJsonObject.Feature feature = new GeoJsonObject.Feature();
            GeoJsonObject.Geometry geometry = GeoJsonFeatureConverter.geometry2GeoJson(geometryArr[i]);
            feature.setGeometry(geometry);
            feature.setProperties(propertiesArr[i]);
            features[i] = feature;
        }
        GeoJsonObject.FeatureCollection featureCollection = new GeoJsonObject.FeatureCollection();
        featureCollection.setFeatures(features);
        blackhole.consume(featureCollection.toGeoJsonString());
    }

    public static void main(String[] args) throws Exception {
//        Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
//        GeoJsonFeatureConverterJmhTest t = new GeoJsonFeatureConverterJmhTest();
//        while (true) {
//            t.testGeoJsonFeature(blackhole);
//        }


        Options opt = new OptionsBuilder()
                .include(GeoJsonFeatureConverterJmhTest.class.getSimpleName())
                .result("jmhResult.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }

}
