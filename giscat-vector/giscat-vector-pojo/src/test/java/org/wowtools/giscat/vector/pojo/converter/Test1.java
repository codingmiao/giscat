package org.wowtools.giscat.vector.pojo.converter;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author liuyu
 * @date 2022/4/1
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 3)
@Threads(4)
@Fork(1)
@State(value = Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Test1 {

    private static final Double[] array = new Double[64];
    static {
        for (int i = 0; i < array.length; i++) {
            array[i] = Double.valueOf(i*10);
        }
    }

    @Benchmark
    public void testOther(Blackhole blackhole){
    }

    @Benchmark
    public void testMe(Blackhole blackhole) {
    }

    private static Double[] copyArray(Double[] in){
        Double[] out = new Double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    public static void main(String[] args) throws Exception {
        double d = 1d;
        Double d1 = (Double) d;
        double d2 = (double) d;
        System.out.println(d1);
        System.out.println(d2);
    }
}
