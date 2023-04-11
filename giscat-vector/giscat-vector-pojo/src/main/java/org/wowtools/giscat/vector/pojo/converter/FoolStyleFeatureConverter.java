/*
 *
 *  * Copyright (c) 2022- "giscat (https://github.com/codingmiao/giscat)"
 *  *
 *  * 本项目采用自定义版权协议，在不同行业使用时有不同约束，详情参阅：
 *  *
 *  * https://github.com/codingmiao/giscat/blob/main/LICENSE
 *
 */

package org.wowtools.giscat.vector.pojo.converter;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.Iterator;
import java.util.List;

import static org.wowtools.giscat.vector.pojo.PojoConstant.geometryFactory;

/**
 * 傻瓜式的对象转换工具，例如把一个坐标list转为一条线
 *
 * @author liuyu
 * @date 2023/4/6
 */
public class FoolStyleFeatureConverter {

    /**
     * 将 x y转为point对象
     *
     * @param x x
     * @param y y
     * @return Point
     */
    public static Point xy2Point(double x, double y) {
        return geometryFactory.createPoint(new Coordinate(x, y));
    }

    /**
     * 将list转为point
     *
     * @param list list
     * @return Point
     */
    public static Point list2Point(List<Double> list) {
        return geometryFactory.createPoint(new Coordinate(list.get(0), list.get(1)));
    }

    /**
     * 将array转为point
     *
     * @param array array
     * @return Point
     */
    public static Point array2Point(double[] array) {
        return geometryFactory.createPoint(new Coordinate(array[0], array[1]));
    }

    /**
     * 将string转为point，例如str2Point("10,2",",")转为POINT(10 2)
     *
     * @param str   str
     * @param regex 分隔符
     * @return Point
     */
    public static Point str2Point(String str, String regex) {
        String[] strs = str.trim().split(regex);
        return geometryFactory.createPoint(new Coordinate(Double.parseDouble(strs[0]), Double.parseDouble(strs[1])));
    }

    /**
     * 将list转为线
     *
     * @param list list 例如[1,2,3,4]转为LINESTRING(1 2,3 4)
     * @return LineString
     */
    public static LineString list2Line(List<Double> list) {
        Coordinate[] coords = new Coordinate[list.size() / 2];
        int i = 0;
        Iterator<Double> iterator = list.iterator();
        while (iterator.hasNext()) {
            double x = iterator.next();
            double y = iterator.next();
            coords[i] = new Coordinate(x, y);
            i++;
        }
        return geometryFactory.createLineString(coords);
    }

    /**
     * 将array转为线
     *
     * @param array list 例如[1,2,3,4]转为LINESTRING(1 2,3 4)
     * @return LineString
     */
    public static LineString array2Line(double[] array) {
        Coordinate[] coords = new Coordinate[array.length / 2];
        for (int i = 0; i < array.length; i += 2) {
            double x = array[i];
            double y = array[i + 1];
            coords[i / 2] = new Coordinate(x, y);
        }
        return geometryFactory.createLineString(coords);
    }

    /**
     * 将list转为线
     *
     * @param list list 例如[[1,2],[3,4]]转为LINESTRING(1 2,3 4)
     * @return LineString
     */
    public static LineString lists2Line(List<double[]> list) {
        Coordinate[] coords = new Coordinate[list.size()];
        int i = 0;
        for (double[] doubles : list) {
            double[] ds = doubles;
            coords[i] = new Coordinate(ds[0], ds[1]);
            i++;
        }
        return geometryFactory.createLineString(coords);
    }

    /**
     * 将array转为线
     *
     * @param array array 例如[[1,2],[3,4]]转为LINESTRING(1 2,3 4)
     * @return LineString
     */
    public static LineString arrays2Line(double[][] array) {
        Coordinate[] coords = new Coordinate[array.length];
        for (int i = 0; i < array.length; i++) {
            double[] ds = array[i];
            coords[i] = new Coordinate(ds[0], ds[1]);
        }
        return geometryFactory.createLineString(coords);
    }

    /**
     * 将string转为线，例如str2Line("10,2;15,3",",",";")转为LINESTRING(10 2,15 3)
     *
     * @param str        str
     * @param xyRegex    xy坐标间的分隔符
     * @param pointRegex 点之间的分隔符
     * @return LineString
     */
    public static LineString str2Line(String str, String xyRegex, String pointRegex) {
        String[] pointArray = str.trim().split(pointRegex);
        Coordinate[] coords = new Coordinate[pointArray.length];
        for (int i = 0; i < pointArray.length; i++) {
            String[] xy = pointArray[i].trim().split(xyRegex);
            coords[i] = new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
        }
        return geometryFactory.createLineString(coords);
    }


    /**
     * 将list转为面
     *
     * @param list list 例如[1,2,3,4,6,6]转为POLYGON ((1 2, 3 4, 6 6, 1 2))
     * @return Polygon
     */
    public static Polygon list2Polygon(List<Double> list) {
        double[] array = new double[list.size()];
        int i = 0;
        for (Double v : list) {
            array[i] = v;
            i++;
        }
        return array2Polygon(array);
    }

    /**
     * 将array转为面
     *
     * @param array array 例如[1,2,3,4,6,6]转为POLYGON ((1 2, 3 4, 6 6, 1 2))
     * @return Polygon
     */
    public static Polygon array2Polygon(double[] array) {
        boolean endSame = array[array.length - 1] == array[1] && array[array.length - 2] == array[0];

        Coordinate[] coords = new Coordinate[endSame ? array.length / 2 : array.length / 2 + 1];
        for (int i = 0; i < array.length; i += 2) {
            double x = array[i];
            double y = array[i + 1];
            coords[i / 2] = new Coordinate(x, y);
        }
        if (!endSame) {
            coords[coords.length - 1] = coords[0].copy();
        }
        return geometryFactory.createPolygon(coords);
    }

    /**
     * 将list转为面
     *
     * @param list list 例如[[1,2],[3,4],[6,6]]转为POLYGON ((1 2, 3 4, 6 6, 1 2))
     * @return Polygon
     */
    public static Polygon lists2Polygon(List<double[]> list) {
        double[][] array = new double[list.size()][];
        int i = 0;
        for (double[] v : list) {
            array[i] = v;
            i++;
        }
        return arrays2Polygon(array);
    }

    /**
     * 将array转为面
     *
     * @param array array 例如[[1,2],[3,4],[6,6]]转为POLYGON ((1 2, 3 4, 6 6, 1 2))
     * @return Polygon
     */
    public static Polygon arrays2Polygon(double[][] array) {
        boolean endSame = array[0][0] == array[array.length - 1][0] && array[0][1] == array[array.length - 1][1];
        Coordinate[] coords = new Coordinate[endSame ? array.length : array.length + 1];
        for (int i = 0; i < array.length; i ++) {
            double[] xy = array[i];
            coords[i] = new Coordinate(xy[0], xy[1]);
        }
        if (!endSame) {
            coords[coords.length - 1] = coords[0].copy();
        }
        return geometryFactory.createPolygon(coords);
    }

    /**
     * 将string转为线，例如str2Line("1,2;3,4;6,6",",",";")转为POLYGON ((1 2, 3 4, 6 6, 1 2))
     *
     * @param str        str
     * @param xyRegex    xy坐标间的分隔符
     * @param pointRegex 点之间的分隔符
     * @return Polygon
     */
    public static Polygon str2Polygon(String str, String xyRegex, String pointRegex){
        String[] points = str.trim().split(pointRegex);
        double[][] array = new double[points.length][];
        int i = 0;
        for (String point: points) {
            String[] xy = point.trim().split(xyRegex);
            array[i] = new double[]{Double.parseDouble(xy[0]),Double.parseDouble(xy[1])};
            i++;
        }
        return arrays2Polygon(array);
    }



}
