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
package org.wowtools.giscat.vector.mbexpression.lookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;
import java.util.Map;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#get">...</a>
 * <p>
 * Syntax
 * ["get", string]: value
 * ["get", string, object]: value
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("get")
public class Get extends Expression<Object> {
    protected Get(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @Nullable Object getValue(@NotNull Feature feature, ExpressionParams expressionParams) {
        String key = (String) expressionArray.get(1);
        Map<String, Object> featureProperties = feature.getProperties();
        if (null == featureProperties) {
            return null;
        }
        Object value = featureProperties.get(key);
        if (null != value && expressionArray.size() == 3) {
            value = expressionArray.get(2);
        }
        value = getRealValue(feature, value, expressionParams);
        return value;
    }
}
