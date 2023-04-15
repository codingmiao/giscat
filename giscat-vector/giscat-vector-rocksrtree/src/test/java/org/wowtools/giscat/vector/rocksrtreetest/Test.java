package org.wowtools.giscat.vector.rocksrtreetest;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.util.Assert;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.PojoConstant;
import org.wowtools.giscat.vector.rocksrtree.*;
import org.wowtools.giscat.vector.util.analyse.Bbox;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        int num = 10001;
        int txSize = 2000;

        TreeBuilder builder = new TreeBuilder(dir, null);
        RTree pTree = builder.getRTree();

        long t = System.currentTimeMillis();
        TreeTransaction tx = builder.newTx();
        for (int i = 0; i < num; i++) {
            Point point = geometryFactory.createPoint(new Coordinate(i, i));
            pTree.add(new Feature(point), tx);
            if (i % txSize == 0) {
                tx.commit();
                tx.close();
                tx = builder.newTx();
                System.out.println("add " + i);
            }
        }
        tx.commit();
        System.out.println("add success,cost " + (System.currentTimeMillis() - t));

        builder.close();
    }

    private static void query(String dir, Function<Feature, RectNd> featureRectNdFunction) throws Exception {
        TreeBuilder builder = new TreeBuilder(dir, null);
        final RectNd rect = new RectNd(new double[]{1.9, 1.9}, new double[]{8.1, 8.1});
        RTree pTree = builder.getRTree();


        long t = System.currentTimeMillis();
        List<Feature> res;
        try (TreeTransaction tx = builder.newTx()) {
            Iterator<Feature> it = pTree.contains(rect, tx);
            res = StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                    .collect(Collectors.toList());
        }
        System.out.println("1 query success,cost " + (System.currentTimeMillis() - t));

        Assert.equals(7, res.size());
        System.out.println(res.size());

        for (Feature feature : res) {
            Assert.isTrue(feature.getGeometry().getCoordinate().x >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().x <= 8);
            Assert.isTrue(feature.getGeometry().getCoordinate().y >= 2);
            Assert.isTrue(feature.getGeometry().getCoordinate().y <= 8);
        }

        for (int i = 0; i < 10; i++) {
            res.clear();
            t = System.currentTimeMillis();
            try (TreeTransaction tx = builder.newTx()) {
                Iterator<Feature> it = pTree.contains(rect, tx);
                res = StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false)
                        .collect(Collectors.toList());
            }
            System.out.println("2 query success,cost " + (System.currentTimeMillis() - t));
            Assert.equals(7, res.size());
        }

        builder.close();
    }

    private static void nearest(String dir) {
        class PointNearestNeighbour extends NearestNeighbour {
            private final double x, y;

            public PointNearestNeighbour(@NotNull PointNd pointNd, int maxHits, @Nullable DistanceResultNodeFilter filter) {
                super(pointNd, maxHits, filter);
                x = pointNd.getCoord(0);
                y = pointNd.getCoord(1);
            }

            @Override
            protected double getDist(Feature feature) {
                Coordinate coord = feature.getGeometry().getCoordinate();
                return Math.sqrt(Math.pow(coord.x - x, 2) + Math.pow(coord.y - y, 2));
            }
        }

        TreeBuilder builder = new TreeBuilder(dir, null);
        RTree pTree = builder.getRTree();
        ArrayList<DistanceResult> res;
        long t = System.currentTimeMillis();
        try (TreeTransaction tx = builder.newTx()) {
            PointNd inputPoint = new PointNd(new double[]{2.8, 2.8});
            int n = 3;
            res = pTree.nearest(new PointNearestNeighbour(inputPoint, n, null), tx);
        }
        System.out.println("nearest 0 cost " + (System.currentTimeMillis() - t));
        Assert.equals(3, res.size());
        Assert.equals("POINT (3 3)", res.get(0).getFeature().getGeometry().toText());
        Assert.equals("POINT (2 2)", res.get(1).getFeature().getGeometry().toText());
        Assert.equals("POINT (4 4)", res.get(2).getFeature().getGeometry().toText());

        for (int i = 0; i < 10; i++) {
            t = System.currentTimeMillis();
            try (TreeTransaction tx = builder.newTx()) {
                PointNd inputPoint = new PointNd(new double[]{2.8, 2.8});
                int n = 3;
                res = pTree.nearest(new NearestNeighbour(inputPoint, n) {

                    private final Geometry inputGeo;

                    {
                        double x = pointNd.getCoord(0);
                        double y = pointNd.getCoord(1);
                        inputGeo = PojoConstant.geometryFactory.createPoint(new Coordinate(x, y));
                    }

                    @Override
                    protected double getDist(Feature feature) {
                        return inputGeo.distance(feature.getGeometry());
                    }
                }, tx);
            }
            System.out.println("nearest 1 cost " + (System.currentTimeMillis() - t));
            Assert.equals(3, res.size());
            Assert.equals("POINT (3 3)", res.get(0).getFeature().getGeometry().toText());
            Assert.equals("POINT (2 2)", res.get(1).getFeature().getGeometry().toText());
            Assert.equals("POINT (4 4)", res.get(2).getFeature().getGeometry().toText());
        }

//        while (true) {
//            try (TreeTransaction tx = builder.newTx()) {
//                PointNd inputPoint = new PointNd(new double[]{2.8, 2.8});
//                int n = 3;
//                res = pTree.nearest(new PointNearestNeighbour(inputPoint, n, null), tx);
//            }
//        }
    }

    public static void main(String[] args) throws Exception {
        String dir = "D:\\_tmp\\1\\rocksrtree";

        Function<Feature, RectNd> featureRectNdFunction = (feature) -> {
            Bbox bbox = new Bbox(feature.getGeometry());
            return new RectNd(new double[]{bbox.xmin, bbox.ymin}, new double[]{bbox.xmax, bbox.ymax});
        };

        add(dir, featureRectNdFunction, PojoConstant.geometryFactory);

        query(dir, featureRectNdFunction);

        nearest(dir);

    }
}
