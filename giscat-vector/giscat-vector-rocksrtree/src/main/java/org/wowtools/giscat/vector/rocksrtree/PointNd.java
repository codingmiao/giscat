/*
 *
 *  * Copyright (c) 2022- "giscat (https://github.com/codingmiao/giscat)"
 *  *
 *  * 本项目采用自定义版权协议，在不同行业使用时有不同约束，详情参阅：
 *  *
 *  * https://github.com/codingmiao/giscat/blob/main/LICENSE
 *
 */

package org.wowtools.giscat.vector.rocksrtree;

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * N dimensional point used to signify the bounds of a HyperRect
 * <p>
 * Created by jcairns on 5/5/15.
 */
final class PointNd {

    final double[] xs;

    protected List<Double> toList() {
        ArrayList<Double> list = new ArrayList<>(xs.length);
        for (double x : xs) {
            list.add(x);
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder('[');
        for (double x : xs) {
            sb.append(x).append(' ');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(']');
        return sb.toString();
    }

    public PointNd(double[] xs) {
        this.xs = xs;
    }

    public PointNd(List<Double> list) {
        double[] arr = new double[list.size()];
        int i = 0;
        for (Double v : list) {
            arr[i] = v;
            i++;
        }
        xs = arr;
    }
    /**
     * The number of dimensions represented by this point
     *
     * @return dimension count
     */
    public int getNDim() {
        return xs.length;
    }

    /**
     * Get the value of this point in the given dimension
     *
     * @param d - dimension
     * @return D - value of this point in the dimension
     */
    public double getCoord(int d) {
        return xs[d];
    }

    /**
     * Calculate the distance from this point to the given point across all dimensions
     *
     * @param p - point to calculate distance to
     * @return distance to the point
     * @throws IllegalArgumentException if a non-existent dimension is requested
     */
    public double distance(PointNd p) {
        if (xs.length != p.xs.length) {
            throw new IllegalArgumentException("输入维度不相等");
        }
        double ds = 0;
        for (int i = 0; i < xs.length; i++) {
            double d = xs[i] - p.xs[i];
            ds += d * d;
        }
        return Math.sqrt(ds);
    }

    /**
     * Calculate the distance from this point to the given point in a specific dimension
     *
     * @param p - point to calculate distance to
     * @param d - dimension to use in calculation
     * @return distance to the point in the fiven dimension
     */
    double distance(PointNd p, int d) {
        double res = xs[d] - p.xs[d];
        return Math.abs(res);
    }

    public PointNd clone() {
        double[] newXs = Arrays.copyOf(xs, xs.length);
        return new PointNd(newXs);
    }

//    public final static class Builder implements RectBuilder {
//
//        @Override
//        public RectNd getBBox(final RectNd point) {
//            return new RectNd(point.clone(), point.clone());
//        }
//
//        @Override
//        public RectNd getMbr(PointNd p1, PointNd p2) {
//            return new RectNd(p1, p2);
//        }
//
//    }
}
