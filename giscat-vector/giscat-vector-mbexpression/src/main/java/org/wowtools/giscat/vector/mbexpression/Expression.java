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
 * 参见 https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions
 * 仅实现了与要素筛选相关的lookup decision string math类型，并增加了spatial类型用于计算空间关系
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
                String name = null;
                try {
                    name = aClass.getAnnotation(ExpressionName.class).value();
                } catch (Exception e) {
                    throw new RuntimeException("获取ExpressionName失败，请检查是否有@ExpressionName注解 " + aClass, e);
                }
                if (null != _implConstructors.put(name, implConstructor)) {
                    throw new RuntimeException("重复的ExpressionName:" + name);
                }
            } catch (Exception e) {
                throw new RuntimeException("注册Expression驱动类失败:" + aClass, e);
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
                sub = parseSub((ArrayList) sub);
                if (null != sub) {
                    expressionArray.set(i, sub);
                }
            }
        }
        try {
            return constructor.newInstance(expressionArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseSub(ArrayList expressionArray) {
        //[expression_name, argument_0, argument_1, ...] or [obj1, obj2, ...]
        Object o0 = expressionArray.get(0);
        if (!(o0 instanceof String)) {
            return null;
        }
        String expressionName = (String) o0;
        Constructor<? extends Expression> constructor = implConstructors.get(expressionName);
        if (null == constructor) {
            return null;
        }
        return newInstance(expressionArray);
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
     * 传入所需要素,经表达式计算后返回对应值
     *
     * @param feature feature
     * @return 对应值
     * @see Feature
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


    protected static Object getRealValue(Feature feature, Object o) {
        if (o instanceof Expression) {
            Expression expression = (Expression) o;
            o = expression.getValue(feature);
            if (o instanceof Expression) {
                return getRealValue(feature, o);
            }
        }
        return o;
    }

}
