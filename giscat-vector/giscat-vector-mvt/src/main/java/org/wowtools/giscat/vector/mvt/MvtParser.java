/*****************************************************************
 *  Copyright (c) 2022- "giscat by 刘雨 (https://github.com/codingmiao/giscat)"
 *  This document is adapted from https://github.com/ElectronicChartCentre/java-vector-tile
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
package org.wowtools.giscat.vector.mvt;

import com.google.protobuf.InvalidProtocolBufferException;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.*;

/**
 * mvt bytes解析
 *
 * @author liuyu
 * @date 2022/5/10
 */
public class MvtParser {

    /**
     * 解析为瓦片坐标系
     *
     * @param data 矢量瓦片 bytes
     * @param gf   jts GeometryFactory
     * @return MvtFeatureLayer
     */
    public static MvtFeatureLayer[] parse2TileCoords(byte[] data, GeometryFactory gf) {
        return parse(null, data, gf);
    }

    /**
     * 解析瓦片并将坐标转为wgs84坐标系
     *
     * @param z    z
     * @param x    x
     * @param y    y
     * @param data 矢量瓦片 bytes
     * @param gf   jts GeometryFactory
     * @return MvtFeatureLayer
     */
    public static MvtFeatureLayer[] parse2Wgs84Coords(byte z, int x, int y, byte[] data, GeometryFactory gf) {
        MvtCoordinateConvertor mvtCoordinateConvertor = new MvtCoordinateConvertor(z, x, y);
        return parse(mvtCoordinateConvertor, data, gf);
    }

    private static MvtFeatureLayer[] parse(MvtCoordinateConvertor mvtCoordinateConvertor, byte[] data, GeometryFactory gf) {
        VectorTile.Tile tile;
        try {
            tile = VectorTile.Tile.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("解析 bytes 出错", e);
        }
        MvtFeatureLayer[] layers = new MvtFeatureLayer[tile.getLayersCount()];
        int i = 0;
        for (VectorTile.Tile.Layer pLayer : tile.getLayersList()) {
            layers[i] = new MvtFeatureLayer(pLayer, gf, mvtCoordinateConvertor);
            i++;
        }
        return layers;
    }


    /**
     * 图层
     */
    public static final class MvtFeatureLayer {
        private final String layerName;
        private final int extent;
        private final Feature[] features;

        private MvtFeatureLayer(VectorTile.Tile.Layer pLayer, GeometryFactory gf, final MvtCoordinateConvertor mvtCoordinateConvertor) {
            layerName = pLayer.getName();
            extent = pLayer.getExtent();

            String[] keys;
            Object[] values;
            {
                List<String> pKeys = pLayer.getKeysList();
                keys = new String[pKeys.size()];
                pKeys.toArray(keys);

                List<VectorTile.Tile.Value> pValueList = pLayer.getValuesList();
                values = new Object[pValueList.size()];
                int i = 0;
                for (VectorTile.Tile.Value value : pValueList) {
                    if (value.hasBoolValue()) {
                        values[i] = value.getBoolValue();
                    } else if (value.hasDoubleValue()) {
                        values[i] = value.getDoubleValue();
                    } else if (value.hasFloatValue()) {
                        values[i] = value.getFloatValue();
                    } else if (value.hasIntValue()) {
                        values[i] = value.getIntValue();
                    } else if (value.hasSintValue()) {
                        values[i] = value.getSintValue();
                    } else if (value.hasUintValue()) {
                        values[i] = value.getUintValue();
                    } else if (value.hasStringValue()) {
                        values[i] = value.getStringValue();
                    }
//                    else {
//                        values[i] = null;
//                    }
                    i++;
                }
            }


            int i = 0;
            List<VectorTile.Tile.Feature> pFeatureList = pLayer.getFeaturesList();
            features = new Feature[pFeatureList.size()];
            for (VectorTile.Tile.Feature pFeature : pFeatureList) {
                features[i] = parseFeature(pFeature, keys, values, gf, mvtCoordinateConvertor);
                i++;
            }
        }

        public String getLayerName() {
            return layerName;
        }

        public int getExtent() {
            return extent;
        }

        public Feature[] getFeatures() {
            return features;
        }
    }


    private static int zigZagDecode(int n) {
        return ((n >> 1) ^ (-(n & 1)));
    }

    private static Feature parseFeature(VectorTile.Tile.Feature pFeature, String[] keys, Object[] values, GeometryFactory gf, final MvtCoordinateConvertor mvtCoordinateConvertor) {

        int tagsCount = pFeature.getTagsCount();
        Map<String, Object> attributes = new HashMap<>(tagsCount / 2);
        int tagIdx = 0;
        while (tagIdx < pFeature.getTagsCount()) {
            String key = keys[pFeature.getTags(tagIdx++)];
            Object value = values[pFeature.getTags(tagIdx++)];
            attributes.put(key, value);
        }

        int x = 0;
        int y = 0;

        LinkedList<LinkedList<Coordinate>> coordsList = new LinkedList<>();
        LinkedList<Coordinate> coords = null;

        int geometryCount = pFeature.getGeometryCount();
        int length = 0;
        int command = 0;
        int i = 0;
        while (i < geometryCount) {

            if (length <= 0) {
                length = pFeature.getGeometry(i++);
                command = length & ((1 << 3) - 1);
                length = length >> 3;
            }

            if (length > 0) {

                if (command == Command.MoveTo) {
                    coords = new LinkedList<>();
                    coordsList.add(coords);
                }

                if (command == Command.ClosePath) {
                    if (pFeature.getType() != VectorTile.Tile.GeomType.POINT && null != coords && !coords.isEmpty()) {
                        coords.add(coords.getFirst());
                    }
                    length--;
                    continue;
                }

                int dx = pFeature.getGeometry(i++);
                int dy = pFeature.getGeometry(i++);

                length--;

                dx = zigZagDecode(dx);
                dy = zigZagDecode(dy);

                x = x + dx;
                y = y + dy;

                Coordinate coord;
                if (null != mvtCoordinateConvertor) {
                    double wgs84X = mvtCoordinateConvertor.mvtX2wgs84(x);
                    double wgs84Y = mvtCoordinateConvertor.mvtY2wgs84(y);
                    coord = new Coordinate(wgs84X, wgs84Y);
                } else {
                    coord = new Coordinate(x, y);
                }

                coords.add(coord);
            }

        }

        Geometry geometry = null;

        switch (pFeature.getType()) {
            case LINESTRING:
                List<LineString> lineStrings = new ArrayList<>();
                for (List<Coordinate> cs : coordsList) {
                    if (cs.size() <= 1) {
                        continue;
                    }
                    lineStrings.add(gf.createLineString(cs.toArray(new Coordinate[cs.size()])));
                }
                if (lineStrings.size() == 1) {
                    geometry = lineStrings.get(0);
                } else if (lineStrings.size() > 1) {
                    geometry = gf.createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
                }
                break;
            case POINT:
                List<Coordinate> allCoords = new ArrayList<>();
                for (List<Coordinate> cs : coordsList) {
                    allCoords.addAll(cs);
                }
                if (allCoords.size() == 1) {
                    geometry = gf.createPoint(allCoords.get(0));
                } else if (allCoords.size() > 1) {
                    geometry = gf.createMultiPointFromCoords(allCoords.toArray(new Coordinate[allCoords.size()]));
                }
                break;
            case POLYGON:
                List<List<LinearRing>> polygonRings = new ArrayList<>();
                List<LinearRing> ringsForCurrentPolygon = new ArrayList<>();
                for (List<Coordinate> cs : coordsList) {
                    // skip hole with too few coordinates
                    if (ringsForCurrentPolygon.size() > 0 && cs.size() < 4) {
                        continue;
                    }
                    if (cs.size() < 4) {
                        continue;
                    }
                    LinearRing ring = gf.createLinearRing(cs.toArray(new Coordinate[cs.size()]));
                    if (Orientation.isCCW(ring.getCoordinates())) {
                        ringsForCurrentPolygon = new ArrayList<>();
                        polygonRings.add(ringsForCurrentPolygon);
                    }
                    ringsForCurrentPolygon.add(ring);
                }
                if (polygonRings.size() == 0 && ringsForCurrentPolygon.size() > 0) {
                    // 有时候外环坐标没有严格按逆时针顺序存储，则取内环为外环
                    LinearRing shell = ringsForCurrentPolygon.get(0);
                    geometry = gf.createPolygon(shell);
                } else {
                    List<Polygon> polygons = new ArrayList<>();
                    for (List<LinearRing> rings : polygonRings) {
                        LinearRing shell = rings.get(0);
                        LinearRing[] holes = rings.subList(1, rings.size()).toArray(new LinearRing[rings.size() - 1]);
                        polygons.add(gf.createPolygon(shell, holes));
                    }
                    if (polygons.size() == 1) {
                        geometry = polygons.get(0);
                    }
                    if (polygons.size() > 1) {
                        geometry = gf.createMultiPolygon(GeometryFactory.toPolygonArray(polygons));
                    }
                }
                break;
//            case UNKNOWN:
//                break;
            default:
                break;
        }

        if (geometry == null) {
            geometry = gf.createGeometryCollection(new Geometry[0]);
        }

        return new Feature(geometry, Collections.unmodifiableMap(attributes));
    }


}
