package org.wowtools.giscat.vector.mbexpression.spatial;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.wowtools.giscat.util.analyse.Bbox;

import java.util.ArrayList;

/**
 * 从表达式中读取geometry或bbox
 *
 * @author liuyu
 * @date 2022/7/26
 */
class Read {
    private static final WKTReader wktReader = new WKTReader();


    public static Geometry readGeometry(Object value) {
        if (null == value) {
            return null;
        }
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

    public static Bbox readBbox(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            Bbox bbox = new Bbox((double) list.get(0), (double) list.get(1), (double) list.get(2), (double) list.get(3));
            return bbox;
        }
        if (value instanceof Bbox) {
            return (Bbox) value;
        }
        throw new RuntimeException("未知的Bbox数据类型 " + value);
    }
}
