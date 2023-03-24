package org.wowtools.giscat.vector.rocksrtree;


import org.locationtech.jts.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author liuyu
 * @date 2023/3/23
 */
public class Test {
    public static void main(String[] args) {
        final RTree pTree = new RTree(new TreeNdBuilder(2, 8));

        for (int i = 0; i < 100; i++) {
            pTree.add(new RectNd(new PointNd(new double[]{i, i}),new PointNd(new double[]{i, i})));
        }

        final RectNd rect = new RectNd(new PointNd(new double[]{1.9,1.9}), new PointNd(new double[]{8.1,8.1}));

        List<RectNd> res = new LinkedList<>();

        Consumer<RectNd> consumer = new Consumer<>() {
            @Override
            public void accept(RectNd point2d) {
                res.add(point2d);
            }
        };
        pTree.search(rect, consumer);
//        Assert.equals(7, res.size());

        for (RectNd re : res) {
            Assert.isTrue(re.min.getCoord(0) >= 2);
            Assert.isTrue(re.min.getCoord(0) <= 8);
            Assert.isTrue(re.min.getCoord(1) >= 2);
            Assert.isTrue(re.min.getCoord(1) <= 8);
        }
    }
}
