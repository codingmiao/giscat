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
package org.wowtools.giscat.vector.mbexpression.decision;

import org.jetbrains.annotations.Nullable;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;
import java.util.Objects;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#match">...</a>
 * <p>
 * Syntax
 * ["match",
 * input: InputType (number or string),
 * label: InputType | [InputType, InputType, ...], output: OutputType,
 * label: InputType | [InputType, InputType, ...], output: OutputType,
 * ...,
 * fallback: OutputType
 * ]: OutputType
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("match")
public class Match extends Expression<Object> {
    protected Match(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @Nullable Object getValue(Feature feature, ExpressionParams expressionParams) {
        Object input = getRealValue(feature, expressionArray.get(1), expressionParams);
        for (int i = 2; i < expressionArray.size() - 1; i += 2) {
            Object o = expressionArray.get(i);
            o = getRealValue(feature, o, expressionParams);
            if (Objects.equals(input, o)) {
                return getRealValue(feature, expressionArray.get(i + 1), expressionParams);
            }
        }
        return getRealValue(feature, expressionArray.get(expressionArray.size() - 1), expressionParams);
    }

}
