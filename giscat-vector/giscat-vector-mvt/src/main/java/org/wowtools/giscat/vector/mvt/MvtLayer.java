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
package org.wowtools.giscat.vector.mvt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.util.analyse.Bbox;
import org.wowtools.giscat.vector.util.analyse.TileClip;

import java.util.*;

/**
 * mvt layer。MvtLayer中的各个方法都非线程安全，但对于多个MvtLayer，对象可以在单独的线程中使用，各个MvtLayer不会彼此影响，即使它们隶属于同一个MvtBuilder。
 *
 * @author liuyu
 * @date 2022/4/24
 */
public class MvtLayer {

    private static final class Ctx {
        int x = 0;
        int y = 0;
    }

    private final Collection<MvtFeature> features = new LinkedList<>();

    private final Map<String, Integer> keys = new LinkedHashMap<>();
    private final Map<Object, Integer> values = new LinkedHashMap<>();

    private final Ctx ctx = new Ctx();


    private final double wgs84SimplifyDistance;

    private final MvtCoordinateConvertor mvtCoordinateConvertor;

    private final TileClip tileClip;

    /**
     * @param mvtBuilder       mvtBuilder
     * @param simplifyDistance 对geometry进行简化的长度,单位是瓦片像素，取值范围[0,extent+clipBuffer]，为0时表示不做简化
     */
    public MvtLayer(@NotNull MvtBuilder mvtBuilder, int simplifyDistance) {
        mvtCoordinateConvertor = mvtBuilder.mvtCoordinateConvertor;
        tileClip = mvtBuilder.tileClip;

        if (simplifyDistance > 0) {
            Bbox bbox = mvtBuilder.getBbox();
            double d = Math.sqrt(Math.pow(bbox.xmax - bbox.xmin, 2) + Math.pow(bbox.ymax - bbox.ymin, 2)) / mvtBuilder.extent;
            wgs84SimplifyDistance = simplifyDistance * d;
        } else {
            wgs84SimplifyDistance = 0;
        }
    }

    /**
     * 向图层中添加要素
     *
     * @param feature feature
     */
    public void addFeature(@NotNull Feature feature) {
        Geometry clipedGeometry = clipGeometry(feature.getGeometry());
        addCipedGeometryAndAttributes(feature.getProperties(), clipedGeometry);
    }

    /**
     * 向图层中添加裁剪过的要素，如果要素已被裁剪好，即要素的geometry完全包含于瓦片内，使用此方法有更好的性能
     *
     * @param feature feature
     */
    public void addClipedFeature(@NotNull Feature feature) {
        addCipedGeometryAndAttributes(feature.getProperties(), feature.getGeometry());
    }

    /**
     * 向图层中添加要素
     *
     * @param features features
     */
    public void addFeatures(@NotNull Iterable<Feature> features) {
        for (Feature feature : features) {
            addFeature(feature);
        }
    }

    /**
     * 向图层中添加裁剪过的要素，如果要素已被裁剪好，即要素的geometry完全包含于瓦片内，使用此方法有更好的性能
     *
     * @param features features
     */
    public void addClipedFeatures(@NotNull Iterable<Feature> features) {
        for (Feature feature : features) {
            addClipedFeature(feature);
        }
    }

    /**
     * 扩展点 供子类覆写以确定是否向图层添加要素
     *
     * @param feature
     */
    protected void addMvtFeature(MvtFeature feature) {
        features.add(feature);
    }

    /**
     * 扩展点 供子类覆写以获取图层中的要素
     *
     * @return 图层中的要素
     */
    protected Collection<MvtFeature> getFeatures() {
        return features;
    }

    private void addCipedGeometryAndAttributes(Map<String, ?> attributes, @Nullable Geometry clipedGeometry) {
        if (null == clipedGeometry || clipedGeometry.isEmpty()) {
            return;//裁剪完没有交集则直接return
        }
        if (wgs84SimplifyDistance > 0) {
            clipedGeometry = TopologyPreservingSimplifier.simplify(clipedGeometry, wgs84SimplifyDistance);
        }
        // 转换并添加feature
        ArrayList<Integer> tags = tags(attributes);
        List<Geometry> resolveGeometries = new LinkedList<>();
        resolveGeometryCollection(clipedGeometry, resolveGeometries);
        for (Geometry resolveGeometry : resolveGeometries) {
            addSampleGeometryFeature(tags, resolveGeometry);
        }
    }

    //拆出GeometryCollection中的geometry塞到list中
    private void resolveGeometryCollection(@NotNull Geometry geometry, @NotNull List<Geometry> resolveGeometries) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subGeometry = geometry.getGeometryN(i);
            if (subGeometry.getClass().equals(GeometryCollection.class)) {
                resolveGeometryCollection(subGeometry, resolveGeometries);
            } else {
                resolveGeometries.add(subGeometry);
            }
        }
    }


    private void addSampleGeometryFeature(ArrayList<Integer> tags, Geometry geometry) {
        VectorTile.Tile.GeomType geomType = toGeomType(geometry);
        ctx.x = 0;
        ctx.y = 0;
        List<Integer> commands = commands(geometry);

        MvtFeature feature = new MvtFeature();
        feature.geomType = geomType;
        feature.commands = commands;

        feature.tags = tags;

        addMvtFeature(feature);
    }


    //将attributes转为tags以便加入到feature
    private ArrayList<Integer> tags(@Nullable Map<String, ?> attributes) {
        if (null == attributes) {
            return null;
        }
        ArrayList<Integer> tags = new ArrayList<>(attributes.size() * 2);
        for (Map.Entry<String, ?> e : attributes.entrySet()) {
            // skip attribute without value
            if (e.getValue() == null) {
                continue;
            }
            tags.add(key(e.getKey()));
            tags.add(value(e.getValue()));
        }
        return tags;
    }

    private @NotNull Integer key(String key) {
        return keys.computeIfAbsent(key, k -> keys.size());
    }

    protected @NotNull Set<String> keys() {
        return keys.keySet();
    }

    private @NotNull Integer value(Object value) {
        return values.computeIfAbsent(value, k -> values.size());
    }

    protected Set<Object> values() {
        return values.keySet();
    }


    private Geometry clipGeometry(Geometry geometry) {
        try {
            return tileClip.intersection(geometry);
        } catch (TopologyException e) {
            geometry = geometry.buffer(0);
            return tileClip.intersection(geometry);
        }
    }


    private static VectorTile.Tile.GeomType toGeomType(Geometry geometry) {
        if (geometry instanceof Point) {
            return VectorTile.Tile.GeomType.POINT;
        }
        if (geometry instanceof MultiPoint) {
            return VectorTile.Tile.GeomType.POINT;
        }
        if (geometry instanceof LineString) {
            return VectorTile.Tile.GeomType.LINESTRING;
        }
        if (geometry instanceof MultiLineString) {
            return VectorTile.Tile.GeomType.LINESTRING;
        }
        if (geometry instanceof Polygon) {
            return VectorTile.Tile.GeomType.POLYGON;
        }
        if (geometry instanceof MultiPolygon) {
            return VectorTile.Tile.GeomType.POLYGON;
        }
        return VectorTile.Tile.GeomType.UNKNOWN;
    }

    List<Integer> commands(Geometry geometry) {

        if (geometry instanceof MultiLineString) {
            return commands((MultiLineString) geometry);
        }
        if (geometry instanceof Polygon) {
            return commands((Polygon) geometry);
        }
        if (geometry instanceof MultiPolygon) {
            return commands((MultiPolygon) geometry);
        }

        return commands(geometry.getCoordinates(), shouldClosePath(geometry), geometry instanceof MultiPoint);
    }

    private List<Integer> commands(MultiLineString mls) {
        List<Integer> commands = new ArrayList<>();
        for (int i = 0; i < mls.getNumGeometries(); i++) {
            commands.addAll(commands(mls.getGeometryN(i).getCoordinates(), false));
        }
        return commands;
    }

    private List<Integer> commands(MultiPolygon mp) {
        List<Integer> commands = new ArrayList<>();
        for (int i = 0; i < mp.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) mp.getGeometryN(i);
            commands.addAll(commands(polygon));
        }
        return commands;
    }

    private List<Integer> commands(Polygon polygon) {

        // According to the vector tile specification, the exterior ring of a polygon
        // must be in clockwise order, while the interior ring in counter-clockwise order.
        // In the tile coordinate system, Y axis is positive down.
        //
        // However, in geographic coordinate system, Y axis is positive up.
        // Therefore, we must reverse the coordinates.
        // So, the code below will make sure that exterior ring is in counter-clockwise order
        // and interior ring in clockwise order.
        LineString exteriorRing = polygon.getExteriorRing();
        if (!Orientation.isCCW(exteriorRing.getCoordinates())) {
            exteriorRing = (LineString) exteriorRing.reverse();
        }
        List<Integer> commands = new ArrayList<>(commands(exteriorRing.getCoordinates(), true));

        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LineString interiorRing = polygon.getInteriorRingN(i);
            if (Orientation.isCCW(interiorRing.getCoordinates())) {
                interiorRing = (LineString) interiorRing.reverse();
            }
            commands.addAll(commands(interiorRing.getCoordinates(), true));
        }
        return commands;
    }

    /**
     * // // // Ex.: MoveTo(3, 6), LineTo(8, 12), LineTo(20, 34), ClosePath //
     * Encoded as: [ 9 3 6 18 5 6 12 22 15 ] // == command type 7 (ClosePath),
     * length 1 // ===== relative LineTo(+12, +22) == LineTo(20, 34) // ===
     * relative LineTo(+5, +6) == LineTo(8, 12) // == [00010 010] = command type
     * 2 (LineTo), length 2 // === relative MoveTo(+3, +6) // == [00001 001] =
     * command type 1 (MoveTo), length 1 // Commands are encoded as uint32
     * varints, vertex parameters are // encoded as sint32 varints (zigzag).
     * Vertex parameters are // also encoded as deltas to the previous position.
     * The original // position is (0,0)
     *
     * @param cs cs
     * @return list
     */
    private List<Integer> commands(Coordinate[] cs, boolean closePathAtEnd) {
        return commands(cs, closePathAtEnd, false);
    }

    private List<Integer> commands(Coordinate[] cs, boolean closePathAtEnd, boolean multiPoint) {
        if (cs.length == 0) {
            throw new IllegalArgumentException("empty geometry");
        }

        List<Integer> r = new ArrayList<>();

        int lineToIndex = 0;
        int lineToLength = 0;

        int x = ctx.x;
        int y = ctx.y;

        for (int i = 0; i < cs.length; i++) {
            Coordinate c = cs[i];
            double cx = mvtCoordinateConvertor.wgs84X2mvt(c.x);
            double cy = mvtCoordinateConvertor.wgs84Y2mvt(c.y);
            if (i == 0) {
                r.add(commandAndLength(Command.MoveTo, multiPoint ? cs.length : 1));
            }

            int _x = (int) Math.round(cx);
            int _y = (int) Math.round(cy);

            // prevent point equal to the previous
            if (i > 0 && _x == x && _y == y) {
                lineToLength--;
                continue;
            }

            // prevent double closing
            if (closePathAtEnd && cs.length > 1 && i == (cs.length - 1) && cs[0].equals(c)) {
                lineToLength--;
                continue;
            }

            // delta, then zigzag
            r.add(zigZagEncode(_x - x));
            r.add(zigZagEncode(_y - y));

            x = _x;
            y = _y;

            if (i == 0 && cs.length > 1 && !multiPoint) {
                // can length be too long?
                lineToIndex = r.size();
                lineToLength = cs.length - 1;
                r.add(commandAndLength(Command.LineTo, lineToLength));
            }

        }

        // update LineTo length
        if (lineToIndex > 0) {
            if (lineToLength == 0) {
                // remove empty LineTo
                r.remove(lineToIndex);
            } else {
                // update LineTo with new length
                r.set(lineToIndex, commandAndLength(Command.LineTo, lineToLength));
            }
        }

        if (closePathAtEnd) {
            r.add(commandAndLength(Command.ClosePath, 1));
        }

        ctx.x = x;
        ctx.y = y;

        return r;
    }


    static int commandAndLength(int command, int repeat) {
        return repeat << 3 | command;
    }

    static int zigZagEncode(int n) {
        // https://developers.google.com/protocol-buffers/docs/encoding#types
        return (n << 1) ^ (n >> 31);
    }

    static boolean shouldClosePath(Geometry geometry) {
        return (geometry instanceof Polygon) || (geometry instanceof LinearRing);
    }


}
