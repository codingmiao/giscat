package org.wowtools.giscat.vector.mbexpression.types;

import org.jetbrains.annotations.Nullable;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#types-to-number">...</a>
 * 但是java是区分了int和double，所以这里也区分出了对应的表达式
 * <p>
 * Syntax
 * ["to-int", value, fallback: value, fallback: value, ...]: number
 *
 * @author liuyu
 * @date 2025/3/20
 */
@ExpressionName("to-int")
public class ToInt extends Expression<Integer> {
    protected ToInt(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @Nullable Integer getValue(Feature feature, ExpressionParams expressionParams) {
        for (int i = 1; i < expressionArray.size(); i++) {
            Object o = expressionArray.get(i);
            if (null == o) {
                return 0;
            }
            o = getRealValue(feature, o, expressionParams);
            try {
                if (o instanceof String) {
                    return Integer.parseInt((String) o);
                } else if (o instanceof Boolean) {
                    Boolean b = (Boolean) o;
                    return Boolean.TRUE.equals(b) ? 1 : 0;
                } else {
                    return Integer.parseInt(o.toString());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new RuntimeException("所有输入都无法转换为Integer");
    }
}
