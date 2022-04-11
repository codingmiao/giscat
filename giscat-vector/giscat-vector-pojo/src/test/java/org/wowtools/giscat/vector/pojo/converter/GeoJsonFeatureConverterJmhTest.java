package org.wowtools.giscat.vector.pojo.converter;


import org.json.JSONObject;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.wololo.geojson.GeoJSON;
import org.wololo.jts2geojson.GeoJSONWriter;
import org.wowtools.giscat.vector.pojo.util.SampleData;

import java.util.concurrent.TimeUnit;


/**
 * 与https://github.com/bjornharrtell/jts2geojson的jmh性能对比测试
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 3)
@Threads(1)
@Fork(2)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GeoJsonFeatureConverterJmhTest {

    GeoJSONWriter writer = new GeoJSONWriter();
    @Benchmark
    public void testBjornharrtell(Blackhole blackhole) throws Exception {
        GeoJSON json = writer.write(SampleData.point);
        String jsonstring = json.toString();
        blackhole.consume(jsonstring);
    }

    @Benchmark
    public void testGeoJsonFeature(Blackhole blackhole) throws Exception {
        JSONObject json = GeoJsonFeatureConverter.geometry2GeoJson(SampleData.point);
        String jsonstring = json.toString();
        blackhole.consume(jsonstring);
    }

    public static void main(String[] args) throws Exception {
        Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
        GeoJsonFeatureConverterJmhTest t = new GeoJsonFeatureConverterJmhTest();
        while (true) {
            t.testGeoJsonFeature(blackhole);
        }


//        Options opt = new OptionsBuilder()
//                .include(GeoJsonFeatureConverterJmhTest.class.getSimpleName())
//                .result("jmhResult.json")
//                .resultFormat(ResultFormatType.JSON).build();
//        new Runner(opt).run();
    }

}
