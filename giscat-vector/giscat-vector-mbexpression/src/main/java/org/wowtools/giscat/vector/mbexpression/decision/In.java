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

import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;
import java.util.Objects;

/**
 * <p>
 * 参见 https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#in
 * 当keyword input都是string时，相当于strInput.indexOf(strKeyword)
 * 否则，相当于 keyword in(xxx,xxx)
 * <p>
 * Syntax
 * ["in",
 * keyword: InputType (boolean, string, or number),
 * input: InputType (array or string)
 * ]: boolean
 *
 * @author liuyu
 * @date 2022/7/15
 */
@ExpressionName("in")
public class In extends Expression<Boolean> {
    protected In(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public Boolean getValue(Feature feature) {
        Object keyword = getRealValue(feature, expressionArray.get(1));
        Object input = getRealValue(feature, expressionArray.get(2));
        if (keyword instanceof String && input instanceof String) {
            String strKeyword = (String) keyword;
            String strInput = (String) input;
            return strInput.indexOf(strKeyword) >= 0;
        } else {
            ArrayList inputArr = (ArrayList) input;
            for (Object inputObj : inputArr) {
                if (Objects.equals(keyword, inputObj)) {
                    return true;
                }
            }
            return false;
        }
    }

}
