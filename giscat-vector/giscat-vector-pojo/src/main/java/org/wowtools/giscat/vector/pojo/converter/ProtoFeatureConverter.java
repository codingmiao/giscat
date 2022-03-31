package org.wowtools.giscat.vector.pojo.converter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import org.locationtech.jts.geom.*;
import org.wowtools.giscat.vector.pojo.proto.ProtoFeature;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * ProtoFeature bytes与Feature的相互转换
 *
 * @author liuyu
 * @date 2022/3/25
 */
public class ProtoFeatureConverter {

    /**
     * geometry 转 ProtoFeature bytes
     *
     * @param geometry geometry
     * @return ProtoFeature bytes
     */
    public static byte[] geometry2Proto(Geometry geometry) {
        ProtoFeature.Geometry.Builder geometryBuilder = ProtoFeature.Geometry.newBuilder();
        if (geometry instanceof Point) {
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

        return geometryBuilder.build().toByteArray();
    }

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
            separators.add(k + 1);
            for (int i = 0; i < holeNum; i++) {
                Coordinate[] childCoordinates = polygon.getInteriorRingN(i).getCoordinates();
                for (int j = 0; j < childCoordinates.length - 1; j++) {//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
                    k++;
                    coordinates[k] = childCoordinates[j];
                }
                if (i < holeNum - 1) {
                    separators.add(k + 1);
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
            for (int j = 0; j < childCoordinates.length; j++) {
                k++;
                coordinates[k] = childCoordinates[j];
            }
            if (i < lineNum - 1) {
                separators.add(k + 1);
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
            coordSeparators.add(k + 1);
            if (holeNum > 0) {
                for (int i = 0; i < holeNum; i++) {
                    Coordinate[] childCoordinates = polygon.getInteriorRingN(i).getCoordinates();
                    for (int j = 0; j < childCoordinates.length - 1; j++) {//环的最后一个坐标和第一个相同，所以这里省掉最后一个坐标
                        k++;
                        coordinateList.add(childCoordinates[j]);
                    }
                    coordSeparators.add(k + 1);
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
        throw new RuntimeException("解析pGeometry逻辑错误");
    }

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
        for (int i = 0; i < coordinates.length; i++) {
            ringCoordinates[i] = coordinates[i];
        }
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
}
