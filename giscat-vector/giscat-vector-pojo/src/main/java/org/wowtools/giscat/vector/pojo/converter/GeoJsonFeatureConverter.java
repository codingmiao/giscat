package org.wowtools.giscat.vector.pojo.converter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.*;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * feature与geojson互转 线程安全
 *
 * @author liuyu
 * @date 2022/3/15
 */
public class GeoJsonFeatureConverter {

    /**
     * 将feature对象转为GeoJson
     *
     * @param feature feature
     * @return geojson
     */
    public static JSONObject toGeoJson(Feature feature) {
        JSONObject jo = new JSONObject();
        jo.put("type", "Feature");
        if (null != feature.getGeometry()) {
            jo.put("geometry", geometry2GeoJson(feature.getGeometry()));
        }
        Map<String, Object> properties = feature.getProperties();
        if (null != properties && properties.size() > 0) {
            jo.put("properties", feature.getProperties());
        }
        return jo;
    }

    /**
     * 将featureCollection对象转为GeoJson
     *
     * @param featureCollection featureCollection
     * @return geojson
     */
    public static JSONObject toGeoJson(FeatureCollection featureCollection) {
        JSONObject jo = new JSONObject();
        jo.put("type", "FeatureCollection");
        JSONArray jaFeatures = new JSONArray();
        for (Feature feature : featureCollection.getFeatures()) {
            jaFeatures.put(toGeoJson(feature));
        }
        jo.put("features", jaFeatures);
        return jo;
    }


    public static JSONObject geometry2GeoJson(Geometry geometry) {
        if (geometry instanceof Point) {
            return point2GeoJson(geometry);
        }
        if (geometry instanceof LineString) {
            return lineString2GeoJson(geometry);
        }
        if (geometry instanceof Polygon) {
            return polygon2GeoJson(geometry);
        }

        if (geometry instanceof MultiPoint) {
            return multiPoint2GeoJson(geometry);
        }
        if (geometry instanceof MultiLineString) {
            return multiLineString2GeoJson(geometry);
        }
        if (geometry instanceof MultiPolygon) {
            return multiPolygon2GeoJson(geometry);
        }

        if (geometry instanceof GeometryCollection) {
            return geometryCollection2GeoJson(geometry);
        }
        //发现其他类型参考 https://en.wikipedia.org/wiki/GeoJSON 补充实现
        throw new RuntimeException("暂未实现的geometry类型 " + geometry.getGeometryType());
    }

    private static JSONObject point2GeoJson(Geometry geometry) {
        Point point = (Point) geometry;
        JSONObject jo = new JSONObject();
        jo.put("type", "Point");
        jo.put("coordinates", coordinate2Ja(point.getCoordinate()));
        return jo;
    }

    private static JSONObject lineString2GeoJson(Geometry geometry) {
        LineString lineString = (LineString) geometry;
        JSONObject jo = new JSONObject();
        jo.put("type", "LineString");
        jo.put("coordinates", coordinates2Ja(lineString.getCoordinates()));
        return jo;
    }

    private static JSONObject polygon2GeoJson(Geometry geometry) {
        Polygon polygon = (Polygon) geometry;
        JSONObject jo = new JSONObject();
        jo.put("type", "Polygon");
        JSONArray rings = new JSONArray();
        rings.put(coordinates2Ja(polygon.getExteriorRing().getCoordinates()));
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            rings.put(coordinates2Ja(polygon.getInteriorRingN(i).getCoordinates()));
        }
        jo.put("coordinates", rings);
        return jo;
    }

    private static JSONObject multiPoint2GeoJson(Geometry geometry) {
        MultiPoint multiPoint = (MultiPoint) geometry;
        JSONObject jo = new JSONObject();
        jo.put("type", "MultiPoint");
        jo.put("coordinates", coordinates2Ja(multiPoint.getCoordinates()));
        return jo;
    }

    private static JSONObject multiLineString2GeoJson(Geometry geometry) {
        MultiLineString multiLineString = (MultiLineString) geometry;
        JSONObject jo = new JSONObject();
        jo.put("type", "MultiLineString");
        JSONArray lines = new JSONArray();
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            lines.put(coordinates2Ja(multiLineString.getGeometryN(i).getCoordinates()));
        }
        jo.put("coordinates", lines);
        return jo;
    }

    private static JSONObject multiPolygon2GeoJson(Geometry geometry) {
        MultiPolygon multiPolygon = (MultiPolygon) geometry;
        JSONObject jo = new JSONObject();
        jo.put("type", "MultiPolygon");
        JSONArray polygons = new JSONArray();
        for (int j = 0; j < multiPolygon.getNumGeometries(); j++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(j);
            JSONArray rings = new JSONArray();
            rings.put(coordinates2Ja(polygon.getExteriorRing().getCoordinates()));
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                rings.put(coordinates2Ja(polygon.getInteriorRingN(i).getCoordinates()));
            }
            polygons.put(rings);
        }
        jo.put("coordinates", polygons);
        return jo;
    }

    private static JSONObject geometryCollection2GeoJson(Geometry geometry) {
        JSONObject jo = new JSONObject();
        jo.put("type", "GeometryCollection");
        JSONArray geometries = new JSONArray();
        GeometryCollection geometryCollection = (GeometryCollection) geometry;
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            JSONObject sub = geometry2GeoJson(geometry.getGeometryN(i));
            geometries.put(sub);
        }
        jo.put("geometries", geometries);
        return jo;
    }


    private static JSONArray coordinate2Ja(Coordinate coordinate) {
        JSONArray ja = new JSONArray();
        ja.put(coordinate.x);
        ja.put(coordinate.y);
        return ja;
    }

    private static JSONArray coordinates2Ja(Coordinate[] coordinates) {
        JSONArray ja = new JSONArray();
        for (Coordinate coordinate : coordinates) {
            ja.put(coordinate2Ja(coordinate));
        }
        return ja;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 将GeoJson对象转为Feature
     *
     * @param geoJsonFeature  geoJsonFeature
     * @param geometryFactory jts GeometryFactory
     * @return Feature
     */
    public static Feature fromGeoJsonFeature(JSONObject geoJsonFeature, GeometryFactory geometryFactory) {
        Feature feature = new Feature();
        JSONObject joGeometry = geoJsonFeature.optJSONObject("geometry");
        if (null != joGeometry) {
            feature.setGeometry(geoJson2Geometry(joGeometry, geometryFactory));
        }
        JSONObject joProperties = geoJsonFeature.optJSONObject("properties");
        if (joProperties != null) {
            feature.setProperties(joProperties.toMap());
        }
        return feature;
    }

    public static Geometry geoJson2Geometry(JSONObject joGeometry, GeometryFactory geometryFactory) {
        String type = joGeometry.getString("type");
        switch (type) {
            case "Point":
                return geoJson2Point(joGeometry, geometryFactory);
            case "LineString":
                return geoJson2LineString(joGeometry, geometryFactory);
            case "Polygon":
                return geoJson2Polygon(joGeometry, geometryFactory);
            case "MultiPoint":
                return geoJson2MultiPoint(joGeometry, geometryFactory);
            case "MultiLineString":
                return geoJson2MultiLineString(joGeometry, geometryFactory);
            case "MultiPolygon":
                return geoJson2MultiPolygon(joGeometry, geometryFactory);
            case "GeometryCollection":
                return geoJson2GeometryCollection(joGeometry, geometryFactory);
            default:
                //发现其他类型参考 https://en.wikipedia.org/wiki/GeoJSON 补充实现
                throw new RuntimeException("暂未实现的geometry类型 " + type);
        }

    }

    private static Point geoJson2Point(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray coordinates = joGeometry.getJSONArray("coordinates");
        return geometryFactory.createPoint(ja2Coordinate(coordinates));
    }

    private static LineString geoJson2LineString(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray coordinates = joGeometry.getJSONArray("coordinates");
        return geometryFactory.createLineString(jas2Coordinate(coordinates));
    }

    private static Polygon geoJson2Polygon(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray rings = joGeometry.getJSONArray("coordinates");
        if (rings.length() == 1) {
            JSONArray ring = rings.getJSONArray(0);
            return geometryFactory.createPolygon(jas2Coordinate(ring));
        } else {
            //FIXME 效率起见，认为第一个环是shell，而没有按顺时针逆时针判断
            LinearRing shell = geometryFactory.createLinearRing(jas2Coordinate(rings.getJSONArray(0)));
            LinearRing[] holes = new LinearRing[rings.length() - 1];
            for (int i = 0; i < holes.length; i++) {
                holes[i] = geometryFactory.createLinearRing(jas2Coordinate(rings.getJSONArray(i + 1)));
            }
            return geometryFactory.createPolygon(shell, holes);
        }
    }


    private static MultiPoint geoJson2MultiPoint(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray jaCoordinates = joGeometry.getJSONArray("coordinates");
        Coordinate[] coordinates = jas2Coordinate(jaCoordinates);
        Point[] points = new Point[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            points[i] = geometryFactory.createPoint(coordinates[i]);
        }
        return geometryFactory.createMultiPoint(points);
    }

    private static MultiLineString geoJson2MultiLineString(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray jaCoordinates = joGeometry.getJSONArray("coordinates");
        LineString[] lineStrings = new LineString[jaCoordinates.length()];
        for (int i = 0; i < lineStrings.length; i++) {
            Coordinate[] lineCoords = jas2Coordinate(jaCoordinates.getJSONArray(i));
            lineStrings[i] = geometryFactory.createLineString(lineCoords);
        }
        return geometryFactory.createMultiLineString(lineStrings);
    }

    private static MultiPolygon geoJson2MultiPolygon(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray jaPolygons = joGeometry.getJSONArray("coordinates");
        Polygon[] polygons = new Polygon[jaPolygons.length()];
        for (int pi = 0; pi < polygons.length; pi++) {
            JSONArray jaRings = jaPolygons.getJSONArray(pi);
            LinearRing shell = geometryFactory.createLinearRing(jas2Coordinate(jaRings.getJSONArray(0)));
            LinearRing[] holes = new LinearRing[jaRings.length() - 1];
            for (int i = 0; i < holes.length; i++) {
                JSONArray jaRing = jaRings.getJSONArray(i + 1);
                Coordinate[] ringCoords = jas2Coordinate(jaRing);
                holes[i] = geometryFactory.createLinearRing(ringCoords);
            }
            polygons[pi] = geometryFactory.createPolygon(shell, holes);
        }
        return geometryFactory.createMultiPolygon(polygons);
    }

    private static GeometryCollection geoJson2GeometryCollection(JSONObject joGeometry, GeometryFactory geometryFactory) {
        JSONArray jaGeometries = joGeometry.getJSONArray("geometries");
        Geometry[] geometries = new Geometry[jaGeometries.length()];
        for (int i = 0; i < geometries.length; i++) {
            geometries[i] = geoJson2Geometry(jaGeometries.getJSONObject(i), geometryFactory);
        }
        return geometryFactory.createGeometryCollection(geometries);
    }

    private static Coordinate ja2Coordinate(JSONArray ja) {
        return new Coordinate(ja.getDouble(0), ja.getDouble(1));
    }

    private static Coordinate[] jas2Coordinate(JSONArray jas) {
        Coordinate[] coordinates = new Coordinate[jas.length()];
        for (int i = 0; i < jas.length(); i++) {
            coordinates[i] = ja2Coordinate(jas.getJSONArray(i));
        }
        return coordinates;
    }

    /**
     * 将GeoJson对象转为Feature
     *
     * @param geoJsonFeatureCollection geoJsonFeatureCollection
     * @param geometryFactory          geometryFactory
     * @return FeatureCollection
     */
    public static FeatureCollection fromGeoJsonFeatureCollection(JSONObject geoJsonFeatureCollection, GeometryFactory geometryFactory) {
        JSONArray jaFeatures = geoJsonFeatureCollection.getJSONArray("features");
        ArrayList<Feature> features = new ArrayList<>(jaFeatures.length());
        for (Object o : jaFeatures) {
            JSONObject joFeature = (JSONObject) o;
            Feature feature = fromGeoJsonFeature(joFeature, geometryFactory);
            features.add(feature);
        }
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(features);
        return featureCollection;
    }

}
