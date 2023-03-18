package org.wowtools.giscat.vector.rocksrtree.pojo;

import org.wowtools.giscat.vector.rocksrtree.internal.RectBuilder;

/**
 * n维外包矩形
 *
 * @author liuyu
 * @date 2021/12/17
 */
public final class RectNd {
    private final PointNd min, max;


    /**
     * 连接到具体数据节点的id
     */
    private long dataNodeId = -1;

    public RectNd(PointNd min, PointNd max) {
        this.min = min;
        this.max = max;
    }

    public RectNd(double[] min, double[] max) {
        this.min = new PointNd(min);
        this.max = new PointNd(max);
    }

    public long getDataNodeId() {
        return dataNodeId;
    }

    public void setDataNodeId(long dataNodeId) {
        this.dataNodeId = dataNodeId;
    }

    @Override
    public boolean equals(Object o) {
        RectNd rectNd = (RectNd) o;
        if (dataNodeId > 0 && dataNodeId == rectNd.dataNodeId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(dataNodeId);
    }

    /**
     * Calculate the resulting mbr when combining param HyperRect with this HyperRect
     *
     * @param r2 - mbr to add
     * @return new HyperRect representing mbr of both HyperRects combined
     */
    public RectNd getMbr(RectNd r2) {
        double[] minXs = new double[min.getNDim()];
        double[] maxXs = new double[min.getNDim()];
        for (int i = 0; i < min.getNDim(); i++) {
            double max1 = max.getCoord(i);
            double max2 = r2.max.getCoord(i);
            maxXs[i] = max1 > max2 ? max1 : max2;

            double min1 = min.getCoord(i);
            double min2 = r2.min.getCoord(i);
            minXs[i] = min1 < min2 ? min1 : min2;
        }
        return new RectNd(new PointNd(minXs), new PointNd(maxXs));
    }

    /**
     * Get number of dimensions used in creating the HyperRect
     *
     * @return number of dimensions
     */
    public int getNDim() {
        return min.getNDim();
    }

    /**
     * Get the minimum HyperPoint of this HyperRect
     *
     * @return min HyperPoint
     */
    public double[] getMinXs() {
        return min.getXs();
    }

    /**
     * Get the minimum HyperPoint of this HyperRect
     *
     * @return min HyperPoint
     */
    public PointNd getMin() {
        return min;
    }

    /**
     * Get the minimum HyperPoint of this HyperRect
     *
     * @return min HyperPoint
     */
    public double[] getMaxXs() {
        return max.getXs();
    }

    /**
     * Get the minimum HyperPoint of this HyperRect
     *
     * @return min HyperPoint
     */
    public PointNd getMax() {
        return max;
    }

    /**
     * Get the HyperPoint representing the center point in all dimensions of this HyperRect
     *
     * @return middle HyperPoint
     */
    public PointNd getCentroid() {
        double[] avgs = new double[min.getNDim()];
        for (int i = 0; i < min.getNDim(); i++) {
            avgs[i] = (min.getCoord(i) + max.getCoord(i)) / 2;
        }
        return new PointNd(avgs);
    }

    /**
     * Calculate the distance between the min and max HyperPoints in given dimension
     *
     * @param d - dimension to calculate
     * @return double - the numeric range of the dimention (min - max)
     */
    public double getRange(int d) {
        return max.getCoord(d) - min.getCoord(d);
    }

    /**
     * Determines if this HyperRect fully encloses parameter HyperRect
     *
     * @param r2 - HyperRect to test
     * @return true if contains, false otherwise
     */
    public boolean contains(RectNd r2) {
        for (int i = 0; i < min.getNDim(); i++) {
            if (!(min.getCoord(i) <= r2.min.getCoord(i) &&
                    max.getCoord(i) >= r2.max.getCoord(i))) {
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
    public boolean intersects(RectNd r2) {
        for (int i = 0; i < min.getNDim(); i++) {
            if (min.getCoord(i) > r2.max.getCoord(i) ||
                    r2.min.getCoord(i) > max.getCoord(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate the "cost" of this HyperRect - usually the area across all dimensions
     *
     * @return - cost
     */
    public double cost() {
        double res = 1;
        for (int i = 0; i < min.getNDim(); i++) {
            res = res * (max.getCoord(i) - min.getCoord(i));
        }
        return Math.abs(res);
    }

    /**
     * Calculate the perimeter of this HyperRect - across all dimesnions
     *
     * @return - perimeter
     */
    public double perimeter() {
        double n = Math.pow(2, getNDim());
        double p = 0.0;
        final int nD = this.getNDim();
        for (int d = 0; d < nD; d++) {
            p += n * this.getRange(d);
        }
        return p;
    }


    public final static class Builder implements RectBuilder {
        public Builder() {
        }

        public RectNd getBBox(RectNd rectNd) {
            return rectNd;
        }

        @Override
        public RectNd getMbr(PointNd p1, PointNd p2) {
            double[] minXs = new double[p1.getNDim()];
            double[] maxXs = new double[p1.getNDim()];

            for (int i = 0; i < p1.getNDim(); i++) {
                double x1 = p1.getCoord(i);
                double x2 = p2.getCoord(i);
                if (x1 > x2) {
                    minXs[i] = x2;
                    maxXs[i] = x1;
                } else {
                    minXs[i] = x1;
                    maxXs[i] = x2;
                }
            }

            PointNd min = new PointNd(minXs);
            PointNd max = new PointNd(maxXs);
            return new RectNd(min, max);
        }
    }
}
