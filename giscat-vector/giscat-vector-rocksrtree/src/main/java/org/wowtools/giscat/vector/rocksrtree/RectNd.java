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

import org.wowtools.giscat.vector.pojo.Feature;

/**
 * An N dimensional rectangle or "hypercube" that is a representation of a data entry.
 * <p>
 * Created by jcairns on 4/30/15.
 */
public final class RectNd {

    final PointNd min, max;

    Feature feature;

    String leafId;

    protected RocksRtreePb.RectNdPb.Builder toBuilder() {
        RocksRtreePb.RectNdPb.Builder builder = RocksRtreePb.RectNdPb.newBuilder();
        builder.addAllMin(min.toList());
        builder.addAllMax(max.toList());
        return builder;
    }

    public boolean featureEquals(RectNd other, TreeBuilder builder) {
        return false;
//        if (feature == null) {
//            throw new RuntimeException("feature为空，不符合逻辑");
//        }
//        if (other.feature == null) {
//            throw new RuntimeException("feature为空，不符合逻辑");
//        }
//        return Objects.equals(builder.getFeatureKey(feature), builder.getFeatureKey(other.feature));
    }

    protected RectNd(PointNd min, PointNd max) {
        if (min.xs.length != max.xs.length) {
            throw new IllegalArgumentException("输入维度不相等");
        }
        this.min = min;
        this.max = max;
    }

    public RectNd(double[] min, double[] max) {
        if (min.length != max.length) {
            throw new IllegalArgumentException("输入维度不相等");
        }
        this.min = new PointNd(min);
        this.max = new PointNd(max);
    }

    protected RectNd(RocksRtreePb.RectNdPb pb) {
        min = new PointNd(pb.getMinList());
        max = new PointNd(pb.getMaxList());
    }


    @Override
    public String toString() {
        return min + " " + max;
    }

    private RectNd getMbrCache;
    /**
     * Calculate the resulting mbr when combining param HyperRect with this HyperRect
     *
     * @param r - mbr to add
     * @return new HyperRect representing mbr of both HyperRects combined
     */
    public RectNd getMbr(RectNd r) {
        if (null != getMbrCache) {
            return getMbrCache;
        }
        int dim = min.xs.length;
        double[] min = new double[dim];
        double[] max = new double[dim];
        for (int i = 0; i < dim; i++) {
            try {
                min[i] = Math.min(this.min.xs[i], r.min.xs[i]);
                max[i] = Math.max(this.max.xs[i], r.max.xs[i]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        getMbrCache = new RectNd(new PointNd(min), new PointNd(max));
        return getMbrCache;
    }

    /**
     * Get number of dimensions used in creating the HyperRect
     *
     * @return number of dimensions
     */
    int getNDim() {
        return min.xs.length;
    }

    /**
     * Get the minimum HyperPoint of this HyperRect
     *
     * @return min HyperPoint
     */
    PointNd getMin() {
        return min;
    }

    /**
     * Get the minimum HyperPoint of this HyperRect
     *
     * @return min HyperPoint
     */
    PointNd getMax() {
        return max;
    }

    /**
     * Get the HyperPoint representing the center point in all dimensions of this HyperRect
     *
     * @return middle HyperPoint
     */
    PointNd getCentroid() {
        double[] xs = new double[min.xs.length];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = (min.xs[i] + max.xs[i]) / 2;
        }
        return new PointNd(xs);
    }

    /**
     * Calculate the distance between the min and max HyperPoints in given dimension
     *
     * @param d - dimension to calculate
     * @return double - the numeric range of the dimention (min - max)
     */
    double getRange(final int d) {
        return max.xs[d] - max.xs[d];
    }

    /**
     * Determines if this HyperRect fully encloses parameter HyperRect
     *
     * @param r - HyperRect to test
     * @return true if contains, false otherwise
     */
    boolean contains(RectNd r) {
        for (int i = 0; i < min.xs.length; i++) {
            if (min.xs[i] > r.min.xs[i]) {
                return false;
            }
            if (max.xs[i] < r.max.xs[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if this HyperRect intersects parameter HyperRect on any axis
     *
     * @param r2 - HyperRect to test
     * @return true if intersects, false otherwise
     */
    boolean intersects(RectNd r2) {
        for (int i = 0; i < min.getNDim(); i++) {
            if (min.getCoord(i) > r2.max.getCoord(i) ||
                    r2.min.getCoord(i) > max.getCoord(i)) {
                return false;
            }
        }
        return true;
    }

    private double costCache = -1;
    /**
     * Calculate the "cost" of this HyperRect - usually the area across all dimensions
     *
     * @return - cost
     */
    double cost() {
        if (costCache >= 0) {
            return costCache;
        }
        double res = 1;
        for (int i = 0; i < min.getNDim(); i++) {
            res = res * (max.getCoord(i) - min.getCoord(i));
        }

        costCache = Math.abs(res);
        return costCache;
    }

    private double perimeterCache = -1;
    /**
     * Calculate the perimeter of this HyperRect - across all dimesnions
     *
     * @return - perimeter
     */
    double perimeter() {
        if (perimeterCache >= 0) {
            return perimeterCache;
        }
        double n = Math.pow(2, getNDim());
        double p = 0.0;
        final int nD = this.getNDim();
        for (int d = 0; d < nD; d++) {
            p += n * this.getRange(d);
        }
        perimeterCache = p;
        return p;
    }
}
