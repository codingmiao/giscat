package org.wowtools.giscat.vector.mbexpression.lookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionName;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;
import java.util.Map;

/**
 * <p>
 * 参见 <a href="https://docs.mapbox.com/mapbox-gl-js/style-spec/expressions/#get">...</a>
 * NestingGet是get的扩充，用以解决featureProperties中嵌套属性的获取，例如["get", "a", "b"]表示取属性的a.b的值，若a为空则直接返回null
 * <p>
 * Syntax
 * ["get", string...]: value
 *
 * @author liuyu
 * @date 2025/2/7
 */
@ExpressionName("nesting-get")
public class NestingGet extends Expression<Object> {
    protected NestingGet(ArrayList expressionArray) {
        super(expressionArray);
    }

    @Override
    public @Nullable Object getValue(@NotNull Feature feature, ExpressionParams expressionParams) {
        Map<String, Object> featureProperties = feature.getProperties();
        if (null == featureProperties) {
            return null;
        }
        String key = (String) expressionArray.get(1);
        Object value = featureProperties.get(key);
        for (int i = 2; i < expressionArray.size(); i++) {
            if (null == value) {
                break;
            }
            key = (String) expressionArray.get(i);
            if (!(value instanceof Map)) {
                throw new RuntimeException("第" + i + "层的值已不是map");
            }
            value = ((Map<String, Object>) value).get(key);
        }
        value = getRealValue(feature, value, expressionParams);
        return value;
    }
}
