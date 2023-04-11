package org.wowtools.giscat.vector.pojo.converter;

import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.util.SampleData;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @date 2022/4/1
 */
public class MakePbfTest1 {


    public static void main(String[] args) throws Exception {
        Map<String, Object> p = Map.of("a", List.of("1", 2.5d, true, "x", Map.of("xx", 111)), "b", "测试");
        FeatureCollection fc = new FeatureCollection();
        fc.setHeaders(Map.of(
                "version",1.1,
                "list",List.of(1,"ss",1.23)

        ));
        List<Feature> features = new LinkedList<>();
        fc.setFeatures(features);
        features.add(new Feature(SampleData.point, p));
        features.add(new Feature(SampleData.lineString, p));
        features.add(new Feature(SampleData.polygon1, p));
        features.add(new Feature(SampleData.polygon2, p));
        features.add(new Feature(SampleData.polygon3, p));
        features.add(new Feature(SampleData.multiPoint, p));
        features.add(new Feature(SampleData.multiLineString, p));
        features.add(new Feature(SampleData.multiPolygon1, p));
        features.add(new Feature(SampleData.multiPolygon2, p));
        features.add(new Feature(SampleData.geometryCollection, p));
        byte[] bytes = ProtoFeatureConverter.featureCollection2Proto(fc);
        save2File("D:/_test/1/testbytes.pbf", bytes);
        System.out.println(GeoJsonFeatureConverter.toGeoJson(fc).toGeoJsonString());
    }


    public static boolean save2File(String fname, byte[] msg) {
        OutputStream fos = null;
        try {
            File file = new File(fname);
            file.mkdirs();
            System.out.println(file.getAbsolutePath());
            fos = new FileOutputStream(file);
            fos.write(msg);
            fos.flush();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            File parent;
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
