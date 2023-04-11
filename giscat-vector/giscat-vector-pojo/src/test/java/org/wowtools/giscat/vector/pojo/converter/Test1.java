package org.wowtools.giscat.vector.pojo.converter;

import org.wowtools.giscat.vector.pojo.converter.FoolStyleFeatureConverter;
import org.locationtech.jts.geom.*;

public class Test1 {
    public static void main(String[] args) {

        {
            Polygon p1 = FoolStyleFeatureConverter.array2Polygon(new double[]{150, 330, 260, 380, 380, 240});
            Polygon p2 = FoolStyleFeatureConverter.array2Polygon(new double[]{190, 260, 269, 325, 290, 160});
            System.out.println("p1与p2是否相交 "+p1.intersects(p2));//true
            System.out.println("p1与p2的交集 "+p1.intersection(p2));//POLYGON ((274.56738768718805 281.25623960066554, 234.76427923844062 296.83136899365365, 269 325, 274.56738768718805 281.25623960066554))
            System.out.println("交集的面积 "+p1.intersection(p2).getArea());//827.2124277609248
            System.out.println("交集的周长 "+p1.intersection(p2).getLength());//131.17314524175666
        }

        {
            LineString l1 = FoolStyleFeatureConverter.array2Line(new double[]{190, 180, 170, 240, 250, 320, 323, 205});
            Polygon p1 = FoolStyleFeatureConverter.array2Polygon(new double[]{150, 330, 260, 380, 380, 240});
            System.out.println("线与多边形是否相交 " + l1.intersects(p1));//true
            System.out.println("线与多边形的交集 "+l1.intersection(p1));//LINESTRING (229.0625 299.0625, 250 320, 274.60261569416497 281.2424547283702)
            System.out.println("相交线段长度 "+l1.intersection(p1).getLength());//75.51691528550752
        }

        {
            Point p = FoolStyleFeatureConverter.xy2Point(250, 310);
            Polygon p1 = FoolStyleFeatureConverter.array2Polygon(new double[]{150, 330, 260, 380, 380, 240});
            System.out.println("点与多边形是否相交 " + p.intersects(p1));//true
        }
    }
}
