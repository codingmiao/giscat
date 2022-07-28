package org.wowtools.giscat.vector.mbexpression.decision;

import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * 比较大小工具
 *
 * @author liuyu
 * @date 2022/7/27
 */
class Compare {


    public static int compare(ArrayList expressionArray, Feature feature) {
        Object o1 = expressionArray.get(1);
        if (o1 instanceof Expression) {
            Expression expression = (Expression) o1;
            o1 = expression.getValue(feature);
        }
        Object o2 = expressionArray.get(2);
        if (o2 instanceof Expression) {
            Expression expression = (Expression) o2;
            o2 = expression.getValue(feature);
        }
        if (o1 instanceof Number) {
            Number n1 = (Number) o1;
            Number n2 = (Number) o2;
            double d = n1.doubleValue() - n2.doubleValue();
            if (d > 0) {
                return 1;
            } else if (d < 0) {
                return -1;
            } else {
                return 0;
            }
        } else if (o1 instanceof String) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.compareTo(s2);
        } else {
            throw new RuntimeException("暂未实现的类型比较 " + o1.getClass());
        }
    }


}
