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
import java.util.Objects;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#index-of">...</a>
 * <p>
 * Syntax
 * ["index-of",
 * keyword: InputType (boolean, string, or number),
 * input: InputType (array or string)
 * ]: number
 * <p>
 * ["index-of",
 * keyword: InputType (boolean, string, or number),
 * input: InputType (array or string),
 * index: number
 * ]: number
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("index-of")
public class IndexOf extends Expression<Object> {
    protected IndexOf(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @NotNull Object getValue(Feature feature, ExpressionParams expressionParams) {
        Object keyword = getRealValue(feature, expressionArray.get(1), expressionParams);
        Object input = getRealValue(feature, expressionArray.get(2), expressionParams);
        int index = 0;
        if (expressionArray.size() == 4) {
            index = (int) getRealValue(feature, expressionArray.get(3), expressionParams);
        }
        if (keyword instanceof String && input instanceof String) {
            String strKeyword = (String) keyword;
            String strInput = (String) input;
            return strInput.indexOf(strKeyword, index);
        } else {
            ArrayList inputArr = (ArrayList) input;
            for (int i = index; i < inputArr.size(); i++) {
                Object inputObj = inputArr.get(i);
                if (Objects.equals(keyword, inputObj)) {
                    return i;
                }
            }
            return -1;
        }
    }

}
