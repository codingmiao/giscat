package org.wowtools.giscat.vector.rocksrtreetest;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.Assert;
import org.rocksdb.Transaction;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.rocksrtree.*;
import org.wowtools.giscat.vector.util.analyse.Bbox;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author liuyu
 * @date 2023/3/23
 */
public class Test {
    public static void main(String[] args) {
        GeometryFactory geometryFactory = new GeometryFactory();
        TreeBuilder builder = new TreeBuilder(2, 8) {
            @Override
            public String getFeatureKey(Feature feature) {
                return feature.getGeometry().toText();
            }

            @Override
            public RectNd getFeatureRect(Feature feature) {
                Bbox bbox = new Bbox(feature.getGeometry());
                return new RectNd(new double[]{bbox.xmin, bbox.ymin}, new double[]{bbox.xmax, bbox.ymax});
            }
        };
        final RTree pTree = new RTree(builder);
        Transaction tx = builder.newTx();
        try {
            for (int i = 0; i < 100; i++) {
                Point point = geometryFactory.createPoint(new Coordinate(i, i));
                pTree.add(new Feature(point), tx);
            }
            builder.commitTx(tx);
        } catch (Exception e) {
            builder.rollbackTx(tx);
            throw e;
        }

        final RectNd rect = new RectNd(new double[]{1.9, 1.9}, new double[]{8.1, 8.1});

        List<Feature> res = new LinkedList<>();

        FeatureConsumer consumer = new FeatureConsumer() {
            @Override
            public boolean accept(Feature f) {
                res.add(f);
                return true;
            }
        };
        pTree.contains(rect, consumer);
        Assert.equals(7, res.size());
        System.out.println(res.size());

        for (Feature feature : res) {
            Assert.isTrue(feature.getGeometry().getCoordinate().x >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().x <= 8);
            Assert.isTrue(feature.getGeometry().getCoordinate().y >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().y <= 8);
        }
    }
}
