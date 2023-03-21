package org.wowtools.giscat.vector.mbexpression;

import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 绑定参数 表达式支持使用以$开头的字符串进行参数绑定。
 * 例如表达式["concat", "$a","$b"]，对其传入绑定参数{"$a":"hello","$b":"world"}时，表达式被解析为["concat", "hello","world"]，表达式输出结果"helloworld"。
 * 换言之，所有$开头的字符串都会被认定为绑定参数，如果字符串的真实值确实以$开头，可通过绑定参数传入具体值来解决，
 * 例如，希望表达式["concat", "$a","sss"]输出结果为"$asss"，则可通过传入参数{"$a":"$a"}来解决。
 * 使用参数绑定可减少解析时的性能消耗。
 * <p>
 * 包含一个geometryFactory对象，用于geometry对象变换相关的表达式，可根据需要通过setGeometryFactory设置自定义的geometryFactory
 *
 * @author liuyu
 * @date 2022/7/28
 */
public class ExpressionParams {
    private static final GeometryFactory defaultGeometryFactory = new GeometryFactory();
    /**
     * 空对象 用以避免实际值为null时缓存重复查询的问题
     */
    public static final Object empty = new Object();

    /**
     * 参数
     */
    private final @Nullable Map<String, Object> params;

    //取值缓存，供表达式解析用，减少重复的解析
    private final Map<Expression, Object> cache = new HashMap<>();

    private GeometryFactory geometryFactory = defaultGeometryFactory;

    /**
     * @param params 绑定参数 例如 {"$a":"hello","$b":"world"}
     */
    public ExpressionParams(Map<String, Object> params) {
        this.params = params;
    }

    public ExpressionParams() {
        this.params = null;
    }

    public @Nullable Object getValue(String paramKey) {
        if (null == params) {
            return null;
        }
        return params.get(paramKey);
    }

    public void putCache(Expression key, @Nullable Object value) {
        if (null == value) {
            value = empty;
        }
        cache.put(key, value);
    }

    public Object getCache(Expression key) {
        return cache.get(key);
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }
}
