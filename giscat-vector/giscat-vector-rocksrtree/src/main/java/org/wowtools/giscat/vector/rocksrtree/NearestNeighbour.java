package org.wowtools.giscat.vector.rocksrtree;

/**
 * @author liuyu
 * @date 2020/6/12
 */


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.*;

/**
 * 最邻近搜索查询条件
 */
public abstract class NearestNeighbour {

    private final DistanceResultNodeFilter filter;
    private final int maxHits;
    protected final PointNd pointNd;



    private static final DistanceResultNodeFilter alwaysTrue = dr -> true;


    /**
     * @param pointNd 目标点
     * @param maxHits 最大返回条数
     * @param filter  过滤器 为null则不过滤
     */
    public NearestNeighbour(@NotNull PointNd pointNd, int maxHits, @Nullable DistanceResultNodeFilter filter) {
        if (null == filter) {
            filter = alwaysTrue;
        }
        this.pointNd = pointNd;
        this.filter = filter;
        this.maxHits = maxHits;
    }

    /**
     * @param pointNd 目标点
     * @param maxHits 最大返回条数
     */
    public NearestNeighbour(@NotNull PointNd pointNd, int maxHits) {
        filter = alwaysTrue;
        this.pointNd = pointNd;
        this.maxHits = maxHits;
    }


    /**
     * 扩展点 查询的点到要素的距离计算方式
     *
     * @param feature 要素
     * @return 距离
     */
    protected abstract double getDist(Feature feature);

    /**
     * @return the nearest neighbour
     */
    public ArrayList<DistanceResult> find(Node root, TreeTransaction tx) {
        ArrayList<DistanceResult> ret =
                new ArrayList<>(maxHits);
        MinDistComparator nc =
                new MinDistComparator(pointNd);
        PriorityQueue<Node> queue = new PriorityQueue<Node>(20, nc);
        queue.add(root);
        while (!queue.isEmpty()) {
            Node n = queue.remove();
            if (n instanceof Branch) {
                nnExpandInternal((Branch) n, ret, maxHits, queue, tx);
            } else {
                nnExpandLeaf((Leaf) n, filter, ret, maxHits);
            }
        }
        return ret;
    }


    //访问索引上的非叶子节点
    private void nnExpandInternal(Branch node,
                                  List<DistanceResult> drs,
                                  int maxHits,
                                  PriorityQueue<Node> queue, TreeTransaction tx) {
        for (Node n : node.getChildren(tx)) {
            double[] mins = n.getBound().min.xs;
            double[] maxs = n.getBound().max.xs;
            double minDist = MinDist.get(mins, maxs, pointNd);
            int t = drs.size();
            // drs is sorted so we can check only the last entry
            if (t < maxHits || minDist <= drs.get(t - 1).dist) {
                queue.add(n);
            }
        }
    }

    //访问索引上的叶子节点
    private void nnExpandLeaf(
            Leaf node,
            DistanceResultNodeFilter filter,
            List<DistanceResult> drs,
            int maxHits) {
        for (int i = 0; i < node.size; i++) {
            RectNd entryRect = node.entryRects[i];
            double dist = getDist(entryRect.feature);
            DistanceResult dr = new DistanceResult(dist, entryRect.feature);
            if (filter.accept(dr)) {
                int n = drs.size();
                if (n < maxHits || dist < drs.get(n - 1).dist) {
                    add(drs, dr, maxHits);
                }
            }
        }

    }

    private void add(List<DistanceResult> drs,
                     DistanceResult dr,
                     int maxHits) {
        int n = drs.size();
        if (n == maxHits)
            drs.remove(n - 1);
        int pos = Collections.binarySearch(drs, dr, comp);
        if (pos < 0) {
            // binarySearch return -(pos + 1) for new entries
            pos = -(pos + 1);
        }
        drs.add(pos, dr);
    }

    private static final Comparator<DistanceResult> comp =
            Comparator.comparingDouble(d -> d.dist);

}
