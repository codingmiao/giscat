package org.wowtools.giscat.vector.mvt;

import org.locationtech.jts.geom.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.converter.GeoJsonFeatureConverter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * 用springboot起一个web服务演示矢量瓦片的使用
 *
 * @author liuyu
 * @date 2022/4/26
 */
@SpringBootApplication
@RestController()
@RequestMapping("/tile")
@CrossOrigin
public class WebDemo {
    public static void main(String[] args) {
        SpringApplication.run(WebDemo.class, args);
    }

    /**
     * 测试数据，中国省份
     * 数据来源
     * https://datav.aliyun.com/portal/school/atlas/area_selector
     */
    private static final FeatureCollection areaFeatureCollection;

    private static final FeatureCollection lineFeatureCollection;
    private static final FeatureCollection pointFeatureCollection;

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    static {
        GeometryFactory gf = new GeometryFactory();
        String strJson = org.wowtools.common.utils.ResourcesReader.readStr(WebDemo.class, "china.json");
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
    }


    private static final String vtContentType = "application/octet-stream";

    @RequestMapping("/{z}/{x}/{y}")
    public void getBgTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, HttpServletResponse response) {
        MvtBuilder mvtBuilder = new MvtBuilder(z, x, y, geometryFactory);

        MvtLayer layer = mvtBuilder.getOrCreateLayer("area");
        for (Feature feature : areaFeatureCollection.getFeatures()) {
            if (mvtBuilder.getBbox().envIntersects(feature.getGeometry())) {
                layer.addFeature(feature);
            }
        }

        layer = mvtBuilder.getOrCreateLayer("line");
        for (Feature feature : lineFeatureCollection.getFeatures()) {
            if (mvtBuilder.getBbox().envIntersects(feature.getGeometry())) {
                layer.addFeature(feature);
            }
        }

        layer = mvtBuilder.getOrCreateLayer("point");
        for (Feature feature : pointFeatureCollection.getFeatures()) {
            if (mvtBuilder.getBbox().envIntersects(feature.getGeometry())) {
                layer.addFeature(feature);
            }
        }

        byte[] bytes = mvtBuilder.toBytes();
        exportByte(bytes, vtContentType, response);
    }


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
