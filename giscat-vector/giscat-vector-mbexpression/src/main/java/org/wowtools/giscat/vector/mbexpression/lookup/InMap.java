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
import java.util.Set;

/**
 * <p>
 * 对in表达式的一个优化，in array时间复杂度是o(n)所以对于array中数据过多的情况性能很差，而in-map则在初始化时做了一个map变成了hash取值
 * 相当于 keyword in(xxx,xxx)
 * <p>
 * Syntax
 * ["in-map",
 * keyword: InputType (boolean, string, or number),
 * input: InputType array 不支持嵌套表达式
 * ]: boolean
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("in-map")
public class InMap extends Expression<Boolean> {
    private final Set<Object> inMap;

    protected InMap(ArrayList expressionArray) {
        super(expressionArray);
        ArrayList inputArr = (ArrayList) expressionArray.get(2);
        inMap = Set.copyOf(inputArr);
    }

    @Override
    public @NotNull Boolean getValue(Feature feature, ExpressionParams expressionParams) {
        Object keyword = getRealValue(feature, expressionArray.get(1), expressionParams);
        return inMap.contains(keyword);
    }

}
