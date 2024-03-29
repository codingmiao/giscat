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
package org.wowtools.giscat.vector.mbexpression.spatial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.wowtools.giscat.vector.util.analyse.TileClip;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * 输入bbox，若geometry与要素相交则裁剪要素的geometry并返回裁剪后的要素，若不相交则返回null
 * Syntax
 * ["bboxIntersection", [xmin,ymin,xmax,ymax] or TileClip]: boolean
 * 示例
 * ["bboxIntersection", [90,20,92.5,21.3]]
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("bboxIntersection")
public class BboxIntersection extends Expression<Feature> {


    protected BboxIntersection(ArrayList expressionArray) {
        super(expressionArray);
    }


    @Override
    public @Nullable Feature getValue(@NotNull Feature feature, @NotNull ExpressionParams expressionParams) {
        TileClip tileClip;
        {
            Object cache = expressionParams.getCache(this);
            if (ExpressionParams.empty == cache) {
                tileClip = null;
            } else if (null == cache) {
                Object value = expressionArray.get(1);
                tileClip = Read.readTileClip(feature, value, expressionParams);
                expressionParams.putCache(this, tileClip);
            } else {
                tileClip = (TileClip) cache;
            }
        }

        Geometry featureGeometry = feature.getGeometry();
        if (null == featureGeometry) {
            return null;
        }
        if (null == tileClip) {
            return null;
        }
        featureGeometry = tileClip.intersection(featureGeometry);
        if (null == featureGeometry || featureGeometry.isEmpty()) {
            return null;
        }
        Feature newFeature = new Feature(featureGeometry, feature.getProperties());
        return newFeature;
    }

}
