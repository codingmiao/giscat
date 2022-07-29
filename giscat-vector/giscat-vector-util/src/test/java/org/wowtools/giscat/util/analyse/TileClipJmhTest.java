package org.wowtools.giscat.util.analyse;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 3)
@Threads(1)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TileClipJmhTest {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private final LineString[] lines;
    private final Geometry clipGeometry;
    private final TileClip tileClip;

    {
        GeometryFactory gf = new GeometryFactory();
        clipGeometry = gf.createPolygon(new Coordinate[]{
                new Coordinate(0.2, 0.2),
                new Coordinate(0.8, 0.2),
                new Coordinate(0.8, 0.8),
                new Coordinate(0.2, 0.8),
                new Coordinate(0.2, 0.2)
        });
        tileClip = new TileClip(0.2, 0.2, 0.8, 0.8, geometryFactory);
        Random random = new Random(233);
        int n = 100;
        lines = new LineString[n];
        for (int i = 0; i < n; i++) {
            Coordinate[] coords = new Coordinate[2 + random.nextInt(50)];

            for (int i1 = 0; i1 < coords.length; i1++) {
                coords[i1] = new Coordinate(random.nextDouble(), random.nextDouble());
            }
            LineString line = gf.createLineString(coords);
            lines[i] = line;
        }
    }

    @Benchmark
    public void jts(Blackhole blackhole) {
        for (LineString line : lines) {
            Geometry g = clipGeometry.intersection(line);
            blackhole.consume(g);
        }
    }

    @Benchmark
    public void tileClip(Blackhole blackhole) {
        for (LineString line : lines) {
            Geometry g = tileClip.intersection(line);
            blackhole.consume(g);
        }
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(TileClipJmhTest.class.getSimpleName())
                .result("jmhResult.json")
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }

}
