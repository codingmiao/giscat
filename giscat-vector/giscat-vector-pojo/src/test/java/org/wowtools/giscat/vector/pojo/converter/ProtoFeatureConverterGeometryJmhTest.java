package org.wowtools.giscat.vector.pojo.converter;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * ProtoFeatureConverter转换bytes与wkb转换bytes的jmh性能测试
 *
 * @author liuyu
 * @date 2022/4/1
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 3)
@Threads(1)
@Fork(2)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ProtoFeatureConverterGeometryJmhTest {

    private final Geometry[] geometries;

    {
        try {
            WKTReader wktReader = new WKTReader();
            geometries = new Geometry[]{
                    wktReader.read("POINT (30 10)"),
                    wktReader.read("LINESTRING (30 10, 10 30, 40 40)"),
                    wktReader.read("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))"),
                    wktReader.read("MULTIPOINT ((10 40), (40 30), (20 20), (30 10))"),
                    wktReader.read("MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10), (41 41, 31 31, 41 21, 31 11))"),
                    wktReader.read("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),((41 41, 21 46, 46 31, 41 41)), ((20 35, 10 30, 10 10, 30 5, 45 20, 20 35), (30 20, 20 15, 20 25, 30 20)))"),
                    wktReader.read("GEOMETRYCOLLECTION (POINT (40 10), LINESTRING (10 10, 20 20, 10 40), POLYGON ((40 40, 20 45, 45 30, 40 40)))"),
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Param(value = {"0", "1", "2", "3", "4", "5", "6"})
//    @Param(value = {"1"})
    private int geometryIndex = 0;

//    private final WKBWriter wkbWriter = new WKBWriter(2,false);

    @Benchmark
    public void testWkb(Blackhole blackhole) throws Exception {
        WKBWriter wkbWriter = new WKBWriter(2, false);
        Geometry geometry = geometries[geometryIndex];
        byte[] bytes = wkbWriter.write(geometry);
        WKBReader wkbReader = new WKBReader();
        Geometry res = wkbReader.read(bytes);
        blackhole.consume(res);
    }

    @Benchmark
    public void testProtoFeature(Blackhole blackhole) {
        Geometry geometry = geometries[geometryIndex];
        byte[] bytes = ProtoFeatureConverter.geometry2Proto(geometry);
        Geometry res = ProtoFeatureConverter.proto2Geometry(bytes, new GeometryFactory());
        blackhole.consume(res);
    }

    public static void main(String[] args) throws Exception {
//        Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
//        ProtoFeatureConverterGeometryJmhTest t = new ProtoFeatureConverterGeometryJmhTest();
//        while (true){
//            t.testProtoFeature(blackhole);
//        }
        Options opt = new OptionsBuilder()
                .include(ProtoFeatureConverterGeometryJmhTest.class.getSimpleName())
                .result("jmhResult.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
