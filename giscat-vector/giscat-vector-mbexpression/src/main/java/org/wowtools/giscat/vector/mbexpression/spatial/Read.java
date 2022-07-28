package org.wowtools.giscat.vector.mbexpression.spatial;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.wowtools.giscat.util.analyse.Bbox;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;

import java.util.ArrayList;

/**
 * 从表达式中读取geometry或bbox
 *
 * @author liuyu
 * @date 2022/7/26
 */
class Read {
    private static final WKTReader wktReader = new WKTReader();


    public static Geometry readGeometry(Object value, ExpressionParams expressionParams) {
        if (null == value) {
            return null;
        }
        value = getValue(value, expressionParams);
        Geometry inputGeometry;
        if (value instanceof String) {
            String wkt = (String) value;
            try {
                inputGeometry = wktReader.read(wkt);
            } catch (ParseException e) {
                throw new RuntimeException("非法的wkt " + wkt, e);
            }
        } else if (value instanceof Geometry) {
            inputGeometry = (Geometry) value;
        } else {
            throw new RuntimeException("未知的value " + value);
        }
        return inputGeometry;
    }

    public static Bbox readBbox(Object value, ExpressionParams expressionParams) {
        if (null == value) {
            return null;
        }
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            return new Bbox(((Number) getValue(list.get(0), expressionParams)).doubleValue(),
                    ((Number) getValue(list.get(1), expressionParams)).doubleValue(),
                    ((Number) getValue(list.get(2), expressionParams)).doubleValue(),
                    ((Number) getValue(list.get(3), expressionParams)).doubleValue());
        }
        if (value instanceof Bbox) {
            return (Bbox) value;
        }
        throw new RuntimeException("未知的Bbox数据类型 " + value);
    }

    private static Object getValue(Object o, ExpressionParams expressionParams) {
        if (o instanceof String) {
            String s = (String) o;
            if (s.charAt(0) == '$') {
                return expressionParams.getValue(s);
            }
        }
        return o;
    }
}
