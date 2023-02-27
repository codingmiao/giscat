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
package org.wowtools.giscat.vector.pojo;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.wowtools.giscat.vector.pojo.converter.GeoJsonFeatureConverter;

import java.util.Map;

/**
 * geojson对象
 *
 * @author liuyu
 * @date 2022/4/12
 */
public class GeoJsonObject {


    private static double[][] jtsCoordinates2Coords(Coordinate[] coordinates) {
        double[][] coords = new double[coordinates.length][];
        for (int i = 0; i < coords.length; i++) {
            Coordinate coordinate = coordinates[i];
            coords[i] = new double[]{coordinate.x, coordinate.y};
        }
        return coords;
    }

    private static double[][][] jtsPolygon2Coords(org.locationtech.jts.geom.Polygon jtsPolygon) {
        int numInteriorRing = jtsPolygon.getNumInteriorRing();
        double[][][] coordinates;
        if (numInteriorRing == 0) {
            coordinates = new double[][][]{
                    jtsCoordinates2Coords(jtsPolygon.getCoordinates())
            };
        } else {
            coordinates = new double[numInteriorRing + 1][][];
            coordinates[0] = jtsCoordinates2Coords(jtsPolygon.getExteriorRing().getCoordinates());
            for (int i = 1; i < coordinates.length; i++) {
                coordinates[i] = jtsCoordinates2Coords(jtsPolygon.getInteriorRingN(i - 1).getCoordinates());
            }
        }
        return coordinates;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = Point.class, name = "Point"),
            @JsonSubTypes.Type(value = LineString.class, name = "LineString"),
            @JsonSubTypes.Type(value = Polygon.class, name = "Polygon"),
            @JsonSubTypes.Type(value = MultiPoint.class, name = "MultiPoint"),
            @JsonSubTypes.Type(value = MultiLineString.class, name = "MultiLineString"),
            @JsonSubTypes.Type(value = MultiPolygon.class, name = "MultiPolygon"),
            @JsonSubTypes.Type(value = GeometryCollection.class, name = "GeometryCollection"),
    })
    public interface Geometry {
        @JsonIgnore
        String getType();

        @JsonIgnore
        default String toGeoJsonString() {
            String geoJson;
            try {
                geoJson = GeoJsonFeatureConverter.mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("转geojson字符串异常", e);
            }
            return geoJson;
        }
    }

    @Setter
    @Getter
    public static final class Point implements Geometry {
        private double[] coordinates;// [x y]

        @Override
        public String getType() {
            return "Point";
        }


        public Point() {
        }

        public Point(org.locationtech.jts.geom.Point jtsPoint) {
            this.coordinates = new double[]{jtsPoint.getX(), jtsPoint.getY()};
        }
    }


    @Setter
    @Getter
    public static final class LineString implements Geometry {
        private double[][] coordinates;//[coord][xy]

        @Override
        public String getType() {
            return "LineString";
        }


        public LineString() {
        }

        public LineString(org.locationtech.jts.geom.LineString jtsLineString) {
            coordinates = jtsCoordinates2Coords(jtsLineString.getCoordinates());
        }
    }

    @Setter
    @Getter
    public static final class Polygon implements Geometry {
        private double[][][] coordinates;//[ring][coord][xy]

        public Polygon() {
        }

        public Polygon(org.locationtech.jts.geom.Polygon jtsPolygon) {
            coordinates = jtsPolygon2Coords(jtsPolygon);
        }

        @Override
        public String getType() {
            return "Polygon";
        }
    }

    @Setter
    @Getter
    public static final class MultiPoint implements Geometry {
        private double[][] coordinates;//[coord][xy]

        public MultiPoint() {
        }

        public MultiPoint(org.locationtech.jts.geom.MultiPoint jtsMultiPoint) {
            coordinates = jtsCoordinates2Coords(jtsMultiPoint.getCoordinates());
        }

        @Override
        public String getType() {
            return "MultiPoint";
        }

    }

    @Setter
    @Getter
    public static final class MultiLineString implements Geometry {
        private double[][][] coordinates;//[subLine][coord][xy]

        public MultiLineString() {
        }

        public MultiLineString(org.locationtech.jts.geom.MultiLineString jtsMultiLineString) {
            coordinates = new double[jtsMultiLineString.getNumGeometries()][][];
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i] = jtsCoordinates2Coords(jtsMultiLineString.getGeometryN(i).getCoordinates());
            }
        }

        @Override
        public String getType() {
            return "MultiLineString";
        }

    }

    @Setter
    @Getter
    public static final class MultiPolygon implements Geometry {
        private double[][][][] coordinates;//[subPolygon][ring][coord][xy]

        public MultiPolygon() {
        }

        public MultiPolygon(org.locationtech.jts.geom.MultiPolygon jtsMultiPolygon) {
            coordinates = new double[jtsMultiPolygon.getNumGeometries()][][][];
            for (int i = 0; i < coordinates.length; i++) {
                coordinates[i] = jtsPolygon2Coords((org.locationtech.jts.geom.Polygon) jtsMultiPolygon.getGeometryN(i));
            }
        }

        @Override
        public String getType() {
            return "MultiPolygon";
        }

    }

    @Setter
    @Getter
    public static final class GeometryCollection implements Geometry {
        private Geometry[] geometries;

        public GeometryCollection() {
        }

        public GeometryCollection(org.locationtech.jts.geom.GeometryCollection jtsGeometryCollection) {
            geometries = new Geometry[jtsGeometryCollection.getNumGeometries()];
            for (int i = 0; i < geometries.length; i++) {
                org.locationtech.jts.geom.Geometry geometry = jtsGeometryCollection.getGeometryN(i);
                geometries[i] = GeoJsonFeatureConverter.geometry2GeoJson(geometry);
            }

        }

        @Override
        public String getType() {
            return "GeometryCollection";
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Setter
    @Getter
    public static final class Feature {
        private final String type = "Feature";
        private Geometry geometry;
        private Map<String, Object> properties;

        @JsonIgnore
        public String toGeoJsonString() {
            String geoJson;
            try {
                geoJson = GeoJsonFeatureConverter.mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("转geojson字符串异常", e);
            }
            return geoJson;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Setter
    @Getter
    public static final class FeatureCollection {
        private final String type = "FeatureCollection";
        private Feature[] features;
        private Map<String, Object> headers;

        @JsonIgnore
        public String toGeoJsonString() {
            String geoJson;
            try {
                geoJson = GeoJsonFeatureConverter.mapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("转geojson字符串异常", e);
            }
            return geoJson;
        }
    }

}
