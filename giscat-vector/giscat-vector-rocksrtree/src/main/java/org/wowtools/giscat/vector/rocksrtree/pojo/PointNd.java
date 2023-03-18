package org.wowtools.giscat.vector.rocksrtree.pojo;


/**
 * n维点
 *
 * @author liuyu
 * @date 2021/12/17
 */
public final class PointNd {
    private final double[] xs;

    public PointNd(double[] xs) {
        this.xs = xs;
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
     * @throws IllegalArgumentException if a non-existent dimension is requested
     */
    public double getCoord(int d) {
        if (d < xs.length) {
            return xs[d];
        } else {
            throw new IllegalArgumentException("Invalid dimension");
        }
    }

    /**
     * Calculate the distance from this point to the given point across all dimensions
     *
     * @param p - point to calculate distance to
     * @return distance to the point
     * @throws IllegalArgumentException if a non-existent dimension is requested
     */
    public double distance(PointNd p) {
        double d = 0;
        for (int i = 0; i < p.getNDim(); i++) {
            double d1 = getCoord(i);
            double d2 = p.getCoord(i);
            double sub = d1 - d2;
            sub = sub * sub;
            d += sub;
        }
        return Math.sqrt(d);
    }

    /**
     * Calculate the distance from this point to the given point in a specific dimension
     *
     * @param p - point to calculate distance to
     * @param d - dimension to use in calculation
     * @return distance to the point in the fiven dimension
     */
    public double distance(PointNd p, int d) {
        double d1 = getCoord(d);
        double d2 = p.getCoord(d);
        return Math.abs(d1 - d2);
    }

    /**
     * 获取坐标点
     *
     * @return 坐标的数组
     */
    public double[] getXs() {
        return xs;
    }
}
