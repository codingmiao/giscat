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
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#slice">...</a>
 * <p>
 * Syntax
 * ["slice",
 * input: InputType (array or string),
 * index: number
 * ]: OutputType (ItemType or string)
 * <p>
 * ["slice",
 * input: InputType (array or string),
 * index: number,
 * index: number
 * ]: OutputType (ItemType or string)
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("slice")
public class Slice extends Expression<Object> {
    protected Slice(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @NotNull Object getValue(Feature feature, ExpressionParams expressionParams) {
        Object input = getRealValue(feature, expressionArray.get(1), expressionParams);
        int start = (int) getRealValue(feature, expressionArray.get(2), expressionParams);
        int end;
        if (expressionArray.size() == 4) {
            end = (int) getRealValue(feature, expressionArray.get(3), expressionParams);
        } else {
            end = -1;
        }
        if (input instanceof String) {
            if (end > 0) {
                return ((String) input).substring(start, end);
            } else {
                return ((String) input).substring(start);
            }
        } else if (input instanceof ArrayList) {
            ArrayList inputArr = (ArrayList) input;
            if (end < 0) {
                end = inputArr.size();
            }
            ArrayList res = new ArrayList(end - start);
            for (int i = start; i < end; i++) {
                res.add(getRealValue(feature, inputArr.get(i), expressionParams));
            }
            return res;
        } else {
            throw new RuntimeException("未知类型 " + feature);
        }
    }
}
