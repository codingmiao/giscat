package org.wowtools.giscat.vector.mbexpression.spatial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.wowtools.giscat.vector.util.analyse.Bbox;
import org.wowtools.giscat.vector.util.analyse.TileClip;
import org.wowtools.giscat.vector.mbexpression.Expression;
import org.wowtools.giscat.vector.mbexpression.ExpressionParams;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.ArrayList;

/**
 * 从表达式中读取geometry或bbox
 *
 * @author liuyu
 * @date 2022/7/26
 */
class Read {
    private static final WKTReader wktReader = new WKTReader();


    public static Geometry readGeometry(Feature feature, @Nullable Object value, ExpressionParams expressionParams) {
        if (null == value) {
            return null;
        }
        value = Expression.getRealValue(feature, value, expressionParams);
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

    public static Bbox readBbox(Feature feature, @Nullable Object value, ExpressionParams expressionParams) {
        if (null == value) {
            return null;
        }
        value = Expression.getRealValue(feature, value, expressionParams);
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            return new Bbox(((Number) Expression.getRealValue(feature, list.get(0), expressionParams)).doubleValue(),
                    ((Number) Expression.getRealValue(feature, list.get(1), expressionParams)).doubleValue(),
                    ((Number) Expression.getRealValue(feature, list.get(2), expressionParams)).doubleValue(),
                    ((Number) Expression.getRealValue(feature, list.get(3), expressionParams)).doubleValue());
        }
        if (value instanceof Bbox) {
            return (Bbox) value;
        }
        throw new RuntimeException("未知的Bbox数据类型 " + value);
    }

    public static TileClip readTileClip(Feature feature, @Nullable Object value, @NotNull ExpressionParams expressionParams) {
        if (null == value) {
            return null;
        }
        value = Expression.getRealValue(feature, value, expressionParams);
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            return new TileClip(((Number) Expression.getRealValue(feature, list.get(0), expressionParams)).doubleValue(),
                    ((Number) Expression.getRealValue(feature, list.get(1), expressionParams)).doubleValue(),
                    ((Number) Expression.getRealValue(feature, list.get(2), expressionParams)).doubleValue(),
                    ((Number) Expression.getRealValue(feature, list.get(3), expressionParams)).doubleValue(),
                    expressionParams.getGeometryFactory()
            );
        }
        if (value instanceof TileClip) {
            return (TileClip) value;
        }
        if (value instanceof Bbox) {
            Bbox bbox = (Bbox) value;
            return new TileClip(bbox.xmin, bbox.ymin, bbox.xmax, bbox.ymax, expressionParams.getGeometryFactory());
        }
        throw new RuntimeException("未知的TileClip数据类型 " + value);
    }

}
