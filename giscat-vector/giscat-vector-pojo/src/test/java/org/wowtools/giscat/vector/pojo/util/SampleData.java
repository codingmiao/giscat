package org.wowtools.giscat.vector.pojo.util;

import org.json.JSONObject;
import org.locationtech.jts.geom.*;

/**
 * 样例数据
 * 来自 https://en.wikipedia.org/wiki/GeoJSON
 *
 * @author liuyu
 * @date 2022/3/25
 */
public class SampleData {

    public static final GeometryFactory geometryFactory = new GeometryFactory();

    private static final String strPoint = "{\n" +
            "    \"type\": \"Point\", \n" +
            "    \"coordinates\": [30.0, 10.0]\n" +
            "}";
    public static final Point point;

    static {
        point = geometryFactory.createPoint(new Coordinate(30.0, 10.0));
    }

    public static final JSONObject pointGeoJson = new JSONObject(strPoint);


    private static final String strLineString = "{\n" +
            "    \"type\": \"LineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [30.0, 10.0], [10.0, 30.0], [40.0, 40.0]\n" +
            "    ]\n" +
            "}";
    public static final LineString lineString;

    static {
        lineString = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(30.0, 10.0),
                new Coordinate(10.0, 30.0),
                new Coordinate(40.0, 40.0)
        });
    }

    public static final JSONObject lineStringGeoJson = new JSONObject(strLineString);


    private static final String strPolygon1 = "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[30.0, 10.0], [40.0, 40.0], [20.0, 40.0], [10.0, 20.0], [30.0, 10.0]]\n" +
            "    ]\n" +
            "}";

    public static final Polygon polygon1;

    static {
        polygon1 = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(30.0, 10.0),
                new Coordinate(40.0, 40.0),
                new Coordinate(20.0, 40.0),
                new Coordinate(10.0, 20.0),
                new Coordinate(30.0, 10.0)
        });
    }

    public static final JSONObject polygon1GeoJson = new JSONObject(strPolygon1);

    private static final String strPolygon2 = "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[35.0, 10.0], [45.0, 45.0], [15.0, 40.0], [10.0, 20.0], [35.0, 10.0]], \n" +
            "        [[20.0, 30.0], [35.0, 35.0], [30.0, 20.0], [20.0, 30.0]]\n" +
            "    ]\n" +
            "}";

    public static final Polygon polygon2;

    static {
        LinearRing shell = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(35.0, 10.0),
                new Coordinate(45.0, 45.0),
                new Coordinate(15.0, 40.0),
                new Coordinate(10.0, 20.0),
                new Coordinate(35.0, 10.0),
        });
        LinearRing[] holes = new LinearRing[]{
                geometryFactory.createLinearRing(new Coordinate[]{
                        new Coordinate(20.0, 30.0),
                        new Coordinate(35.0, 35.0),
                        new Coordinate(30.0, 20.0),
                        new Coordinate(20.0, 30.0)
                })
        };
        polygon2 = geometryFactory.createPolygon(shell, holes);
    }

    public static final JSONObject polygon2GeoJson = new JSONObject(strPolygon2);


    private static final String strPolygon3 = "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[10.0, 10.0], [20.0, 10.0], [20.0, 20.0],[10.0, 20.0], [10.0, 10.0]], \n" +
            "        [[11.0, 11.0], [12.0, 11.0], [12.0, 12.0], [20.0, 30.0]], \n" +
            "        [[17.0, 17.0], [18.0, 17.0], [18.0, 18.0], [17.0, 17.0]]\n" +
            "    ]\n" +
            "}";

    public static final Polygon polygon3;

    static {
        LinearRing shell = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(10.0, 10.0),
                new Coordinate(20.0, 10.0),
                new Coordinate(20.0, 20.0),
                new Coordinate(10.0, 20.0),
                new Coordinate(10.0, 10.0),
        });
        LinearRing[] holes = new LinearRing[]{
                geometryFactory.createLinearRing(new Coordinate[]{
                        new Coordinate(11.0, 11.0),
                        new Coordinate(12.0, 11.0),
                        new Coordinate(12.0, 12.0),
                        new Coordinate(11.0, 11.0)
                }),
                geometryFactory.createLinearRing(new Coordinate[]{
                        new Coordinate(17.0, 17.0),
                        new Coordinate(18.0, 17.0),
                        new Coordinate(18.0, 18.0),
                        new Coordinate(17.0, 17.0)
                })
        };
        polygon3 = geometryFactory.createPolygon(shell, holes);
    }

    public static final JSONObject polygon3GeoJson = new JSONObject(strPolygon3);

    private static final String strMultiPoint = "{\n" +
            "    \"type\": \"MultiPoint\", \n" +
            "    \"coordinates\": [\n" +
            "        [10.0, 40.0], [40.0, 30.0], [20.0, 20.0], [30.0, 10.0]\n" +
            "    ]\n" +
            "}";

    public static final MultiPoint multiPoint;

    static {
        multiPoint = geometryFactory.createMultiPointFromCoords(new Coordinate[]{
                new Coordinate(10.0, 40.0),
                new Coordinate(40.0, 30.0),
                new Coordinate(20.0, 20.0),
                new Coordinate(30.0, 10.0)
        });
    }

    public static final JSONObject multiPointGeoJson = new JSONObject(strMultiPoint);

    private static final String strMultiLineString = "{\n" +
            "    \"type\": \"MultiLineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [[10.0, 10.0], [20.0, 20.0], [10.0, 40.0]], \n" +
            "        [[40.0, 40.0], [30.0, 30.0], [40.0, 20.0], [30.0, 10.0]]\n" +
            "    ]\n" +
            "}";

    public static final MultiLineString multiLineString;

    static {
        LineString[] lineStrings = new LineString[]{
                geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(10.0, 10.0),
                        new Coordinate(20.0, 20.0),
                        new Coordinate(10.0, 40.0),
                }),
                geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(40.0, 40.0),
                        new Coordinate(30.0, 30.0),
                        new Coordinate(40.0, 20.0),
                        new Coordinate(30.0, 10.0)
                })
        };
        multiLineString = geometryFactory.createMultiLineString(lineStrings);
    }

    public static final JSONObject multiLineStringGeoJson = new JSONObject(strMultiLineString);

    private static final String strMultiPolygon1 = "{\n" +
            "    \"type\": \"MultiPolygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [\n" +
            "            [[30.0, 20.0], [45.0, 40.0], [10.0, 40.0], [30.0, 20.0]]\n" +
            "        ], \n" +
            "        [\n" +
            "            [[15.0, 5.0], [40.0, 10.0], [10.0, 20.0], [5.0, 10.0], [15.0, 5.0]]\n" +
            "        ]\n" +
            "    ]\n" +
            "}";

    public static final MultiPolygon multiPolygon1;

    static {
        Polygon[] polygons = new Polygon[]{
                geometryFactory.createPolygon(new Coordinate[]{
                        new Coordinate(30.0, 20.0),
                        new Coordinate(45.0, 40.0),
                        new Coordinate(10.0, 40.0),
                        new Coordinate(30.0, 20.0),
                }),
                geometryFactory.createPolygon(new Coordinate[]{
                        new Coordinate(15.0, 5.0),
                        new Coordinate(40.0, 10.0),
                        new Coordinate(10.0, 20.0),
                        new Coordinate(5.0, 10.0),
                        new Coordinate(15.0, 5.0)
                }),
        };
        multiPolygon1 = geometryFactory.createMultiPolygon(polygons);
    }

    public static final JSONObject multiPolygon1GeoJson = new JSONObject(strMultiPolygon1);

    private static final String strMultiPolygon2 = "{\n" +
            "    \"type\": \"MultiPolygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [\n" +
            "            [[40.0, 40.0], [20.0, 45.0], [45.0, 30.0], [40.0, 40.0]]\n" +
            "        ], \n" +
            "        [\n" +
            "            [[20.0, 35.0], [10.0, 30.0], [10.0, 10.0], [30.0, 5.0], [45.0, 20.0], [20.0, 35.0]], \n" +
            "            [[30.0, 20.0], [20.0, 15.0], [20.0, 25.0], [30.0, 20.0]]\n" +
            "        ]\n" +
            "    ]\n" +
            "}";

    public static final MultiPolygon multiPolygon2;

    static {
        Polygon polygon1 = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(40.0, 40.0),
                new Coordinate(20.0, 45.0),
                new Coordinate(45.0, 30.0),
                new Coordinate(40.0, 40.0),
        });

        LinearRing shell = geometryFactory.createLinearRing(new Coordinate[]{
                new Coordinate(20.0, 35.0),
                new Coordinate(10.0, 30.0),
                new Coordinate(10.0, 10.0),
                new Coordinate(30.0, 5.0),
                new Coordinate(45.0, 20.0),
                new Coordinate(20.0, 35.0)
        });
        LinearRing[] holes = new LinearRing[]{
                geometryFactory.createLinearRing(new Coordinate[]{
                        new Coordinate(30.0, 20.0),
                        new Coordinate(20.0, 15.0),
                        new Coordinate(20.0, 25.0),
                        new Coordinate(30.0, 20.0)
                })
        };
        Polygon polygon2 = geometryFactory.createPolygon(shell, holes);

        Polygon[] polygons = new Polygon[]{
                polygon1,
                polygon2,
        };
        multiPolygon2 = geometryFactory.createMultiPolygon(polygons);
    }

    public static final JSONObject multiPolygon2GeoJson = new JSONObject(strMultiPolygon2);

    private static final String strGeometryCollection = "{\n" +
            "    \"type\": \"GeometryCollection\",\n" +
            "    \"geometries\": [\n" +
            "        {\n" +
            "            \"type\": \"Point\",\n" +
            "            \"coordinates\": [40.0, 10.0]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"LineString\",\n" +
            "            \"coordinates\": [\n" +
            "                [10.0, 10.0], [20.0, 20.0], [10.0, 40.0]\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Polygon\",\n" +
            "            \"coordinates\": [\n" +
            "                [[40.0, 40.0], [20.0, 45.0], [45.0, 30.0], [40.0, 40.0]]\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static final GeometryCollection geometryCollection;

    static {
        Point point = geometryFactory.createPoint(new Coordinate(40.0, 10.0));
        LineString lineString = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(10.0, 10.0),
                new Coordinate(20.0, 20.0),
                new Coordinate(10.0, 40.0)
        });
        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(40.0, 40.0),
                new Coordinate(20.0, 45.0),
                new Coordinate(45.0, 30.0),
                new Coordinate(40.0, 40.0)
        });
        Geometry[] geometries = new Geometry[]{
                point,
                lineString,
                polygon
        };
        geometryCollection = geometryFactory.createGeometryCollection(geometries);
    }

    public static final JSONObject geometryCollectionGeoJson = new JSONObject(strGeometryCollection);

}
