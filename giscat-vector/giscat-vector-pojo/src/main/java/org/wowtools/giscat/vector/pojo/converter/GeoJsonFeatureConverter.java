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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

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
    public static GeoJsonObject.Geometry geometry2GeoJson(@Nullable Geometry geometry) {
        if (null == geometry || geometry.isEmpty()) {
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
    public static GeoJsonObject.@NotNull Feature toGeoJson(@NotNull Feature feature) {
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
    public static GeoJsonObject.@NotNull FeatureCollection toGeoJson(@NotNull FeatureCollection featureCollection) {
        GeoJsonObject.FeatureCollection geoJsonFeatureCollection = new GeoJsonObject.FeatureCollection();

        if (null != featureCollection.getHeaders()) {
            geoJsonFeatureCollection.setHeaders(featureCollection.getHeaders());
        }

        GeoJsonObject.Feature[] geoJsonFeatures = new GeoJsonObject.Feature[featureCollection.getFeatures().size()];
        int i = 0;
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
    public static @Nullable Geometry geoJson2Geometry(String strGeojsonGeometry, @NotNull GeometryFactory geometryFactory) {
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
    public static Geometry geoJson2Geometry(GeoJsonObject.@Nullable Geometry geojsonGeometry, @NotNull GeometryFactory geometryFactory) {
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
    public static @NotNull Feature fromGeoJsonFeature(String strGeoJsonFeature, @NotNull GeometryFactory geometryFactory) {
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
    public static @NotNull Feature fromGeoJsonFeature(GeoJsonObject.@NotNull Feature geoJsonFeature, @NotNull GeometryFactory geometryFactory) {
        Geometry geometry = geoJson2Geometry(geoJsonFeature.getGeometry(), geometryFactory);
        return new Feature(geometry, geoJsonFeature.getProperties());
    }

    /**
     * 将GeoJson对象转为Feature
     *
     * @param strGeoJsonFeatureCollection geoJsonFeatureCollection string
     * @param geometryFactory             geometryFactory
     * @return FeatureCollection
     */
    public static @NotNull FeatureCollection fromGeoJsonFeatureCollection(String strGeoJsonFeatureCollection, @NotNull GeometryFactory geometryFactory) {
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
    public static @NotNull FeatureCollection fromGeoJsonFeatureCollection(GeoJsonObject.@NotNull FeatureCollection geoJsonFeatureCollection, @NotNull GeometryFactory geometryFactory) {
        FeatureCollection featureCollection = new FeatureCollection();

        featureCollection.setHeaders(geoJsonFeatureCollection.getHeaders());

        List<Feature> features = new ArrayList<>(geoJsonFeatureCollection.getFeatures().length);
        for (int i = 0; i < geoJsonFeatureCollection.getFeatures().length; i++) {
            Feature feature = fromGeoJsonFeature(geoJsonFeatureCollection.getFeatures()[i], geometryFactory);
            features.add(feature);
        }
        featureCollection.setFeatures(features);

        return featureCollection;
    }

    private static Coordinate @NotNull [] coords2Coordinates(double[] @NotNull [] coords) {
        Coordinate[] coordinates = new Coordinate[coords.length];
        for (int i = 0; i < coordinates.length; i++) {
            double[] xy = coords[i];
            coordinates[i] = new Coordinate(xy[0], xy[1]);
        }
        return coordinates;
    }

    private static LinearRing coords2Ring(double[] @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
        Coordinate[] coordinates = coords2Coordinates(coords);
        return geometryFactory.createLinearRing(coordinates);
    }

    private static org.locationtech.jts.geom.Point coords2Point(double @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
        return geometryFactory.createPoint(new Coordinate(coords[0], coords[1]));
    }

    private static org.locationtech.jts.geom.LineString coords2LineString(double[] @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
        Coordinate[] coordinates = coords2Coordinates(coords);
        return geometryFactory.createLineString(coordinates);
    }

    private static org.locationtech.jts.geom.Polygon coords2Polygon(double[][] @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
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

    private static org.locationtech.jts.geom.MultiPoint coords2MultiPoint(double[] @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.Point[] points = new org.locationtech.jts.geom.Point[coords.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = coords2Point(coords[i], geometryFactory);
        }
        return geometryFactory.createMultiPoint(points);
    }

    private static org.locationtech.jts.geom.MultiLineString coords2MultiLineString(double[][] @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.LineString[] subs = new org.locationtech.jts.geom.LineString[coords.length];
        for (int i = 0; i < subs.length; i++) {
            subs[i] = coords2LineString(coords[i], geometryFactory);
        }
        return geometryFactory.createMultiLineString(subs);
    }

    private static org.locationtech.jts.geom.MultiPolygon coords2MultiPolygon(double[][][] @NotNull [] coords, @NotNull GeometryFactory geometryFactory) {
        org.locationtech.jts.geom.Polygon[] subs = new org.locationtech.jts.geom.Polygon[coords.length];
        for (int i = 0; i < subs.length; i++) {
            subs[i] = coords2Polygon(coords[i], geometryFactory);
        }
        return geometryFactory.createMultiPolygon(subs);
    }

    private static org.locationtech.jts.geom.GeometryCollection toGeometryCollection(GeoJsonObject.@NotNull GeometryCollection geometryCollection, @NotNull GeometryFactory geometryFactory) {
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
