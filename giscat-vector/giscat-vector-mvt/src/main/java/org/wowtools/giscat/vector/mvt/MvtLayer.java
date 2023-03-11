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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.wowtools.giscat.vector.util.analyse.Bbox;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.*;

/**
 * mvt layer
 *
 * @author liuyu
 * @date 2022/4/24
 */
public final class MvtLayer {

    protected final List<MvtFeature> features = new ArrayList<>();

    private final Map<String, Integer> keys = new LinkedHashMap<>();
    private final Map<Object, Integer> values = new LinkedHashMap<>();

    private final MvtBuilder mvtBuilder;

    private final double wgs84SimplifyDistance;


    /**
     * @param mvtBuilder       mvtBuilder
     * @param simplifyDistance 对geometry进行简化的长度,单位是瓦片像素，取值范围[0,extent+clipBuffer]，为0时表示不做简化
     */
    protected MvtLayer(@NotNull MvtBuilder mvtBuilder, int simplifyDistance) {
        this.mvtBuilder = mvtBuilder;

        if (simplifyDistance > 0) {
            Bbox bbox = mvtBuilder.getBbox();
            double d = Math.sqrt(Math.pow(bbox.xmax - bbox.xmin, 2) + Math.pow(bbox.ymax - bbox.ymin, 2)) / mvtBuilder.extent;
            wgs84SimplifyDistance = simplifyDistance * d;
        } else {
            wgs84SimplifyDistance = 0;
        }
    }

    public void addFeatures(@NotNull Iterable<Feature> features) {
        for (Feature feature : features) {
            addFeature(feature);
        }
    }

    public void addClipedFeatures(@NotNull Iterable<Feature> features) {
        for (Feature feature : features) {
            addClipedFeature(feature);
        }
    }

    public void addFeature(@NotNull Feature feature) {
        Geometry clipedGeometry = clipGeometry(feature.getGeometry());
        addCipedGeometryAndAttributes(feature.getProperties(), clipedGeometry);
    }

    public void addClipedFeature(@NotNull Feature feature) {
        addCipedGeometryAndAttributes(feature.getProperties(), feature.getGeometry());
    }

    public void addCipedGeometryAndAttributes(Map<String, ?> attributes, @Nullable Geometry clipedGeometry) {
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

        MvtFeature feature = new MvtFeature();
        feature.geometry = geometry;

        feature.tags = tags;

        features.add(feature);
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

    protected @NotNull List<String> keys() {
        return new ArrayList<>(keys.keySet());
    }

    private @NotNull Integer value(Object value) {
        return values.computeIfAbsent(value, k -> values.size());
    }

    protected List<Object> values() {
        return List.copyOf(values.keySet());
    }


    private Geometry clipGeometry(Geometry geometry) {
        try {
            return mvtBuilder.tileClip.intersection(geometry);
        } catch (TopologyException e) {
            geometry = geometry.buffer(0);
            return mvtBuilder.tileClip.intersection(geometry);
        }
    }

}
