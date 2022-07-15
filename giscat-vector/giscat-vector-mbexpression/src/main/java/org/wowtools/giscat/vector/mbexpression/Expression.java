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
package org.wowtools.giscat.vector.mbexpression;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reflections.Reflections;
import org.wowtools.giscat.vector.pojo.Feature;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An expression defines a formula for computing the value of the property using the operators described below. The expression operators provided by Mapbox GL include:
 * <p>
 * Mathematical operators for performing arithmetic and other operations on numeric values
 * Logical operators for manipulating boolean values and making conditional decisions
 * String operators for manipulating strings
 * Data operators for providing access to the properties of source features
 *
 * @author liuyu
 * @date 2022/7/15
 */
public abstract class Expression<R> {
    private static final Map<String, Constructor<? extends Expression>> implConstructors;

    static {
        //扫描包下的所有实现类，放入implConstructors对象以便按json解析
        Reflections reflections = new Reflections("org.wowtools.giscat.vector.mbexpression");
        Set<Class<? extends Expression>> classList = reflections.getSubTypesOf(Expression.class);
        HashMap<String, Constructor<? extends Expression>> _implConstructors = new HashMap<>();
        for (Class<? extends Expression> aClass : classList) {
            try {
                Constructor<? extends Expression> implConstructor = aClass.getDeclaredConstructor(ArrayList.class);
                implConstructor.setAccessible(true);
                String name = implConstructor.newInstance(new ArrayList<>(0)).getExpressionName();
                _implConstructors.put(name, implConstructor);
            } catch (Exception e) {
                throw new RuntimeException("注册Expression驱动类失败:" + aClass.getClass(), e);
            }
        }
        implConstructors = Map.copyOf(_implConstructors);
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    protected final ArrayList expressionArray;

    protected Expression(ArrayList expressionArray) {
        this.expressionArray = expressionArray;
    }


    /**
     * 将数组形式的表达式解析为对象
     *
     * @param expressionArray 数组
     * @return Expression
     */
    public static Expression newInstance(ArrayList expressionArray) {
        //[expression_name, argument_0, argument_1, ...]
        String expressionName = (String) expressionArray.get(0);
        Constructor<? extends Expression> constructor = implConstructors.get(expressionName);
        if (null == constructor) {
            throw new RuntimeException("expressionName " + expressionName + " 尚未被注册");
        }
        for (int i = 1; i < expressionArray.size(); i++) {
            Object sub = expressionArray.get(i);
            if (sub instanceof ArrayList) {
                sub = newInstance((ArrayList) sub);
                expressionArray.set(i, sub);
            }
        }
        try {
            return constructor.newInstance(expressionArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字符串形式的表达式解析为对象
     *
     * @param expressionStr
     * @return
     */
    public static Expression newInstance(String expressionStr) {
        ArrayList expressionArray;
        try {
            expressionArray = jsonMapper.readValue(expressionStr, ArrayList.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析json异常 " + expressionStr, e);
        }
        return newInstance(expressionArray);
    }

    /**
     * 获取表达式名称
     *
     * @return
     */
    public abstract String getExpressionName();

    /**
     * 传入所需要素，返回对应值
     *
     * @param feature feature
     * @return 对应值
     */
    public abstract R getValue(Feature feature);

    /**
     * 获取此表达式的数组
     *
     * @return 表达式的数组
     */
    public ArrayList getExpressionArray() {
        return expressionArray;
    }
}
