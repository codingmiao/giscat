/*
 * Copyright (c) 2022- "giscat,"
 *
 * This file is part of giscat.
 *
 * giscat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wowtools.giscat.vector.pojo.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.pojo.GeoJsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * feature与geojson互转 线程安全
 *
 * @author liuyu
 * @date 2022/3/15
 */
public class GeoJsonFeatureConverter {

    /**
     * jackson ObjectMapper
     */
    public static final ObjectMapper mapper = new ObjectMapper();

    private static final JavaType typeGeometry = mapper.constructType(GeoJsonObject.Geometry.class);
    private static final JavaType typeFeature = mapper.constructType(GeoJsonObject.Feature.class);
    private static final JavaType typeFeatureCollection = mapper.constructType(GeoJsonObject.FeatureCollection.class);


    //////////////////////////////////////////////////////////////////////////////////////// feature -> geojson

    /**
     * 将geometry对象转为GeoJson
     *
     * @param geometry geometry
     * @return geojson
     */
    public static GeoJsonObject.Geometry geometry2GeoJson(Geometry geometry) {
        if (null == geometry) {
            return null;
        }
        if (geometry instanceof org.locationtech.jts.geom.Point) {
            return new GeoJsonObject.Point((org.locationtech.jts.geom.Point) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.LineString) {
            return new GeoJsonObject.LineString((org.locationtech.jts.geom.LineString) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.Polygon) {
            return new GeoJsonObject.Polygon((org.locationtech.jts.geom.Polygon) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiPoint) {
            return new GeoJsonObject.MultiPoint((org.locationtech.jts.geom.MultiPoint) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiLineString) {
            return new GeoJsonObject.MultiLineString((org.locationtech.jts.geom.MultiLineString) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiPolygon) {
            return new GeoJsonObject.MultiPolygon((org.locationtech.jts.geom.MultiPolygon) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.GeometryCollection) {
            return new GeoJsonObject.GeometryCollection((org.locationtech.jts.geom.GeometryCollection) geometry);
        } else {
            throw new RuntimeException("未知类型 " + geometry.getGeometryType());
        }
    }

    /**
     * 将feature对象转为GeoJson
     *
     * @param feature feature
     * @return geojson
     */
    public static GeoJsonObject.Feature toGeoJson(Feature feature) {
        GeoJsonObject.Feature geoJsonFeature = new GeoJsonObject.Feature();
        geoJsonFeature.setGeometry(geometry2GeoJson(feature.getGeometry()));
        geoJsonFeature.setProperties(feature.getProperties());
        return geoJsonFeature;
    }

    /**
     * 将featureCollection对象转为GeoJson
     *
     * @param featureCollection featureCollection
     * @return geojson
     */
    public static GeoJsonObject.FeatureCollection toGeoJson(FeatureCollection featureCollection) {
        GeoJsonObject.Feature[] geoJsonFeatures = new GeoJsonObject.Feature[featureCollection.getFeatures().size()];
        int i = 0;
        GeoJsonObject.FeatureCollection geoJsonFeatureCollection = new GeoJsonObject.FeatureCollection();
        for (Feature feature : featureCollection.getFeatures()) {
            geoJsonFeatures[i] = toGeoJson(feature);
            i++;
        }
        geoJsonFeatureCollection.setFeatures(geoJsonFeatures);
        return geoJsonFeatureCollection;
    }


    //////////////////////////////////////////////////////////////////////////////////////// feature <- geojson

    /**
     * 将GeoJson对象转为Geometry
     *
     * @param strGeojsonGeometry geojsonGeometry string
     * @param geometryFactory    jts GeometryFactory
     * @return jts Geometry
     */
    public static Geometry geoJson2Geometry(String strGeojsonGeometry, GeometryFactory geometryFactory) {
        GeoJsonObject.Geometry geojsonGeometry;
        try {
            geojsonGeometry = mapper.readValue(strGeojsonGeometry, typeGeometry);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return geoJson2Geometry(geojsonGeometry, geometryFactory);
    }

    /**
     * 将GeoJson对象转为Geometry
     *
     * @param geojsonGeometry geojsonGeometry
     * @param geometryFactory jts GeometryFactory
     * @return jts Geometry
     */
    public static Geometry geoJson2Geometry(GeoJsonObject.Geometry geojsonGeometry, GeometryFactory geometryFactory) {
        if (null == geojsonGeometry) {
            return null;
        }
        if (geojsonGeometry instanceof GeoJsonObject.Point) {
            return coords2Point(((GeoJsonObject.Point) geojsonGeometry).getCoordinates(), geometryFactory);
        } else if (geojsonGeometry instanceof GeoJsonObject.LineString) {
            return coords2LineString(((GeoJsonObject.LineString) geojsonGeometry).getCoordinates(), geometryFactory);
        } else if (geojsonGeometry instanceof GeoJsonObject.Polygon) {
            return coords2Polygon(((GeoJsonObject.Polygon) geojsonGeometry).getCoordinates(), geometryFactory);
        } else if (geojsonGeometry instanceof GeoJsonObject.MultiPoint) {
            return coords2MultiPoint(((GeoJsonObject.MultiPoint) geojsonGeometry).getCoordinates(), geometryFactory);
        } else if (geojsonGeometry instanceof GeoJsonObject.MultiLineString) {
            return coords2MultiLineString(((GeoJsonObject.MultiLineString) geojsonGeometry).getCoordinates(), geometryFactory);
        } else if (geojsonGeometry instanceof GeoJsonObject.MultiPolygon) {
            return coords2MultiPolygon(((GeoJsonObject.MultiPolygon) geojsonGeometry).getCoordinates(), geometryFactory);
        } else if (geojsonGeometry instanceof GeoJsonObject.GeometryCollection) {
            return toGeometryCollection((GeoJsonObject.GeometryCollection) geojsonGeometry, geometryFactory);
        } else {
            throw new RuntimeException("未知类型 " + geojsonGeometry.getType());
        }
    }


    /**
     * 将GeoJson对象转为Feature
     *
     * @param strGeoJsonFeature geoJsonFeature string
     * @param geometryFactory   jts GeometryFactory
     * @return Feature
     */
    public static Feature fromGeoJsonFeature(String strGeoJsonFeature, GeometryFactory geometryFactory) {
        GeoJsonObject.Feature geoJsonFeature;
        try {
            geoJsonFeature = mapper.readValue(strGeoJsonFeature, typeFeature);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return fromGeoJsonFeature(geoJsonFeature, geometryFactory);
    }

    /**
     * 将GeoJson对象转为Feature
     *
     * @param geoJsonFeature  geoJsonFeature
     * @param geometryFactory jts GeometryFactory
     * @return Feature
     */
    public static Feature fromGeoJsonFeature(GeoJsonObject.Feature geoJsonFeature, GeometryFactory geometryFactory) {
        Geometry geometry = geoJson2Geometry(geoJsonFeature.getGeometry(), geometryFactory);
        Feature feature = new Feature(geometry, geoJsonFeature.getProperties());
        return feature;
    }

    /**
     * 将GeoJson对象转为Feature
     *
     * @param strGeoJsonFeatureCollection geoJsonFeatureCollection string
     * @param geometryFactory             geometryFactory
     * @return FeatureCollection
     */
    public static FeatureCollection fromGeoJsonFeatureCollection(String strGeoJsonFeatureCollection, GeometryFactory geometryFactory) {
        GeoJsonObject.FeatureCollection geoJsonFeatureCollection;
        try {
            geoJsonFeatureCollection = mapper.readValue(strGeoJsonFeatureCollection, typeFeatureCollection);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return fromGeoJsonFeatureCollection(geoJsonFeatureCollection, geometryFactory);
    }

    /**
     * 将GeoJson对象转为Feature
     *
     * @param geoJsonFeatureCollection geoJsonFeatureCollection
     * @param geometryFactory          geometryFactory
     * @return FeatureCollection
     */
    public static FeatureCollection fromGeoJsonFeatureCollection(GeoJsonObject.FeatureCollection geoJsonFeatureCollection, GeometryFactory geometryFactory) {
        List<Feature> features = new ArrayList<>(geoJsonFeatureCollection.getFeatures().length);
        for (int i = 0; i < geoJsonFeatureCollection.getFeatures().length; i++) {
            Feature feature = fromGeoJsonFeature(geoJsonFeatureCollection.getFeatures()[i], geometryFactory);
            features.add(feature);
        }
        FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.setFeatures(features);
        return featureCollection;
    }

    private static Coordinate[] coords2Coordinates(double[][] coords) {
        Coordinate[] coordinates = new Coordinate[coords.length];
        for (int i = 0; i < coordinates.length; i++) {
            double[] xy = coords[i];
            coordinates[i] = new Coordinate(xy[0], xy[1]);
        }
        return coordinates;
    }

    private static LinearRing coords2Ring(double[][] coords, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = coords2Coordinates(coords);
        return geometryFactory.createLinearRing(coordinates);
    }

    private static org.locationtech.jts.geom.Point coords2Point(double[] coords, GeometryFactory geometryFactory) {
        return geometryFactory.createPoint(new Coordinate(coords[0], coords[1]));
    }

    private static org.locationtech.jts.geom.LineString coords2LineString(double[][] coords, GeometryFactory geometryFactory) {
        Coordinate[] coordinates = coords2Coordinates(coords);
        return geometryFactory.createLineString(coordinates);
    }

    private static org.locationtech.jts.geom.Polygon coords2Polygon(double[][][] coords, GeometryFactory geometryFactory) {
        LinearRing ring = coords2Ring(coords[0], geometryFactory);
        if (coords.length == 1) {
            return geometryFactory.createPolygon(ring);
        } else {
            LinearRing[] holes = new LinearRing[coords.length - 1];
            for (int i = 0; i < holes.length; i++) {
                holes[i] = coords2Ring(coords[i + 1], geometryFactory);
            }
            return geometryFactory.createPolygon(ring, holes);
        }
    }

    private static org.locationtech.jts.geom.MultiPoint coords2MultiPoint(double[][] coords, GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.Point[] points = new org.locationtech.jts.geom.Point[coords.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = coords2Point(coords[i], geometryFactory);
        }
        return geometryFactory.createMultiPoint(points);
    }

    private static org.locationtech.jts.geom.MultiLineString coords2MultiLineString(double[][][] coords, GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.LineString[] subs = new org.locationtech.jts.geom.LineString[coords.length];
        for (int i = 0; i < subs.length; i++) {
            subs[i] = coords2LineString(coords[i], geometryFactory);
        }
        return geometryFactory.createMultiLineString(subs);
    }

    private static org.locationtech.jts.geom.MultiPolygon coords2MultiPolygon(double[][][][] coords, GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.Polygon[] subs = new org.locationtech.jts.geom.Polygon[coords.length];
        for (int i = 0; i < subs.length; i++) {
            subs[i] = coords2Polygon(coords[i], geometryFactory);
        }
        return geometryFactory.createMultiPolygon(subs);
    }

    private static org.locationtech.jts.geom.GeometryCollection toGeometryCollection(GeoJsonObject.GeometryCollection geometryCollection, GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.Geometry[] geos = new org.locationtech.jts.geom.Geometry[geometryCollection.getGeometries().length];
        for (int i = 0; i < geos.length; i++) {
            GeoJsonObject.Geometry sub = geometryCollection.getGeometries()[i];
            if (sub instanceof GeoJsonObject.Point) {
                geos[i] = coords2Point(((GeoJsonObject.Point) sub).getCoordinates(), geometryFactory);
            } else if (sub instanceof GeoJsonObject.LineString) {
                geos[i] = coords2LineString(((GeoJsonObject.LineString) sub).getCoordinates(), geometryFactory);
            } else if (sub instanceof GeoJsonObject.Polygon) {
                geos[i] = coords2Polygon(((GeoJsonObject.Polygon) sub).getCoordinates(), geometryFactory);
            } else if (sub instanceof GeoJsonObject.MultiPoint) {
                geos[i] = coords2MultiPoint(((GeoJsonObject.MultiPoint) sub).getCoordinates(), geometryFactory);
            } else if (sub instanceof GeoJsonObject.MultiLineString) {
                geos[i] = coords2MultiLineString(((GeoJsonObject.MultiLineString) sub).getCoordinates(), geometryFactory);
            } else if (sub instanceof GeoJsonObject.MultiPolygon) {
                geos[i] = coords2MultiPolygon(((GeoJsonObject.MultiPolygon) sub).getCoordinates(), geometryFactory);
            } else if (sub instanceof GeoJsonObject.GeometryCollection) {
                geos[i] = toGeometryCollection((GeoJsonObject.GeometryCollection) sub, geometryFactory);
            } else {
                throw new RuntimeException("未知类型 " + sub.getType());
            }
        }
        return geometryFactory.createGeometryCollection(geos);
    }


}
