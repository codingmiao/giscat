package org.wowtools.giscat.vector.mvt.demos.threads;

import org.locationtech.jts.geom.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wowtools.giscat.vector.mvt.MvtBuilder;
import org.wowtools.giscat.vector.mvt.MvtLayer;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.converter.GeoJsonFeatureConverter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 用springboot起一个web服务演示矢量瓦片的使用
 * 多线程添加图层和要素演示
 *
 * @author liuyu
 * @date 2022/4/26
 */
@SpringBootApplication
@RestController()
@RequestMapping("/tile")
@CrossOrigin
public class ThreadsDemo {
    public static void main(String[] args) {
        SpringApplication.run(ThreadsDemo.class, args);
    }

    /**
     * 测试数据，中国省份
     * 数据来源
     * https://datav.aliyun.com/portal/school/atlas/area_selector
     */
    private static final FeatureCollection areaFeatureCollection;//面数据
    private static final FeatureCollection lineFeatureCollection;//线数据
    private static final FeatureCollection pointFeatureCollection;//点数据

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    static {
        //构造示例数据
        GeometryFactory gf = new GeometryFactory();
        String strJson = org.wowtools.common.utils.ResourcesReader.readStr(ThreadsDemo.class, "china.json");
        areaFeatureCollection = GeoJsonFeatureConverter.fromGeoJsonFeatureCollection(strJson, gf);
        ArrayList<Feature> pointFeatures = new ArrayList<>(areaFeatureCollection.getFeatures().size());
        ArrayList<Feature> lineFeatures = new ArrayList<>(areaFeatureCollection.getFeatures().size());
        for (Feature feature : areaFeatureCollection.getFeatures()) {

            Feature pointFeature = new Feature();
            ArrayList center = (ArrayList) feature.getProperties().get("center");
            if (center != null) {
                Point point = gf.createPoint(new Coordinate((Double) center.get(0), (Double) center.get(1)));
                pointFeature.setProperties(Map.of("name", feature.getProperties().get("name")));
                pointFeature.setGeometry(point);
                pointFeatures.add(pointFeature);
            }

            Feature lineFeature = new Feature();
            if (feature.getGeometry() instanceof MultiPolygon) {

                MultiPolygon multiPolygon = (MultiPolygon) feature.getGeometry();
                LineString[] lines = new LineString[multiPolygon.getNumGeometries()];
                for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                    Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                    lines[i] = gf.createLineString(polygon.getExteriorRing().getCoordinates());
                }
                MultiLineString ml = gf.createMultiLineString(lines);
                lineFeature.setGeometry(ml);
            } else {
                LineString line = gf.createLineString(feature.getGeometry().getCoordinates());
                lineFeature.setGeometry(line);
            }

            lineFeatures.add(lineFeature);
        }

        lineFeatureCollection = new FeatureCollection();
        lineFeatureCollection.setFeatures(lineFeatures);

        pointFeatureCollection = new FeatureCollection();
        pointFeatureCollection.setFeatures(pointFeatures);

//        StringBuilder sb = new StringBuilder();
//        {
//            int i = 1;
//            for (Feature pointFeature : pointFeatures) {
//                sb.append("insert into test_point values (" + i + ",st_geometryfromtext('" + pointFeature.getGeometry() + "',4326));\n");
//                i++;
//            }
//        }
//        {
//            int i = 1;
//            for (Feature lineFeature : lineFeatures) {
//                sb.append("insert into test_line values (" + i + ",'line" + i + "',st_geometryfromtext('" + lineFeature.getGeometry() + "',4326));\n");
//                i++;
//            }
//        }
//        {
//            int i = 1;
//            for (Feature feature : areaFeatureCollection.getFeatures()) {
//                sb.append("insert into test_polygon values (" + i + ",'" + feature.getProperties().get("name") + "',st_geometryfromtext('" + feature.getGeometry() + "',4326));\n");
//                i++;
//            }
//        }
//        System.out.println(sb);
    }


    private static final String vtContentType = "application/octet-stream";

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    @RequestMapping("/{z}/{x}/{y}")
    public void getTile(@PathVariable byte z, @PathVariable int x, @PathVariable int y, HttpServletResponse response) {
        //构造一个MvtBuilder对象
        MvtBuilder mvtBuilder = new MvtBuilder(z, x, y, geometryFactory);

        //向mvt中添加layer
        MvtLayer areaLayer = mvtBuilder.createLayer("省区域");
        MvtLayer lineLayer = mvtBuilder.createLayer("省边界");
        MvtLayer pointLayer = mvtBuilder.createLayer("省会位置");

        //多线程添加数据，实际运用中也可以起多个线程去查询并添加
        Semaphore semaphore = new Semaphore(0);

        threadPool.execute(() -> {
            for (Feature feature : areaFeatureCollection.getFeatures()) {
                //这里简单地从内存中取数据并判断其是否与瓦片有交集，实际运用中可从数据库查询，例如postgis的ST_intersects函数
                if (mvtBuilder.getBbox().envIntersects(feature.getGeometry())) {
                    areaLayer.addFeature(feature);
                }
            }
            semaphore.release();
        });

        threadPool.execute(() -> {
            for (Feature feature : pointFeatureCollection.getFeatures()) {
                //这里简单地从内存中取数据并判断其是否与瓦片有交集，实际运用中可从数据库查询，例如postgis的ST_intersects函数
                if (mvtBuilder.getBbox().envIntersects(feature.getGeometry())) {
                    pointLayer.addFeature(feature);
                }
            }
            semaphore.release();
        });

        threadPool.execute(() -> {
            for (Feature feature : lineFeatureCollection.getFeatures()) {
                //这里简单地从内存中取数据并判断其是否与瓦片有交集，实际运用中可从数据库查询，例如postgis的ST_intersects函数
                if (mvtBuilder.getBbox().envIntersects(feature.getGeometry())) {
                    lineLayer.addFeature(feature);
                }
            }
            semaphore.release();
        });

        try {
            semaphore.acquire(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //数据添加完毕，转为bytes
        byte[] bytes = mvtBuilder.toBytes();
        exportByte(bytes, vtContentType, response);
    }

    //将bytes写进HttpServletResponse
    private void exportByte(byte[] bytes, String contentType, HttpServletResponse response) {
        response.setContentType(contentType);
        try (OutputStream os = response.getOutputStream()) {
            os.write(bytes);
            os.flush();
        } catch (org.apache.catalina.connector.ClientAbortException e) {
            //地图移动时客户端主动取消， 产生异常"你的主机中的软件中止了一个已建立的连接"，无需处理
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
