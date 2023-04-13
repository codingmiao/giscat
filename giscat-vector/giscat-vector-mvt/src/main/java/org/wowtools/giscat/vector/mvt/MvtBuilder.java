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

import org.locationtech.jts.geom.GeometryFactory;
import org.wowtools.giscat.vector.util.analyse.Bbox;
import org.wowtools.giscat.vector.util.analyse.TileClip;
import org.wowtools.giscat.vector.util.cst.Tile2Wgs84;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * mvt瓦片构造器
 *
 * @author liuyu
 * @date 2022/4/24
 */
public class MvtBuilder {
    protected final int extent;

    protected final TileClip tileClip;

    protected final MvtCoordinateConvertor mvtCoordinateConvertor;

    private final Map<String, MvtLayer> layers = new LinkedHashMap<>();

    private final Bbox bbox;

    public MvtBuilder(byte z, int x, int y, GeometryFactory geometryFactory) {
        this(z, x, y, 4096, 8, geometryFactory);
    }


    /**
     * Create with the given extent value.
     * <p>
     * The extent value control how detailed the coordinates are encoded in the
     * vector tile. 4096 is a good default, 256 can be used to reduce density.
     * <p>
     * The clip buffer value control how large the clipping area is outside of the
     * tile for geometries. 0 means that the clipping is done at the tile border. 8
     * is a good default.
     *
     * @param extent     a int with extent value. 4096 is a good value.
     * @param clipBuffer a int with clip buffer size for geometries. 8 is a good value.
     */
    public MvtBuilder(byte z, int x, int y, int extent, int clipBuffer, GeometryFactory geometryFactory) {
        this.extent = extent;
        bbox = createTileBbox(z, x, y, extent, clipBuffer);
        tileClip = new TileClip(bbox.xmin, bbox.ymin, bbox.xmax, bbox.ymax, geometryFactory);
        mvtCoordinateConvertor = new MvtCoordinateConvertor(z, x, y);


    }

    /**
     * 新建一个图层，按simplifyDistance的值简化geometry。此方法非线程安全，但得到的MvtLayer对象可以在单独的线程中使用，各个MvtLayer不会彼此影响。
     *
     * @param layerName        图层名 本方法没有对图层名进行唯一校验，故若图层已存在，则原图层会被覆盖
     * @param simplifyDistance 对geometry进行简化的长度,单位是瓦片像素，取值范围[0,extent+clipBuffer]，为0时表示不做简化
     * @return MvtLayer
     */
    public MvtLayer createLayer(String layerName, int simplifyDistance) {
        MvtLayer layer = new MvtLayer(this, simplifyDistance);
        layers.put(layerName, layer);
        return layer;
    }

    /**
     * 新建一个图层，不对geometry进行简化。此方法非线程安全，但得到的MvtLayer对象可以在单独的线程中使用，各个MvtLayer不会彼此影响。
     *
     * @param layerName 图层名 本方法没有对图层名进行唯一校验，故若图层已存在，则原图层会被覆盖
     * @return MvtLayer
     */
    public MvtLayer createLayer(String layerName) {
        return createLayer(layerName, 0);
    }

    /**
     * 新建或获取一个图层，按simplifyDistance的值简化geometry。此方法非线程安全，但得到的MvtLayer对象可以在单独的线程中使用，各个MvtLayer不会彼此影响。
     *
     * @param layerName        图层名
     * @param simplifyDistance 对geometry进行简化的长度,单位是瓦片像素，取值范围[0,extent+clipBuffer]，为0时表示不做简化
     * @return 若已有同名图层则返回现有图层，否则新建一个
     */
    public MvtLayer getOrCreateLayer(String layerName, int simplifyDistance) {
        MvtLayer layer = layers.get(layerName);
        if (layer != null) {
            return layer;
        }
        return createLayer(layerName, simplifyDistance);
    }

    /**
     * 新建或获取一个图层，不对geometry进行简化。此方法非线程安全，但得到的MvtLayer对象可以在单独的线程中使用，各个MvtLayer不会彼此影响。
     *
     * @param layerName 图层名
     * @return 若已有同名图层则返回现有图层，否则新建一个
     */
    public MvtLayer getOrCreateLayer(String layerName) {
        MvtLayer layer = layers.get(layerName);
        if (layer != null) {
            return layer;
        }
        return createLayer(layerName, 0);
    }

    /**
     * 移除一个图层
     *
     * @param layerName 图层名
     */
    public void removeLayer(String layerName) {
        layers.remove(layerName);
    }

    /**
     * 添加一个自定义实现的图层
     *
     * @param layerName 图层名
     * @param layer     图层具体实例
     * @param <T>       MvtLayer的实现类，可通过扩展MvtLayer.addMvtFeature、MvtLayer.getFeatures方法来灵活地控制添加要素的方式
     */
    public <T extends MvtLayer> void addLayer(String layerName, T layer) {
        layers.put(layerName, layer);
    }


    private static Bbox createTileBbox(byte z, int tileX, int tileY, int extent, int clipBuffer) {
        //瓦片左上角坐标
        double x0 = Tile2Wgs84.tileX2lon(tileX, z);
        double y0 = Tile2Wgs84.tileY2lat(tileY, z);
        //瓦片右下角坐标
        double x1 = Tile2Wgs84.tileX2lon(tileX + 1, z);
        double y1 = Tile2Wgs84.tileY2lat(tileY + 1, z);
        //clipBuffer后的坐标
        double dx = (x1 - x0) / extent;
        double clipBufferX = dx * clipBuffer;
        x0 = x0 - clipBufferX;
        x1 = x1 + clipBufferX;

        double dy = (y0 - y1) / extent;
        double clipBufferY = dy * clipBuffer;
        y0 = y0 + clipBufferY;
        y1 = y1 - clipBufferY;

        return new Bbox(x0, y1, x1, y0);
    }


    public Bbox getBbox() {
        return bbox;
    }


    /**
     * 转为 bytes
     *
     * @return bytes
     */
    public byte[] toBytes() {

        VectorTile.Tile.Builder tile = VectorTile.Tile.newBuilder();
        layers.forEach((layerName, layer) -> {
            VectorTile.Tile.Layer.Builder tileLayer = VectorTile.Tile.Layer.newBuilder();

            tileLayer.setVersion(2);
            tileLayer.setName(layerName);

            tileLayer.addAllKeys(layer.keys());

            for (Object value : layer.values()) {
                VectorTile.Tile.Value.Builder tileValue = VectorTile.Tile.Value.newBuilder();
                if (value instanceof String) {
                    tileValue.setStringValue((String) value);
                } else if (value instanceof Integer) {
                    tileValue.setSintValue((Integer) value);
                } else if (value instanceof Long) {
                    tileValue.setSintValue((Long) value);
                } else if (value instanceof Float) {
                    tileValue.setFloatValue((Float) value);
                } else if (value instanceof Double) {
                    tileValue.setDoubleValue((Double) value);
                } else if (value instanceof Boolean) {
                    tileValue.setBoolValue((Boolean) value);
                } else {
                    tileValue.setStringValue(value.toString());
                }
                tileLayer.addValues(tileValue.build());
            }

            tileLayer.setExtent(extent);
            for (MvtFeature feature : layer.getFeatures()) {
                VectorTile.Tile.Feature.Builder featureBuilder = VectorTile.Tile.Feature.newBuilder();

                if (null != feature.tags) {
                    featureBuilder.addAllTags(feature.tags);
                }


                featureBuilder.setType(feature.geomType);
                featureBuilder.addAllGeometry(feature.commands);

                tileLayer.addFeatures(featureBuilder.build());
            }

            tile.addLayers(tileLayer.build());
        });


        return tile.build().toByteArray();
    }

}
