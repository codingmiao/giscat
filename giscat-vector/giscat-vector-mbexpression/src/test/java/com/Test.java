package com;

import org.wowtools.giscat.vector.mbexpression.Expression;

import java.util.Map;

/**
 * @author liuyu
 * @date 2022/7/15
 */
public class Test {

    public static void main(String[] args) throws Exception {

        String expressionStr = "[\"==\",[\"get\", \"name1\"],[\"get\", \"name2\"]]";
        Expression expression = Expression.newInstance(expressionStr);

        System.out.println(expression.getValue(Map.of(
                "name1","1",
                "name2","1"
        )));
        System.out.println(expression.getValue(Map.of(
                "name1","1",
                "name2",1
        )));
    }
}
