package org.wowtools.giscat.vector.mbexpression;

import org.junit.Assert;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.util.analyse.Bbox;
import org.wowtools.giscat.vector.util.analyse.TileClip;

import java.util.*;

public class ExpressionTest {

    private static Object getValue(Feature feature, String strExpression) {
        ExpressionParams expressionParams = new ExpressionParams();
        Expression expression = Expression.newInstance(strExpression);
        return expression.getValue(feature, expressionParams);
    }

    private static Object getValue(Feature feature, String strExpression, ExpressionParams expressionParams) {
        Expression expression = Expression.newInstance(strExpression);
        return expression.getValue(feature, expressionParams);
    }

    private static Feature buildTestFeature() {
        LineString geo = new GeometryFactory().createLineString(new Coordinate[]{
                new Coordinate(10, 10),
                new Coordinate(20, 20)
        });
        Feature feature = new Feature(geo, Map.of(
                "str1", "1",
                "str2", "2",
                "int1", 1,
                "int2", 2,
                "double1", 1d,
                "double2", 2d
        ));
        return feature;
    }

    @org.junit.Test
    public void filterIterator() {
        List<Feature> features = new LinkedList<>();
        for (int i = 1; i <= 100; i++) {
            Geometry geo = new GeometryFactory().createPoint(new Coordinate(i, i));
            Feature feature = new Feature(geo, Map.of(
                    "id", i
            ));
            features.add(feature);
        }
        {
            Expression expression = Expression.newInstance("[\"bboxIntersection\",\"$1\"]");
            Iterator<Feature> iterator = expression.filter(features.iterator(), new ExpressionParams(Map.of("$1", new Bbox(10.1, 10.1, 100, 100))));
            int i = 0;
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                int id = (int) feature.getProperties().get("id");
                Assert.assertTrue(id > 10);
                i++;
            }
            Assert.assertEquals(90, i);
        }
        {
            Expression expression = Expression.newInstance("[\">\", [\"get\", \"id\"],10]");
            Iterator<Feature> iterator = expression.filter(features.iterator(), new ExpressionParams());
            int i = 0;
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                int id = (int) feature.getProperties().get("id");
                Assert.assertTrue(id > 10);
                i++;
            }
            Assert.assertEquals(90, i);
        }
    }

    @org.junit.Test
    public void expressionParams() {
        Feature feature = buildTestFeature();
        Assert.assertEquals(true,
                getValue(feature,
                        "[\"==\", \"$1\",\"$2\"]",
                        new ExpressionParams(Map.of("$1", 1, "$2", 1)))
        );
        Assert.assertEquals(true,
                getValue(feature,
                        "[\"==\", \"$1\",\"$2\"]",
                        new ExpressionParams(Map.of("$1", "$1", "$2", "$1")))
        );
    }

    @org.junit.Test
    public void decision() {
        Feature feature = buildTestFeature();
//        System.out.println(GeoJsonFeatureConverter.toGeoJson(feature).toGeoJsonString());
        //all
        Assert.assertEquals(false,
                getValue(feature, "[\"all\", true,true,false]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\"all\", true,true,true]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\"all\", [\"get\", \"str1\"]]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"all\", [\"get\", \"str3\"]]")
        );
        //any
        Assert.assertEquals(true,
                getValue(feature, "[\"any\", true,true,false]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"any\", false,false,false]")
        );
        //case
        Assert.assertEquals(1,
                getValue(feature, "[\"case\", true,1,0]")
        );
        Assert.assertEquals(2,
                getValue(feature, "[\"case\", false,1,true,2,0]")
        );
        Assert.assertEquals(0,
                getValue(feature, "[\"case\", false,1,false,2,0]")
        );
        //==
        Assert.assertEquals(true,
                getValue(feature, "[\"==\", 1.0,1.0]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"==\", 1.0,1.1]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"==\", \"1\",1]")
        );
        //>=
        Assert.assertEquals(true,
                getValue(feature, "[\">=\", 1.0,1.0]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\">=\", 1.1,1]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\">=\", 0.9,1]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\">=\", \"a\",\"b\"]")
        );

        //>
        Assert.assertEquals(true,
                getValue(feature, "[\">\", 1.1,1.0]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\">\", 1.0,1.0]")
        );

        //<=
        Assert.assertEquals(true,
                getValue(feature, "[\"<=\", 1,2]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\"<=\", 1,1]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"<=\", 1,0.2]")
        );
        //<
        Assert.assertEquals(true,
                getValue(feature, "[\"<\", 1,2]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"<\", 1,1]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"<\", 1,0.2]")
        );
        //match
        Assert.assertEquals(2,
                getValue(feature, "[\"match\", \"a\", \"b\",1,\"a\",2,3]")
        );
        Assert.assertEquals(3,
                getValue(feature, "[\"match\", \"c\", \"b\",1,\"a\",2,3]")
        );
        //!
        Assert.assertEquals(true,
                getValue(feature, "[\"!\",false]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"!\",true]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"!\",123]")
        );
        //!=
        Assert.assertEquals(false,
                getValue(feature, "[\"!=\",1.0,1.0]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\"!=\",1.0,1.2]")
        );
        //组合测试
        Assert.assertEquals(true,
                getValue(feature, "[\"all\", [\"==\", [\"get\", \"int1\"], [\"get\", \"int1\"]]]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"all\", [\"==\", [\"get\", \"int1\"], [\"get\", \"int2\"]]]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\"all\", [\"==\", [\"get\", \"double1\"], [\"get\", \"double1\"]]]")
        );
    }

    @org.junit.Test
    public void lookup() {
        Feature feature = buildTestFeature();
        //at
        Assert.assertEquals(2,
                getValue(feature, "[\"at\",1,[1,2,3,4]]")
        );
        //get
        Assert.assertEquals("1",
                getValue(feature, "[\"get\",\"str1\"]")
        );
        //has
        Assert.assertEquals(true,
                getValue(feature, "[\"has\", \"int1\"]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"has\", \"int3\"]")
        );
        //in
        Assert.assertEquals(true,
                getValue(feature, "[\"in\", 1,[0,1]]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"in\", 1,[0,2]]")
        );
        Assert.assertEquals(true,
                getValue(feature, "[\"in\", \"a\",\"abc\"]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"in\", \"a\",\"bbc\"]")
        );
        //index-of
        Assert.assertEquals(2,
                getValue(feature, "[\"index-of\", \"a\",\"ihas\"]")
        );
        Assert.assertEquals(-1,
                getValue(feature, "[\"index-of\", \"a\",\"ihs\"]")
        );
        Assert.assertEquals(1,
                getValue(feature, "[\"index-of\", \"a\",[\"i\",\"a\",\"a\"]]")
        );
        Assert.assertEquals(2,
                getValue(feature, "[\"index-of\", \"a\",[\"i\",\"a\",\"a\"],2]")
        );
        //length
        Assert.assertEquals(4,
                getValue(feature, "[\"length\", \"ihas\"]")
        );
        Assert.assertEquals(3,
                getValue(feature, "[\"length\", [\"i\",\"a\",\"a\"]]")
        );
        //slice
        Assert.assertArrayEquals(new Object[]{2, 3, 4},
                ((ArrayList) getValue(feature, "[\"slice\",[1,2,3,4],1]")).toArray()
        );
        Assert.assertArrayEquals(new Object[]{2, 3},
                ((ArrayList) getValue(feature, "[\"slice\",[1,2,3,4],1,3]")).toArray()
        );
        Assert.assertEquals("bcd",
                getValue(feature, "[\"slice\",\"abcd\",1]")
        );
        Assert.assertEquals("bc",
                getValue(feature, "[\"slice\",\"abcd\",1,3]")
        );
    }

    @org.junit.Test
    public void math() {

    }

    @org.junit.Test
    public void spatial() throws Exception {
        WKTReader wktReader = new WKTReader();
        Feature feature;
        //bboxIntersection
        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"bboxIntersection\",[15,15,16,16]]");
        Assert.assertEquals("LINESTRING (15 15, 16 16)", feature.getGeometry().toText());

        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"bboxIntersection\",[\"$1\",\"$2\",\"$3\",\"$4\"]]"
                , new ExpressionParams(Map.of("$1", 15, "$2", 15, "$3", 16, "$4", 16)));
        Assert.assertEquals("LINESTRING (15 15, 16 16)", feature.getGeometry().toText());

        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"bboxIntersection\",\"$1\"]"
                , new ExpressionParams(Map.of("$1", new TileClip(15, 15, 16, 16, new GeometryFactory()))));
        Assert.assertEquals("LINESTRING (15 15, 16 16)", feature.getGeometry().toText());

        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"bboxIntersection\",[0,0,5,5]]");
        Assert.assertEquals(null, feature);
        //bboxIntersects
        feature = buildTestFeature();
        Assert.assertEquals(true,
                getValue(feature, "[\"bboxIntersects\",[0,0,50,50]]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"bboxIntersects\",[0,0,5,5]]")
        );

        Assert.assertEquals(true,
                getValue(feature, "[\"bboxIntersects\",[\"$1\",\"$2\",\"$3\",\"$4\"]]",
                        new ExpressionParams(Map.of("$1", 0, "$2", 0, "$3", 50, "$4", 50))
                )
        );

        ArrayList<Double> list = new ArrayList<>();
        list.add(0d);
        list.add(0d);
        list.add(50d);
        list.add(50d);
        Assert.assertEquals(true,
                getValue(feature, "[\"bboxIntersects\",\"$1\"]",
                        new ExpressionParams(Map.of("$1", list))
                )
        );

        Assert.assertEquals(true,
                getValue(feature, "[\"bboxIntersects\",\"$1\"]",
                        new ExpressionParams(Map.of("$1", new Bbox(0, 0, 50, 50)))
                )
        );

        //geoIntersection
        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"geoIntersection\",\"LINESTRING(0 0,16 16)\"]");
        Assert.assertEquals("LINESTRING (10 10, 16 16)", feature.getGeometry().toText());

        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"geoIntersection\",\"$1\"]",
                new ExpressionParams(Map.of("$1", "LINESTRING(0 0,16 16)")));
        Assert.assertEquals("LINESTRING (10 10, 16 16)", feature.getGeometry().toText());

        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"geoIntersection\",\"$1\"]",
                new ExpressionParams(Map.of("$1", wktReader.read("LINESTRING(0 0,16 16)"))));
        Assert.assertEquals("LINESTRING (10 10, 16 16)", feature.getGeometry().toText());

        feature = buildTestFeature();
        feature = (Feature) getValue(feature, "[\"geoIntersection\",\"LINESTRING(0 0,1 1)\"]");
        Assert.assertEquals(null, feature);
        //geoIntersects
        feature = buildTestFeature();
        Assert.assertEquals(true,
                getValue(feature, "[\"geoIntersects\",\"LINESTRING(0 0,16 16)\"]")
        );
        Assert.assertEquals(false,
                getValue(feature, "[\"geoIntersects\",\"LINESTRING(0 0,1 1)\"]")
        );
    }

    @org.junit.Test
    public void string() {
        Feature feature = buildTestFeature();
        //concat
        Assert.assertEquals("abcd",
                getValue(feature, "[\"concat\",\"a\",\"b\",\"cd\"]")
        );
        //concat
        Assert.assertEquals("asd",
                getValue(feature, "[\"downcase\",\"aSd\"]")
        );
        //upcase
        Assert.assertEquals("ASD",
                getValue(feature, "[\"upcase\",\"aSd\"]")
        );
    }

}
