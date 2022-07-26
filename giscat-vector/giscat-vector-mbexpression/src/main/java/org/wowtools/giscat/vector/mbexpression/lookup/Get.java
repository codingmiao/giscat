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

import org.wowtools.giscat.vector.mbexpression.Expression;

import java.util.ArrayList;
import java.util.Map;

/**
 * get
 * <p>
 * 参见 https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#get
 * <p>
 * Syntax
 * ["get", string]: value
 * ["get", string, object]: value
 *
 * @author liuyu
 * @date 2022/7/15
 */
public class Get extends Expression<Object> {
    protected Get(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public String getExpressionName() {
        return "get";
    }

    @Override
    public Object getValue(Map<String, Object> featureProperties) {
        String key = (String) expressionArray.get(1);
        Object value = featureProperties.get(key);
        if (null != value && expressionArray.size() == 3) {
            value = expressionArray.get(2);
        }
        return value;
    }
}
