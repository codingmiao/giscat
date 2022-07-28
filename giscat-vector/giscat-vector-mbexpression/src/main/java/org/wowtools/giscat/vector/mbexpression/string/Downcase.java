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
package org.wowtools.giscat.vector.mbexpression.string;

import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;
import java.util.Locale;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#downcase">...</a>
 * <p>
 * Syntax
 * ["downcase", string]: string
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("downcase")
public class Downcase extends Expression<String> {
    protected Downcase(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public String getValue(Feature feature, ExpressionParams expressionParams) {
        String s = (String) getRealValue(feature, expressionArray.get(1), expressionParams);
        if (s == null) {
            return null;
        }
        return s.toLowerCase(Locale.ROOT);
    }

}
