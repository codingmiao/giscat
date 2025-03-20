package org.wowtools.giscat.vector.mbexpression.types;

import org.jetbrains.annotations.Nullable;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/style-spec/reference/expressions/#types-to-string">...</a>
 * <p>
 * Syntax
 * ["to-string", value]: number
 *
 * @author liuyu
 * @date 2025/3/20
 */
@ExpressionName("to-string")
public class ToString extends Expression<String> {
    protected ToString(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @Nullable String getValue(Feature feature, ExpressionParams expressionParams) {
        Object o = expressionArray.get(1);
        o = getRealValue(feature, o, expressionParams);
        if (null == o) {
            return null;
        }
        return o.toString();
    }
}
