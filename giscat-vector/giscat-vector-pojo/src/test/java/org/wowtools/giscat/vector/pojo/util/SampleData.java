package org.wowtools.giscat.vector.pojo.util;

import org.locationtech.jts.geom.*;
import org.wowtools.giscat.vector.pojo.GeoJsonObject;
import org.wowtools.giscat.vector.pojo.converter.GeoJsonFeatureConverter;

/**
 * 样例数据
 * 来自 https://en.wikipedia.org/wiki/GeoJSON
 *
 * @author liuyu
 * @date 2022/3/25
 */
public class SampleData {

    public static final GeometryFactory geometryFactory = new GeometryFactory();

    public static final String strPoint = "{\n" +
            "    \"type\": \"Point\", \n" +
            "    \"coordinates\": [30.0, 10.0]\n" +
            "}";
    public static final Point point;

    static {
        point = geometryFactory.createPoint(new Coordinate(30.0, 10.0));
    }

    public static final GeoJsonObject.Geometry pointGeoJson = GeoJsonFeatureConverter.geometry2GeoJson(point);


    public static final String strLineString = "{\n" +
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

    public static final GeoJsonObject.Geometry lineStringGeoJson = GeoJsonFeatureConverter.geometry2GeoJson(lineString);


    public static final String strPolygon1 = "{\n" +
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

    public static final GeoJsonObject.Geometry polygon1GeoJson = GeoJsonFeatureConverter.geometry2GeoJson(polygon1);

    public static final String strPolygon2 = "{\n" +
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

    public static final GeoJsonObject.Geometry polygon2GeoJson = GeoJsonFeatureConverter.geometry2GeoJson(polygon2);


    public static final String strPolygon3 = "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[10.0, 10.0], [20.0, 10.0], [20.0, 20.0],[10.0, 20.0], [10.0, 10.0]], \n" +
            "        [[11.0, 11.0], [12.0, 11.0], [12.0, 12.0], [11.0, 11.0]], \n" +
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

    public static final GeoJsonObject.Geometry polygon3GeoJson = GeoJsonFeatureConverter.geometry2GeoJson(polygon3);

    public static final String strMultiPoint = "{\n" +
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

    public static final GeoJsonObject.Geometry multiPointGeoJson = GeoJsonFeatureConverter.geometry2GeoJson(multiPoint);

    public static final String strMultiLineString = "{\n" +
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

    public static final GeoJsonObject.Geometry multiLineStringGeoJson = GeoJsonFeatureConverter.geometry2GeoJson(multiLineString);

    public static final String strMultiPolygon1 = "{\n" +
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

    public static final GeoJsonObject.Geometry multiPolygon1GeoJson = GeoJsonFeatureConverter.geometry2GeoJson(multiPolygon1);

    public static final String strMultiPolygon2 = "{\n" +
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

    public static final GeoJsonObject.Geometry multiPolygon2GeoJson = GeoJsonFeatureConverter.geometry2GeoJson(multiPolygon2);

    public static final String strGeometryCollection = "{\n" +
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

    public static final GeoJsonObject.Geometry geometryCollectionGeoJson = GeoJsonFeatureConverter.geometry2GeoJson(geometryCollection);

    public static final String strFeatureCollection1 = "{\n" +
            "  \"type\": \"FeatureCollection\",\n" +
            "  \"features\": [\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"stroke\": \"#555555\",\n" +
            "        \"stroke-width\": 2,\n" +
            "        \"stroke-opacity\": 1,\n" +
            "        \"sss\": \"sssaa\"\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"LineString\",\n" +
            "        \"coordinates\": [\n" +
            "          [\n" +
            "            101.173095703125,\n" +
            "            24.93127614538456\n" +
            "          ],\n" +
            "          [\n" +
            "            101.986083984375,\n" +
            "            25.145284610685064\n" +
            "          ],\n" +
            "          [\n" +
            "            102.1893310546875,\n" +
            "            24.98107885823501\n" +
            "          ],\n" +
            "          [\n" +
            "            103.41430664062499,\n" +
            "            24.961160190729043\n" +
            "          ],\n" +
            "          [\n" +
            "            103.447265625,\n" +
            "            25.418470119273117\n" +
            "          ],\n" +
            "          [\n" +
            "            102.7496337890625,\n" +
            "            25.878994400196202\n" +
            "          ],\n" +
            "          [\n" +
            "            103.546142578125,\n" +
            "            25.94816628853973\n" +
            "          ]\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"stroke\": \"#555555\",\n" +
            "        \"stroke-width\": 2,\n" +
            "        \"stroke-opacity\": 1,\n" +
            "        \"aaa\": 213,\n" +
            "        \"asd\": \"aaa\"\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"LineString\",\n" +
            "        \"coordinates\": [\n" +
            "          [\n" +
            "            103.9031982421875,\n" +
            "            26.115985925333536\n" +
            "          ],\n" +
            "          [\n" +
            "            104.04602050781249,\n" +
            "            25.567220388070023\n" +
            "          ],\n" +
            "          [\n" +
            "            104.56787109374999,\n" +
            "            25.23972731233395\n" +
            "          ],\n" +
            "          [\n" +
            "            104.0789794921875,\n" +
            "            24.557116164309626\n" +
            "          ]\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"marker-color\": \"#7e7e7e\",\n" +
            "        \"marker-size\": \"medium\",\n" +
            "        \"marker-symbol\": \"\",\n" +
            "        \"ss\": 1\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [\n" +
            "          102.733154296875,\n" +
            "          26.204734267107604\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"marker-color\": \"#7e7e7e\",\n" +
            "        \"marker-size\": \"medium\",\n" +
            "        \"marker-symbol\": \"\",\n" +
            "        \"aa\": 11\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [\n" +
            "          104.4525146484375,\n" +
            "          26.347575438494673\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {},\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Point\",\n" +
            "        \"coordinates\": [\n" +
            "          101.766357421875,\n" +
            "          25.750424835909385\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"stroke\": \"#555555\",\n" +
            "        \"stroke-width\": 2,\n" +
            "        \"stroke-opacity\": 1,\n" +
            "        \"fill\": \"#555555\",\n" +
            "        \"fill-opacity\": 0.5,\n" +
            "        \"ss\": true\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Polygon\",\n" +
            "        \"coordinates\": [\n" +
            "          [\n" +
            "            [\n" +
            "              100.4425048828125,\n" +
            "              26.426308999847024\n" +
            "            ],\n" +
            "            [\n" +
            "              100.6182861328125,\n" +
            "              25.70588750345636\n" +
            "            ],\n" +
            "            [\n" +
            "              101.10717773437499,\n" +
            "              25.859223554761407\n" +
            "            ],\n" +
            "            [\n" +
            "              101.77734374999999,\n" +
            "              26.534479888888043\n" +
            "            ],\n" +
            "            [\n" +
            "              101.0028076171875,\n" +
            "              26.775039386999605\n" +
            "            ],\n" +
            "            [\n" +
            "              100.4425048828125,\n" +
            "              26.426308999847024\n" +
            "            ]\n" +
            "          ]\n" +
            "        ]\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"Feature\",\n" +
            "      \"properties\": {\n" +
            "        \"stroke\": \"#555555\",\n" +
            "        \"stroke-width\": 2,\n" +
            "        \"stroke-opacity\": 1,\n" +
            "        \"fill\": \"#555555\",\n" +
            "        \"fill-opacity\": 0.5,\n" +
            "        \"aa\": \"ss\"\n" +
            "      },\n" +
            "      \"geometry\": {\n" +
            "        \"type\": \"Polygon\",\n" +
            "        \"coordinates\": [\n" +
            "          [\n" +
            "            [\n" +
            "              102.50244140624999,\n" +
            "              27.381523191705053\n" +
            "            ],\n" +
            "            [\n" +
            "              103.22753906249999,\n" +
            "              26.931865156388916\n" +
            "            ],\n" +
            "            [\n" +
            "              103.73291015625,\n" +
            "              27.244862521497282\n" +
            "            ],\n" +
            "            [\n" +
            "              103.22753906249999,\n" +
            "              27.46928747369202\n" +
            "            ],\n" +
            "            [\n" +
            "              102.50244140624999,\n" +
            "              27.381523191705053\n" +
            "            ]\n" +
            "          ]\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

}
