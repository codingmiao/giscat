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

import org.locationtech.jts.geom.Geometry;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * 判断输入的wkt geometry是否与要素的geometry相交
 * 注意，参数不支持表达式嵌套
 * Syntax
 * ["geoIntersects", wkt_string or geometry]: boolean
 * 示例
 * ["geoIntersects", "LINESTRING(100 20,120 30)"]
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("geoIntersects")
public class GeoIntersects extends Expression<Boolean> {

    protected GeoIntersects(ArrayList expressionArray) {
        super(expressionArray);
    }


    @Override
    public Boolean getValue(Feature feature, ExpressionParams expressionParams) {
        Geometry inputGeometry;
        {
            Object cache = expressionParams.getCache(this);
            if (ExpressionParams.empty == cache) {
                inputGeometry = null;
            } else if (null == cache) {
                Object value = expressionArray.get(1);
                inputGeometry = Read.readGeometry(value, expressionParams);
                expressionParams.putCache(this, inputGeometry);
            } else {
                inputGeometry = (Geometry) cache;
            }
        }

        Geometry featureGeometry = feature.getGeometry();
        if (null == featureGeometry) {
            return false;
        }
        if (null == inputGeometry) {
            return false;
        }
        return inputGeometry.intersects(featureGeometry);
    }

}
