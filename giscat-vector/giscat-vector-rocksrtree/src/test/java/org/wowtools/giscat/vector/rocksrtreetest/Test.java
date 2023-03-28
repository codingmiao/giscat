package org.wowtools.giscat.vector.rocksrtreetest;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.Assert;
import org.rocksdb.Transaction;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.rocksrtree.*;
import org.wowtools.giscat.vector.util.analyse.Bbox;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
    public static void main(String[] args) {
        String dir = "D:\\_tmp\\1\\rocksrtree";
        deleteFolder(new File(dir));
        GeometryFactory geometryFactory = new GeometryFactory();
        TreeBuilder builder = new TreeBuilder(dir,null,2, 8) {
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
        TreeTransaction tx = builder.newTx();
        try {
            for (int i = 0; i < 100; i++) {
                Point point = geometryFactory.createPoint(new Coordinate(i, i));
                pTree.add(new Feature(point), tx);
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
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
        tx = builder.newTx();
        pTree.contains(rect, consumer,tx);
        tx.close();
        Assert.equals(7, res.size());
        System.out.println(res.size());

        for (Feature feature : res) {
            Assert.isTrue(feature.getGeometry().getCoordinate().x >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().x <= 8);
            Assert.isTrue(feature.getGeometry().getCoordinate().y >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().y <= 8);
        }
        deleteFolder(new File(dir));
    }
}
