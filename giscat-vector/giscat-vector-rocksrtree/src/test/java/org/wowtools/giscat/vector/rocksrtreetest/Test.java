package org.wowtools.giscat.vector.rocksrtreetest;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.Assert;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.rocksrtree.*;
import org.wowtools.giscat.vector.util.analyse.Bbox;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author liuyu
 * @date 2023/3/23
 */
public class Test {
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles(); // 获取文件夹中的所有文件和子文件夹
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file); // 递归删除子文件夹或文件
                }
            }
        }
        folder.delete(); // 删除空文件夹或文件
    }

    private static void add(String dir, Function<Feature, RectNd> featureRectNdFunction, GeometryFactory geometryFactory) {
        deleteFolder(new File(dir));
        int num = 12345;
        int txSize = 1000;

        TreeBuilder builder = new TreeBuilder(dir, null, 2, 64, featureRectNdFunction);
        RTree pTree = builder.getRTree();

        long t = System.currentTimeMillis();
        TreeTransaction tx = builder.newTx();
        for (int i = 0; i < num; i++) {
            Point point = geometryFactory.createPoint(new Coordinate(i, i));
            pTree.add(new Feature(point), tx);
            if (i % txSize == 0) {
                tx.commit();
                tx = builder.newTx();
                System.out.println("add "+i);
            }
        }
        tx.commit();
        System.out.println("add success,cost " + (System.currentTimeMillis() - t));

        builder.close();
    }

    private static void query(String dir, Function<Feature, RectNd> featureRectNdFunction) {
        TreeBuilder builder = new TreeBuilder(dir, null, featureRectNdFunction);
        final RectNd rect = new RectNd(new double[]{1.9, 1.9}, new double[]{8.1, 8.1});
        RTree pTree = builder.getRTree();

        List<Feature> res = new LinkedList<>();
        FeatureConsumer consumer = new FeatureConsumer() {
            @Override
            public boolean accept(Feature f) {
                res.add(f);
                return true;
            }
        };

        long t = System.currentTimeMillis();
        try (TreeTransaction tx = builder.newTx()) {
            pTree.contains(rect, consumer, tx);
        }
        System.out.println("query success,cost " + (System.currentTimeMillis() - t));

        Assert.equals(7, res.size());
        System.out.println(res.size());

        for (Feature feature : res) {
            Assert.isTrue(feature.getGeometry().getCoordinate().x >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().x <= 8);
            Assert.isTrue(feature.getGeometry().getCoordinate().y >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().y <= 8);
        }
//        builder.close();
    }

    public static void main(String[] args) {
        String dir = "D:\\_tmp\\1\\rocksrtree";

        Function<Feature, RectNd> featureRectNdFunction = (feature) -> {
            Bbox bbox = new Bbox(feature.getGeometry());
            return new RectNd(new double[]{bbox.xmin, bbox.ymin}, new double[]{bbox.xmax, bbox.ymax});
        };
        GeometryFactory geometryFactory = new GeometryFactory();

        add(dir, featureRectNdFunction, geometryFactory);

        query(dir, featureRectNdFunction);

    }
}
