package org.wowtools.giscat.vector.mbexpression.decision;

import org.jetbrains.annotations.NotNull;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * 比较大小工具
 *
 * @author liuyu
 * @date 2022/7/27
 */
class Compare {

    /**
     * 不成立
     */
    public static final int impossible = -2;

    /**
     * 比较大小
     *
     * @param expressionArray  expressionArray
     * @param feature          feature
     * @param expressionParams expressionParams
     * @return 1 大于 0 等于 -1小于 -2 因为空值等原因不成立
     */
    public static int compare(@NotNull ArrayList expressionArray, Feature feature, ExpressionParams expressionParams) {
        Object o1 = expressionArray.get(1);
        if (o1 instanceof Expression) {
            Expression expression = (Expression) o1;
            o1 = expression.getValue(feature, expressionParams);
        }
        Object o2 = expressionArray.get(2);
        if (o2 instanceof Expression) {
            Expression expression = (Expression) o2;
            o2 = expression.getValue(feature, expressionParams);
        }
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null ^ o2 == null) {
            return impossible;
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
