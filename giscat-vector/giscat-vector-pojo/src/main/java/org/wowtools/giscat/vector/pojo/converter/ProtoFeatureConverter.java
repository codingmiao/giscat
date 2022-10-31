/*****************************************************************
 *  Copyright (c) 2022- "giscat by 刘雨 (https://github.com/codingmiao/giscat)"
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.wowtools.giscat.vector.pojo.converter;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import org.locationtech.jts.geom.*;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.proto.ProtoFeature;

import java.util.*;

/**
 * ProtoFeature bytes与Feature的相互转换
 *
 * @author liuyu
 * @date 2022/3/25
 */
public class ProtoFeatureConverter {
    // List.indexes 标注list中的第n个元素的类型是什么类型，如[1L,2D,'SSS'] 的indexes为 [5,2,7]
    private static final int doubleValueIdsIndex = 2;
    private static final int floatValueIdsIndex = 3;
    private static final int sint32ValueIdsIndex = 4;
    private static final int sint64ValueIdsIndex = 5;
    private static final int boolValuesIndex = 6;
    private static final int stringValueIdsIndex = 7;
    private static final int bytesValueIdsIndex = 8;
    private static final int mapValuesIndex = 9;
    private static final int subListValuesIndex = 10;

    private static final ProtoFeature.NullGeometry nullGeometry = ProtoFeature.NullGeometry.getDefaultInstance();
    private static final ProtoFeature.Map nullMap = ProtoFeature.Map.newBuilder().build();

    /**
     * geometry 转 ProtoFeature bytes
     *
     * @param geometry geometry
     * @return ProtoFeature bytes
     */
    public static byte[] geometry2Proto(Geometry geometry) {
        return geometry2ProtoBuilder(geometry).build().toByteArray();
    }

    private static ProtoFeature.Geometry.Builder geometry2ProtoBuilder(Geometry geometry) {
        ProtoFeature.Geometry.Builder geometryBuilder = ProtoFeature.Geometry.newBuilder();
        if (null == geometry) {
            geometryBuilder.setNullGeometry(nullGeometry);
        } else if (geometry instanceof Point) {
            geometryBuilder.setPoint(point2Proto((Point) geometry));
        } else if (geometry instanceof LineString) {
            geometryBuilder.setLineString(lineString2Proto((LineString) geometry));

        } else if (geometry instanceof Polygon) {
            geometryBuilder.setPolygon(polygon2Proto((Polygon) geometry));

        } else if (geometry instanceof MultiPoint) {
            geometryBuilder.setMultiPoint(multiPoint2Proto((MultiPoint) geometry));

        } else if (geometry instanceof MultiLineString) {
            geometryBuilder.setMultiLineString(multiLineString2Proto((MultiLineString) geometry));

        } else if (geometry instanceof MultiPolygon) {
            geometryBuilder.setMultiPolygon(multiPolygon2Proto((MultiPolygon) geometry));

        } else if (geometry instanceof GeometryCollection) {
            geometryBuilder.setGeometryCollection(geometryCollection2Proto((GeometryCollection) geometry));
        } else {
            throw new RuntimeException("未实现的geometry类型 " + geometry.getClass());
        }
        return geometryBuilder;
    }


    /**
     * 将coordinates转换为 xs ys zs list以便放入Proto
     */
    private static final class ProtoCoordinateCell {
        private final ArrayList<Double> xs;
        private final ArrayList<Double> ys;
        private final ArrayList<Double> zs;

        public ProtoCoordinateCell(Coordinate[] coordinates) {
            if (Double.isNaN(coordinates[0].getZ())) {
                xs = new ArrayList<>();
                ys = new ArrayList<>();
                zs = null;
                for (Coordinate coordinate : coordinates) {
                    xs.add(coordinate.getX());
                    ys.add(coordinate.getY());
                }
            } else {
                xs = new ArrayList<>();
                ys = new ArrayList<>();
                zs = new ArrayList<>();
                for (Coordinate coordinate : coordinates) {
                    xs.add(coordinate.getX());
                    ys.add(coordinate.getY());
                    zs.add(coordinate.getZ());
                }
            }
        }
    }

    private static ProtoFeature.Point.Builder point2Proto(Point point) {
        Coordinate coordinate = point.getCoordinate();
        ProtoFeature.Point.Builder builder = ProtoFeature.Point.newBuilder();
        builder.setX(coordinate.getX());
        builder.setY(coordinate.getY());
        if (!Double.isNaN(coordinate.getZ())) {
            builder.setZ(coordinate.getZ());
        }
        return builder;
    }

    private static ProtoFeature.LineString.Builder lineString2Proto(LineString lineString) {
        ProtoFeature.LineString.Builder builder = ProtoFeature.LineString.newBuilder();
        Coordinate[] coordinates = lineString.getCoordinates();
        ProtoCoordinateCell protoCoordinateCell = new ProtoCoordinateCell(coordinates);
        builder.addAllXs(protoCoordinateCell.xs);
        builder.addAllYs(protoCoordinateCell.ys);
        if (null != protoCoordinateCell.zs) {
            builder.addAllZs(protoCoordinateCell.zs);
        }
        return builder;
    }

    private static ProtoFeature.Polygon.Builder polygon2Proto(Polygon polygon) {
        ProtoFeature.Polygon.Builder builder = ProtoFeature.Polygon.newBuilder();
        List<Integer> separators = new LinkedList<>();

        int holeNum = polygon.getNumInteriorRing();
        Coordinate[] coordinates = new Coordinate[polygon.getNumPoints() - 1 - holeNum];//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
        int k = -1;
        Coordinate[] shellCoordinates = polygon.getExteriorRing().getCoordinates();
        for (int x = 0; x < shellCoordinates.length - 1; x++) {//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
            k++;
            coordinates[k] = shellCoordinates[x];
        }
        if (holeNum > 0) {
            separators.add(k);
            for (int i = 0; i < holeNum; i++) {
                Coordinate[] childCoordinates = polygon.getInteriorRingN(i).getCoordinates();
                for (int j = 0; j < childCoordinates.length - 1; j++) {//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
                    k++;
                    coordinates[k] = childCoordinates[j];
                }
                if (i < holeNum - 1) {
                    separators.add(k);
                }
            }
        }

        ProtoCoordinateCell protoCoordinateCell = new ProtoCoordinateCell(coordinates);
        builder.addAllXs(protoCoordinateCell.xs);
        builder.addAllYs(protoCoordinateCell.ys);
        if (null != protoCoordinateCell.zs) {
            builder.addAllZs(protoCoordinateCell.zs);
        }
        builder.addAllSeparators(separators);
        return builder;
    }

    private static ProtoFeature.MultiPoint.Builder multiPoint2Proto(MultiPoint multiPoint) {
        ProtoFeature.MultiPoint.Builder builder = ProtoFeature.MultiPoint.newBuilder();
        Coordinate[] coordinates = multiPoint.getCoordinates();
        ProtoCoordinateCell protoCoordinateCell = new ProtoCoordinateCell(coordinates);
        builder.addAllXs(protoCoordinateCell.xs);
        builder.addAllYs(protoCoordinateCell.ys);
        if (null != protoCoordinateCell.zs) {
            builder.addAllZs(protoCoordinateCell.zs);
        }
        return builder;
    }

    private static ProtoFeature.MultiLineString.Builder multiLineString2Proto(MultiLineString multiLineString) {
        ProtoFeature.MultiLineString.Builder builder = ProtoFeature.MultiLineString.newBuilder();
        List<Integer> separators = new LinkedList<>();
        int lineNum = multiLineString.getNumGeometries();
        Coordinate[] coordinates = new Coordinate[multiLineString.getNumPoints()];
        int k = -1;
        for (int i = 0; i < lineNum; i++) {
            Coordinate[] childCoordinates = multiLineString.getGeometryN(i).getCoordinates();
            for (Coordinate childCoordinate : childCoordinates) {
                k++;
                coordinates[k] = childCoordinate;
            }
            if (i < lineNum - 1) {
                separators.add(k);
            }
        }
        if (separators.size() > 0) {
            builder.addAllSeparators(separators);
        }
        ProtoCoordinateCell protoCoordinateCell = new ProtoCoordinateCell(coordinates);
        builder.addAllXs(protoCoordinateCell.xs);
        builder.addAllYs(protoCoordinateCell.ys);
        if (null != protoCoordinateCell.zs) {
            builder.addAllZs(protoCoordinateCell.zs);
        }
        return builder;
    }

    private static ProtoFeature.MultiPolygon.Builder multiPolygon2Proto(MultiPolygon multiPolygon) {
        ProtoFeature.MultiPolygon.Builder builder = ProtoFeature.MultiPolygon.newBuilder();
        int polygonNum = multiPolygon.getNumGeometries();
        LinkedList<Integer> coordSeparators = new LinkedList<>();
        LinkedList<Integer> polygonSeparators = new LinkedList<>();
        LinkedList<Coordinate> coordinateList = new LinkedList<>();
        int k = -1;
        int beforePolygonSeparator = 0;
        for (int gi = 0; gi < polygonNum; gi++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(gi);
            int holeNum = polygon.getNumInteriorRing();
            Coordinate[] shellCoordinates = polygon.getExteriorRing().getCoordinates();
            for (int x = 0; x < shellCoordinates.length - 1; x++) {//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
                k++;
                coordinateList.add(shellCoordinates[x]);
            }
            coordSeparators.add(k);
            if (holeNum > 0) {
                for (int i = 0; i < holeNum; i++) {
                    Coordinate[] childCoordinates = polygon.getInteriorRingN(i).getCoordinates();
                    for (int j = 0; j < childCoordinates.length - 1; j++) {//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
                        k++;
                        coordinateList.add(childCoordinates[j]);
                    }
                    coordSeparators.add(k);
                }
            }
            int currentPolygonSeparator = beforePolygonSeparator + 1 + holeNum;//前一个位置 + 外环 + 内环
            polygonSeparators.add(currentPolygonSeparator);
            beforePolygonSeparator = currentPolygonSeparator;
        }
        coordSeparators.removeLast();
        polygonSeparators.removeLast();

        if (coordSeparators.size() > 0) {
            builder.addAllCoordSeparators(coordSeparators);
        }

        if (polygonSeparators.size() > 0) {
            builder.addAllPolygonSeparators(polygonSeparators);
        }

        Coordinate[] coordinates = new Coordinate[coordinateList.size()];
        coordinateList.toArray(coordinates);
        ProtoCoordinateCell protoCoordinateCell = new ProtoCoordinateCell(coordinates);
        builder.addAllXs(protoCoordinateCell.xs);
        builder.addAllYs(protoCoordinateCell.ys);
        if (null != protoCoordinateCell.zs) {
            builder.addAllZs(protoCoordinateCell.zs);
        }

        return builder;
    }

    private static ProtoFeature.GeometryCollection.Builder geometryCollection2Proto(GeometryCollection geometryCollection) {
        ProtoFeature.GeometryCollection.Builder builder = ProtoFeature.GeometryCollection.newBuilder();
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry geometry = geometryCollection.getGeometryN(i);
            // geometryCollection中不允许null，所以这个检查可以跳过
//            if (null == geometry) {
//                continue;
//            }
            if (geometry instanceof Point) {
                builder.addPoints(point2Proto((Point) geometry));
            } else if (geometry instanceof LineString) {
                builder.addLineStrings(lineString2Proto((LineString) geometry));
            } else if (geometry instanceof Polygon) {
                builder.addPolygons(polygon2Proto((Polygon) geometry));
            } else if (geometry instanceof MultiPoint) {
                builder.addMultiPoints(multiPoint2Proto((MultiPoint) geometry));
            } else if (geometry instanceof MultiLineString) {
                builder.addMultiLineStrings(multiLineString2Proto((MultiLineString) geometry));
            } else if (geometry instanceof MultiPolygon) {
                builder.addMultiPolygons(multiPolygon2Proto((MultiPolygon) geometry));
            } else if (geometry instanceof GeometryCollection) {
                builder.addGeometryCollections(geometryCollection2Proto((GeometryCollection) geometry));
            } else {
                throw new RuntimeException("未实现的geometry类型 " + geometry.getClass());
            }
        }
        return builder;
    }

    /**
     * ProtoFeature bytes 转 geometry
     *
     * @param bytes           ProtoFeature bytes
     * @param geometryFactory jts geometryFactory
     * @return geometry
     */
    public static Geometry proto2Geometry(byte[] bytes, GeometryFactory geometryFactory) {
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        ProtoFeature.Geometry pGeometry;
        try {
            pGeometry = ProtoFeature.Geometry.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        return proto2Geometry(pGeometry, geometryFactory);
    }

    private static Geometry proto2Geometry(ProtoFeature.Geometry pGeometry, GeometryFactory geometryFactory) {
        if (pGeometry.hasPoint()) {
            ProtoFeature.Point pPoint = pGeometry.getPoint();
            return proto2Point(pPoint, geometryFactory);
        }
        if (pGeometry.hasLineString()) {
            ProtoFeature.LineString pLineString = pGeometry.getLineString();
            return proto2LineString(pLineString, geometryFactory);
        }
        if (pGeometry.hasPolygon()) {
            ProtoFeature.Polygon pPolygon = pGeometry.getPolygon();
            return proto2Polygon(pPolygon, geometryFactory);
        }

        if (pGeometry.hasMultiPoint()) {
            ProtoFeature.MultiPoint pMultiPoint = pGeometry.getMultiPoint();
            return proto2MultiPoint(pMultiPoint, geometryFactory);
        }
        if (pGeometry.hasMultiLineString()) {
            ProtoFeature.MultiLineString pMultiLineString = pGeometry.getMultiLineString();
            return proto2MultiLineString(pMultiLineString, geometryFactory);
        }
        if (pGeometry.hasMultiPolygon()) {
            ProtoFeature.MultiPolygon pMultiPolygon = pGeometry.getMultiPolygon();
            return proto2MultiPolygon(pMultiPolygon, geometryFactory);
        }
        if (pGeometry.hasGeometryCollection()) {
            ProtoFeature.GeometryCollection pGeometryCollection = pGeometry.getGeometryCollection();
            return proto2GeometryCollection(pGeometryCollection, geometryFactory);
        }
        if (pGeometry.hasNullGeometry()) {
            return null;
        }
        throw new RuntimeException("解析pGeometry逻辑错误");
    }

    //点的z字段，用以判断点是否有z值
    private static final Descriptors.FieldDescriptor FieldDescriptor_Point_z = ProtoFeature.Point.getDescriptor().findFieldByNumber(ProtoFeature.Point.Z_FIELD_NUMBER);


    private static Coordinate[] proto2Coordinates(List<Double> xs, List<Double> ys, List<Double> zs) {
        int n = xs.size();
        Coordinate[] coordinates = new Coordinate[n];
        if (zs.size() != xs.size()) {
            for (int i = 0; i < n; i++) {
                coordinates[i] = new Coordinate(xs.get(i), ys.get(i));
            }
        } else {
            for (int i = 0; i < n; i++) {
                coordinates[i] = new Coordinate(xs.get(i), ys.get(i), zs.get(i));
            }
        }
        return coordinates;
    }

    private static Coordinate[][] separatorCoordinates(Coordinate[] coordinates, List<Integer> separators) {
        Coordinate[][] separatorCoordinates = new Coordinate[separators.size() + 1][];
        int idx = 0;
        int i = 0;
        int beforeSeparator = 0;
        for (int separator : separators) {
            separator = separator + 1;
            int k = 0;
            Coordinate[] subCoordinate = new Coordinate[separator - beforeSeparator];
            for (; i < separator; i++) {
                subCoordinate[k] = coordinates[i];
                k++;
            }
            separatorCoordinates[idx] = subCoordinate;
            beforeSeparator = separator;
            idx++;
        }

        int k = 0;
        Coordinate[] subCoordinate = new Coordinate[coordinates.length - i];
        for (; i < coordinates.length; i++) {
            subCoordinate[k] = coordinates[i];
            k++;
        }
        separatorCoordinates[idx] = subCoordinate;
        return separatorCoordinates;
    }

    private static Point proto2Point(ProtoFeature.Point pPoint, GeometryFactory geometryFactory) {
        double x = pPoint.getX();
        double y = pPoint.getY();
        Coordinate coordinate = new Coordinate(x, y);
        if (pPoint.hasField(FieldDescriptor_Point_z)) {
            coordinate.setZ(pPoint.getZ());
        }
        return geometryFactory.createPoint(coordinate);
    }

    private static LineString proto2LineString(ProtoFeature.LineString pLineString, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = proto2Coordinates(pLineString.getXsList(), pLineString.getYsList(), pLineString.getZsList());
        return geometryFactory.createLineString(coordinates);
    }

    private static LinearRing coordinates2LinearRing(Coordinate[] coordinates, GeometryFactory geometryFactory) {
        //coordinates的最后一个坐标被省略了，所以这里手工补上后才能转为环
        Coordinate[] ringCoordinates = new Coordinate[coordinates.length + 1];
        System.arraycopy(coordinates, 0, ringCoordinates, 0, coordinates.length);
        ringCoordinates[coordinates.length] = coordinates[0].copy();
        return geometryFactory.createLinearRing(ringCoordinates);
    }

    private static Polygon proto2Polygon(Coordinate[][] separatorCoordinates, GeometryFactory geometryFactory) {
        LinearRing shell = coordinates2LinearRing(separatorCoordinates[0], geometryFactory);
        LinearRing[] holes = new LinearRing[separatorCoordinates.length - 1];
        for (int i = 1; i < separatorCoordinates.length; i++) {
            holes[i - 1] = coordinates2LinearRing(separatorCoordinates[i], geometryFactory);
        }
        return geometryFactory.createPolygon(shell, holes);
    }

    private static Polygon proto2Polygon(ProtoFeature.Polygon pPolygon, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = proto2Coordinates(pPolygon.getXsList(), pPolygon.getYsList(), pPolygon.getZsList());
        List<Integer> separators = pPolygon.getSeparatorsList();
        if (separators.size() == 0) {
            return geometryFactory.createPolygon(coordinates2LinearRing(coordinates, geometryFactory));
        } else {
            return proto2Polygon(separatorCoordinates(coordinates, separators), geometryFactory);
        }
    }

    private static MultiPoint proto2MultiPoint(ProtoFeature.MultiPoint pMultiPoint, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = proto2Coordinates(pMultiPoint.getXsList(), pMultiPoint.getYsList(), pMultiPoint.getZsList());
        return geometryFactory.createMultiPointFromCoords(coordinates);
    }


    private static MultiLineString proto2MultiLineString(ProtoFeature.MultiLineString pMultiLineString, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = proto2Coordinates(pMultiLineString.getXsList(), pMultiLineString.getYsList(), pMultiLineString.getZsList());
        List<Integer> separators = pMultiLineString.getSeparatorsList();
        if (separators.size() == 0) {
            return geometryFactory.createMultiLineString(new LineString[]{geometryFactory.createLineString(coordinates)});
        } else {
            Coordinate[][] separatorCoordinates = separatorCoordinates(coordinates, separators);
            LineString[] lineStrings = new LineString[separatorCoordinates.length];
            for (int i = 0; i < lineStrings.length; i++) {
                lineStrings[i] = geometryFactory.createLineString(separatorCoordinates[i]);
            }
            return geometryFactory.createMultiLineString(lineStrings);
        }
    }

    private static MultiPolygon proto2MultiPolygon(ProtoFeature.MultiPolygon pMultiPolygon, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = proto2Coordinates(pMultiPolygon.getXsList(), pMultiPolygon.getYsList(), pMultiPolygon.getZsList());
        ArrayList<Integer> polygonSeparators;
        {
            List<Integer> list = pMultiPolygon.getPolygonSeparatorsList();
            polygonSeparators = new ArrayList<>(list.size() + 1);
            polygonSeparators.addAll(list);
        }

        List<Integer> coordSeparators = pMultiPolygon.getCoordSeparatorsList();
        if (polygonSeparators.size() == 0) {
            Polygon polygon = geometryFactory.createPolygon(coordinates2LinearRing(coordinates, geometryFactory));
            return geometryFactory.createMultiPolygon(new Polygon[]{polygon});
        } else {
            Polygon[] polygons = new Polygon[polygonSeparators.size() + 1];
            int idx = 0;
            int beforePolygonSeparator = 0;
            Coordinate[][] separatorCoordinates = separatorCoordinates(coordinates, coordSeparators);
            polygonSeparators.add(separatorCoordinates.length);
            for (int polygonSeparator : polygonSeparators) {
                Coordinate[][] subSeparatorCoordinates = new Coordinate[polygonSeparator - beforePolygonSeparator][];
                int i1 = 0;
                for (int i = beforePolygonSeparator; i < polygonSeparator; i++) {
                    subSeparatorCoordinates[i1] = separatorCoordinates[i];
                    i1++;
                }
                Polygon polygon = proto2Polygon(subSeparatorCoordinates, geometryFactory);
                polygons[idx] = polygon;
                idx++;
                beforePolygonSeparator = polygonSeparator;
            }
            return geometryFactory.createMultiPolygon(polygons);
        }
    }

    private static GeometryCollection proto2GeometryCollection(ProtoFeature.GeometryCollection pGeometryCollection, GeometryFactory geometryFactory) {
        List<Geometry> geometryList = new LinkedList<>();
        for (ProtoFeature.Point point : pGeometryCollection.getPointsList()) {
            geometryList.add(proto2Point(point, geometryFactory));
        }

        for (ProtoFeature.LineString lineString : pGeometryCollection.getLineStringsList()) {
            geometryList.add(proto2LineString(lineString, geometryFactory));
        }

        for (ProtoFeature.Polygon polygon : pGeometryCollection.getPolygonsList()) {
            geometryList.add(proto2Polygon(polygon, geometryFactory));
        }

        for (ProtoFeature.MultiPoint multiPoint : pGeometryCollection.getMultiPointsList()) {
            geometryList.add(proto2MultiPoint(multiPoint, geometryFactory));
        }

        for (ProtoFeature.MultiLineString multiLineString : pGeometryCollection.getMultiLineStringsList()) {
            geometryList.add(proto2MultiLineString(multiLineString, geometryFactory));
        }

        for (ProtoFeature.MultiPolygon multiPolygon : pGeometryCollection.getMultiPolygonsList()) {
            geometryList.add(proto2MultiPolygon(multiPolygon, geometryFactory));
        }

        for (ProtoFeature.GeometryCollection geometryCollection : pGeometryCollection.getGeometryCollectionsList()) {
            geometryList.add(proto2GeometryCollection(geometryCollection, geometryFactory));
        }

        Geometry[] geometries = new Geometry[geometryList.size()];
        geometryList.toArray(geometries);
        return geometryFactory.createGeometryCollection(geometries);
    }

    /**
     * Feature 转 ProtoFeature bytes
     *
     * @param feature
     * @return ProtoFeature bytes
     */
    public static byte[] feature2Proto(Feature feature) {
        FeatureCollection fc = new FeatureCollection();
        fc.setFeatures(List.of(feature));
        return featureCollection2Proto(fc);
    }

    /**
     * FeatureCollection 转 ProtoFeature bytes
     *
     * @param featureCollection FeatureCollection
     * @return ProtoFeature bytes
     */
    public static byte[] featureCollection2Proto(FeatureCollection featureCollection) {
        ToProtoKeyValueCell keyValueCell = new ToProtoKeyValueCell();//收集key-value与id对应关系
        ProtoFeature.FeatureCollection.Builder builder = ProtoFeature.FeatureCollection.newBuilder();
        for (Feature feature : featureCollection.getFeatures()) {
            //properties转换
            Map<String, Object> properties = feature.getProperties();
            if (null != properties) {
                ProtoFeature.Map.Builder propertiesBuilder = ProtoFeature.Map.newBuilder();
                properties.forEach((k, v) -> {
                    if (null == v) {
                        return;
                    }
                    PropertiesSetter setter = getPropertiesSetter(v);
                    setter.setKey(propertiesBuilder, keyValueCell, k);
                    setter.setValue(propertiesBuilder, keyValueCell, v);
                });
                builder.addPropertiess(propertiesBuilder);
            } else {
                builder.addPropertiess(nullMap);
            }
            //geometry转换
            Geometry geometry = feature.getGeometry();
            ProtoFeature.Geometry.Builder geometryBuilder = geometry2ProtoBuilder(geometry);
            builder.addGeometries(geometryBuilder);

        }
        keyValueCell.toProto(builder);
        return builder.build().toByteArray();
    }

    private interface PropertiesSetter {
        /**
         * 向一个mapBuilder中添加key
         *
         * @param propertiesBuilder propertiesBuilder
         * @param keyValueCell      keyValueCell
         * @param key               key
         */
        void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key);

        /**
         * 向一个mapBuilder中添加T vaule
         *
         * @param propertiesBuilder propertiesBuilder
         * @param keyValueCell      keyValueCell
         * @param value             T vaule
         */
        void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value);

        /**
         * 向一个listBuilder中添加<T> vaule
         *
         * @param propertiesBuilder propertiesBuilder
         * @param keyValueCell      keyValueCell
         * @param value             <T> vaule
         */
        void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value);
    }

    /**
     * properties存放的是key id、value id,KeyValueCell对象存放id和实际值的映射关系
     */
    private static final class ToProtoKeyValueCell {
        private static final class ReusableIndex<T> {
            //自增id
            private int index = 0;
            //实际值-id对应关系
            private final HashMap<T, Integer> indexMap = new HashMap<>();

            public Integer getId(T t) {
//                if (index < 0) {
//                    throw new RuntimeException("值数量超过上限 " + Integer.MAX_VALUE);
//                }
                Integer id = indexMap.get(t);
                if (id != null) {
                    return id;
                }
                id = index;
                indexMap.put(t, id);
                index++;
                return id;
            }

            //输出一个按id顺序的t list
            public ArrayList<T> toList() {
                ArrayList<SortT<T>> sortList = new ArrayList<>(indexMap.size());
                indexMap.forEach((t, id) -> sortList.add(new SortT(t, id)));
                sortList.sort(Comparator.comparingInt((SortT c) -> c.id));
                ArrayList<T> res = new ArrayList<>(indexMap.size());
                for (SortT<T> sortT : sortList) {
                    res.add(sortT.t);
                }
                return res;
            }

            public int size() {
                return index;
            }
        }

        private static final class NoReusableIndex<T> {
            //实际值-id对应关系
            private final List<T> list = new LinkedList<>();

            public Integer getId(T t) {
                int id = list.size();
                list.add(t);
                return id;
            }

            //输出一个按id顺序的t list
            public ArrayList<T> toList() {
                ArrayList<T> res = new ArrayList<>(list.size());
                res.addAll(list);
                return res;
            }

            public int size() {
                return list.size();
            }
        }

        private static final class SortT<T> {
            private final T t;
            private final int id;

            public SortT(T t, int id) {
                this.t = t;
                this.id = id;
            }
        }


        private final ReusableIndex<String> keys = new ReusableIndex<>();
        private final ReusableIndex<Double> doubleValues = new ReusableIndex<>();
        private final ReusableIndex<Float> floatValues = new ReusableIndex<>();
        private final ReusableIndex<Integer> sint32Values = new ReusableIndex<>();
        private final ReusableIndex<Long> sint64Values = new ReusableIndex<>();
        private final ReusableIndex<String> stringValues = new ReusableIndex<>();
        private final ReusableIndex<byte[]> bytesValues = new ReusableIndex<>();

        public void toProto(ProtoFeature.FeatureCollection.Builder builder) {
            if (keys.size() == 0) {
                return;
            }
            builder.addAllKeys(keys.toList());
            if (doubleValues.size() > 0) {
                builder.addAllDoubleValues(doubleValues.toList());
            }
            if (floatValues.size() > 0) {
                builder.addAllFloatValues(floatValues.toList());
            }
            if (sint32Values.size() > 0) {
                builder.addAllSint32Values(sint32Values.toList());
            }
            if (sint64Values.size() > 0) {
                builder.addAllSint64Values(sint64Values.toList());
            }
            if (stringValues.size() > 0) {
                builder.addAllStringValues(stringValues.toList());
            }
            if (bytesValues.size() > 0) {
                // bytes比较特殊,需要转成ByteString，所以不用toList单独写一下
                ArrayList<SortT<byte[]>> sortList = new ArrayList<>(bytesValues.size());
                bytesValues.indexMap.forEach((t, id) -> sortList.add(new SortT(t, id)));
                sortList.sort(Comparator.comparingInt((c) -> c.id));
                ArrayList<ByteString> res = new ArrayList<>(bytesValues.indexMap.size());
                for (SortT<byte[]> sortT : sortList) {
                    res.add(ByteString.copyFrom(sortT.t));
                }
                builder.addAllBytesValues(res);
            }
        }

    }


    private static final Map<Class, PropertiesSetter> propertiesSetterMap;
    private static final PropertiesSetter listPropertiesSetter;
    private static final PropertiesSetter mapPropertiesSetter;
    static {
        PropertiesSetter doublePropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addDoubleKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addDoubleValueIds(keyValueCell.doubleValues.getId((Double) value));
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(doubleValueIdsIndex);
                propertiesBuilder.addDoubleValueIds(keyValueCell.doubleValues.getId((Double) value));
            }
        };

        PropertiesSetter floatPropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addFloatKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addFloatValueIds(keyValueCell.floatValues.getId((Float) value));
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(floatValueIdsIndex);
                propertiesBuilder.addFloatValueIds(keyValueCell.floatValues.getId((Float) value));
            }
        };

        PropertiesSetter sint32PropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addSint32KeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addSint32ValueIds(keyValueCell.sint32Values.getId((Integer) value));
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(sint32ValueIdsIndex);
                propertiesBuilder.addSint32ValueIds(keyValueCell.sint32Values.getId((Integer) value));
            }
        };

        PropertiesSetter sint64PropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addSint64KeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addSint64ValueIds(keyValueCell.sint64Values.getId((Long) value));
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(sint64ValueIdsIndex);
                propertiesBuilder.addSint64ValueIds(keyValueCell.sint64Values.getId((Long) value));
            }
        };

        PropertiesSetter boolPropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addBoolKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addBoolValues((Boolean) value);
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(boolValuesIndex);
                propertiesBuilder.addBoolValues((Boolean) value);
            }
        };

        PropertiesSetter stringPropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addStringKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addStringValueIds(keyValueCell.stringValues.getId((String) value));
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(stringValueIdsIndex);
                propertiesBuilder.addStringValueIds(keyValueCell.stringValues.getId((String) value));
            }
        };

        PropertiesSetter bytesPropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addBytesKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addBytesValueIds(keyValueCell.bytesValues.getId((byte[]) value));
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(bytesValueIdsIndex);
                propertiesBuilder.addBytesValueIds(keyValueCell.bytesValues.getId((byte[]) value));
            }
        };

        mapPropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addSubMapKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                ProtoFeature.Map.Builder subBuilder = createMapBuilder(keyValueCell, value);
                propertiesBuilder.addSubMapValues(subBuilder);
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(mapValuesIndex);
                ProtoFeature.Map.Builder subBuilder = createMapBuilder(keyValueCell, value);
                propertiesBuilder.addMapValues(subBuilder);
            }

            private ProtoFeature.Map.Builder createMapBuilder(ToProtoKeyValueCell keyValueCell, Object value) {
                Map<String, Object> subProperties = (Map<String, Object>) value;
                ProtoFeature.Map.Builder subBuilder = ProtoFeature.Map.newBuilder();
                subProperties.forEach((k, v) -> {
                    if (null == v) {
                        return;
                    }
                    PropertiesSetter setter = getPropertiesSetter(v);
                    setter.setKey(subBuilder, keyValueCell, k);
                    setter.setValue(subBuilder, keyValueCell, v);
                });
                return subBuilder;
            }
        };

         listPropertiesSetter = new PropertiesSetter() {
            @Override
            public void setKey(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, String key) {
                propertiesBuilder.addListKeyIds(keyValueCell.keys.getId(key));
            }

            @Override
            public void setValue(ProtoFeature.Map.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                ProtoFeature.List.Builder listBuilder = createListBuilder(keyValueCell, value);
                propertiesBuilder.addListValues(listBuilder);
            }

            @Override
            public void setValue(ProtoFeature.List.Builder propertiesBuilder, ToProtoKeyValueCell keyValueCell, Object value) {
                propertiesBuilder.addIndexes(subListValuesIndex);
                ProtoFeature.List.Builder listBuilder = createListBuilder(keyValueCell, value);
                propertiesBuilder.addSubListValues(listBuilder);
            }

            private ProtoFeature.List.Builder createListBuilder(ToProtoKeyValueCell keyValueCell, Object value) {
                ProtoFeature.List.Builder builder = ProtoFeature.List.newBuilder();
                List<Object> listProperties = (List<Object>) value;
                for (Object v : listProperties) {
                    if (null == v) {
                        continue;
                    }
                    PropertiesSetter setter = getPropertiesSetter(v);
                    setter.setValue(builder, keyValueCell, v);
                }
                return builder;
            }
        };
        Map.Entry<Class, PropertiesSetter>[] entries = new Map.Entry[]{
                new AbstractMap.SimpleEntry(double.class, doublePropertiesSetter),
                new AbstractMap.SimpleEntry(Double.class, doublePropertiesSetter),
                new AbstractMap.SimpleEntry(float.class, floatPropertiesSetter),
                new AbstractMap.SimpleEntry(Float.class, floatPropertiesSetter),
                new AbstractMap.SimpleEntry(int.class, sint32PropertiesSetter),
                new AbstractMap.SimpleEntry(Integer.class, sint32PropertiesSetter),
                new AbstractMap.SimpleEntry(long.class, sint64PropertiesSetter),
                new AbstractMap.SimpleEntry(Long.class, sint64PropertiesSetter),
                new AbstractMap.SimpleEntry(boolean.class, boolPropertiesSetter),
                new AbstractMap.SimpleEntry(Boolean.class, boolPropertiesSetter),
                new AbstractMap.SimpleEntry(String.class, stringPropertiesSetter),
                new AbstractMap.SimpleEntry(byte[].class, bytesPropertiesSetter),
                new AbstractMap.SimpleEntry(HashMap.class, mapPropertiesSetter),
                new AbstractMap.SimpleEntry(ArrayList.class, listPropertiesSetter),
                new AbstractMap.SimpleEntry(LinkedList.class, listPropertiesSetter),
        };
        propertiesSetterMap = Map.ofEntries(entries);
    }

    private static PropertiesSetter getPropertiesSetter(Object value) {
        PropertiesSetter setter = propertiesSetterMap.get(value.getClass());
        if (null == setter) {
            if (value instanceof Map){
                setter = mapPropertiesSetter;
            } else if (value instanceof List) {
                setter = listPropertiesSetter;
            }else {
                throw new RuntimeException("未知对象类型 " + value.getClass());
            }
        }
        return setter;
    }


    /**
     * ProtoFeature bytes 转 Feature
     *
     * @param bytes           ProtoFeature bytes
     * @param geometryFactory jts GeometryFactory
     * @return Feature
     */
    public static Feature proto2feature(byte[] bytes, GeometryFactory geometryFactory) {
        FeatureCollection fc = proto2featureCollection(bytes, geometryFactory);
        return fc.getFeatures().get(0);
    }

    /**
     * ProtoFeature bytes 转 FeatureCollection
     *
     * @param bytes           ProtoFeature bytes
     * @param geometryFactory jts GeometryFactory
     * @return FeatureCollection
     */
    public static FeatureCollection proto2featureCollection(byte[] bytes, GeometryFactory geometryFactory) {
        ProtoFeature.FeatureCollection pFeatureCollection;
        try {
            pFeatureCollection = ProtoFeature.FeatureCollection.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        //构造Properties 真实值
        FromProtoKeyValueCell keyValueCell = new FromProtoKeyValueCell(pFeatureCollection);

        //构造feature
        int featureNum = pFeatureCollection.getGeometriesCount();
        ArrayList<Feature> features = new ArrayList<>(featureNum);
        for (int i = 0; i < featureNum; i++) {
            Feature feature = new Feature();

            //geometry转换
            ProtoFeature.Geometry pGeometry = pFeatureCollection.getGeometries(i);
            Geometry geometry = proto2Geometry(pGeometry, geometryFactory);
            feature.setGeometry(geometry);
            //properties转换
            ProtoFeature.Map pProperties = pFeatureCollection.getPropertiess(i);
            feature.setProperties(keyValueCell.parseProperties(pProperties));

            features.add(feature);
        }

        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(features);
        return featureCollection;
    }

    /**
     * properties存放的是key id、value id,KeyValueCell对象存放id和实际值的映射关系
     */
    private static final class FromProtoKeyValueCell {
        private final HashMap<Integer, String> keyMap;
        private final HashMap<Integer, Double> doubleValueMap;
        private final HashMap<Integer, Float> floatValueMap;
        private final HashMap<Integer, Integer> sint32ValueMap;
        private final HashMap<Integer, Long> sint64ValueMap;
        private final HashMap<Integer, String> stringValueMap;
        private final HashMap<Integer, byte[]> bytesValueMap;

        public FromProtoKeyValueCell(ProtoFeature.FeatureCollection pFeatureCollection) {
            int n;
            n = pFeatureCollection.getKeysCount();
            keyMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                keyMap.put(i, pFeatureCollection.getKeys(i));
            }

            n = pFeatureCollection.getDoubleValuesCount();
            doubleValueMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                doubleValueMap.put(i, pFeatureCollection.getDoubleValues(i));
            }

            n = pFeatureCollection.getFloatValuesCount();
            floatValueMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                floatValueMap.put(i, pFeatureCollection.getFloatValues(i));
            }

            n = pFeatureCollection.getSint32ValuesCount();
            sint32ValueMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                sint32ValueMap.put(i, pFeatureCollection.getSint32Values(i));
            }

            n = pFeatureCollection.getSint64ValuesCount();
            sint64ValueMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                sint64ValueMap.put(i, pFeatureCollection.getSint64Values(i));
            }


            n = pFeatureCollection.getStringValuesCount();
            stringValueMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                stringValueMap.put(i, pFeatureCollection.getStringValues(i));
            }

            n = pFeatureCollection.getBytesValuesCount();
            bytesValueMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) {
                bytesValueMap.put(i, pFeatureCollection.getBytesValues(i).toByteArray());
            }
        }

        private static void putValueToMap(HashMap<String, Object> map, List<Integer> keyIdList, List<Integer> valueIdList
                , HashMap<Integer, String> keyMap, Map<Integer, ?> valueMap) {
            if (null == keyIdList || keyIdList.size() == 0) {
                return;
            }
            Iterator<Integer> keyIdIterator = keyIdList.iterator();
            Iterator<Integer> valueIdIterator = valueIdList.iterator();
            while (keyIdIterator.hasNext()) {
                Integer keyId = keyIdIterator.next();
                Integer valueId = valueIdIterator.next();
                String key = keyMap.get(keyId);
                Object value = valueMap.get(valueId);
                map.put(key, value);
            }
        }

        private HashMap<String, Object> parseMap(ProtoFeature.Map pMap) {
            HashMap<String, Object> map = new HashMap<>();
            //基本值
            putValueToMap(map, pMap.getDoubleKeyIdsList(), pMap.getDoubleValueIdsList(), keyMap, doubleValueMap);
            putValueToMap(map, pMap.getFloatKeyIdsList(), pMap.getFloatValueIdsList(), keyMap, floatValueMap);
            putValueToMap(map, pMap.getSint32KeyIdsList(), pMap.getSint32ValueIdsList(), keyMap, sint32ValueMap);
            putValueToMap(map, pMap.getSint64KeyIdsList(), pMap.getSint64ValueIdsList(), keyMap, sint64ValueMap);
            {
                // bool直接存值，所以单独处理
                Iterator<Integer> keyIdIterator = pMap.getBoolKeyIdsList().iterator();
                Iterator<Boolean> valueIterator = pMap.getBoolValuesList().stream().iterator();
                while (keyIdIterator.hasNext()) {
                    Integer keyId = keyIdIterator.next();
                    boolean value = valueIterator.next();
                    String key = keyMap.get(keyId);
                    map.put(key, value);
                }
            }
            putValueToMap(map, pMap.getStringKeyIdsList(), pMap.getStringValueIdsList(), keyMap, stringValueMap);
            putValueToMap(map, pMap.getBytesKeyIdsList(), pMap.getBytesValueIdsList(), keyMap, bytesValueMap);
            {
                //subMap
                List<Integer> keyIdList = pMap.getSubMapKeyIdsList();
                if (null != keyIdList && keyIdList.size() > 0) {
                    Iterator<Integer> keyIdIterator = keyIdList.iterator();
                    Iterator<ProtoFeature.Map> valueIdIterator = pMap.getSubMapValuesList().iterator();
                    while (keyIdIterator.hasNext()) {
                        Integer keyId = keyIdIterator.next();
                        String key = keyMap.get(keyId);
                        ProtoFeature.Map subPMap = valueIdIterator.next();
                        HashMap<String, Object> subMap = parseMap(subPMap);
                        map.put(key, subMap);
                    }
                }
            }
            {
                //list
                List<Integer> keyIdList = pMap.getListKeyIdsList();
                if (null != keyIdList && keyIdList.size() > 0) {
                    Iterator<Integer> keyIdIterator = keyIdList.iterator();
                    Iterator<ProtoFeature.List> valueIdIterator = pMap.getListValuesList().iterator();
                    while (keyIdIterator.hasNext()) {
                        Integer keyId = keyIdIterator.next();
                        String key = keyMap.get(keyId);
                        ProtoFeature.List pList = valueIdIterator.next();
                        ArrayList<Object> list = parseList(pList);
                        map.put(key, list);
                    }
                }
            }
            return map;
        }

        private ArrayList<Object> parseList(ProtoFeature.List pList) {
            // indexes 标注list中的第n个元素的类型是什么类型，如[1L,2D,'SSS'] 的indexes为 [5,2,7]
            /*
            	// valueId/value
                repeated int32 doubleValueIds = 2;
                repeated int32 floatValueIds = 3;
                repeated int32 sint32ValueIds = 4;
                repeated int32 sint64ValueIds = 5;
                repeated bool boolValues = 6;
                repeated int32 stringValueIds = 7;
                repeated int32 bytesValueIds = 8;
                // map
                repeated Map mapValues = 9;
                // children
                repeated List subListValues = 10;
            * */
            List<Integer> indexes = pList.getIndexesList();
            Iterator<Integer> doubleValueIdsIterator = pList.getDoubleValueIdsList().iterator();
            Iterator<Integer> floatValueIdsIterator = pList.getFloatValueIdsList().iterator();
            Iterator<Integer> sint32ValueIdsIterator = pList.getSint32ValueIdsList().iterator();
            Iterator<Integer> sint64ValueIdsIterator = pList.getSint64ValueIdsList().iterator();
            Iterator<Boolean> boolValuesIterator = pList.getBoolValuesList().iterator();
            Iterator<Integer> stringValueIdsIterator = pList.getStringValueIdsList().iterator();
            Iterator<Integer> bytesValueIdsIterator = pList.getBytesValueIdsList().iterator();
            Iterator<ProtoFeature.Map> mapValuesIterator = pList.getMapValuesList().iterator();
            Iterator<ProtoFeature.List> subListValuesIterator = pList.getSubListValuesList().iterator();

            ArrayList<Object> list = new ArrayList<>(indexes.size());

            for (Integer index : indexes) {
                Object value;
                Integer valueId;
                switch (index) {
                    case doubleValueIdsIndex:
                        valueId = doubleValueIdsIterator.next();
                        value = doubleValueMap.get(valueId);
                        break;
                    case floatValueIdsIndex:
                        valueId = floatValueIdsIterator.next();
                        value = floatValueMap.get(valueId);
                        break;
                    case sint32ValueIdsIndex:
                        valueId = sint32ValueIdsIterator.next();
                        value = sint32ValueMap.get(valueId);
                        break;
                    case sint64ValueIdsIndex:
                        valueId = sint64ValueIdsIterator.next();
                        value = sint64ValueMap.get(valueId);
                        break;
                    case boolValuesIndex:
                        value = boolValuesIterator.next();
                        break;
                    case stringValueIdsIndex:
                        valueId = stringValueIdsIterator.next();
                        value = stringValueMap.get(valueId);
                        break;
                    case bytesValueIdsIndex:
                        valueId = bytesValueIdsIterator.next();
                        value = bytesValueMap.get(valueId);
                        break;
                    case mapValuesIndex:
                        ProtoFeature.Map pMap = mapValuesIterator.next();
                        value = parseMap(pMap);
                        break;
                    case subListValuesIndex:
                        ProtoFeature.List subList = subListValuesIterator.next();
                        value = parseList(subList);
                        break;
                    default:
                        throw new RuntimeException("未知index类型 " + index);
                }
                list.add(value);
            }
            return list;
        }

        public Map<String, Object> parseProperties(ProtoFeature.Map pProperties) {
            return parseMap(pProperties);
        }
    }
}
