package org.wowtools.giscat.vector.rocksrtree;

import java.util.Comparator;

/**
 * A comparator that uses the MINDIST metrics to sort Nodes
 *
 * @author liuyu
 * @date 2020/6/12
 */
public class MinDistComparator implements Comparator<Node> {
    private final PointNd pointNd;

    public MinDistComparator(PointNd pointNd) {
        this.pointNd = pointNd;
    }

    @Override
    public int compare(Node n1, Node n2) {
        double[] mins1 = n1.getBound().min.xs;
        double[] maxs1 = n1.getBound().max.xs;

        double[] mins2 = n2.getBound().min.xs;
        double[] maxs2 = n2.getBound().max.xs;

        return Double.compare(MinDist.get(mins1, maxs1, pointNd),
                MinDist.get(mins2, maxs2, pointNd));
    }

    public PointNd getPointNd() {
        return pointNd;
    }
}
