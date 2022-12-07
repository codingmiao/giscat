/*****************************************************************
 *  Copyright (c) 2022- "giscat by 刘雨 (https://github.com/codingmiao/giscat)"
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.wowtools.giscat.util.analyse;

import org.locationtech.jts.geom.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 高效的瓦片中数据裁剪器扩展,用以提高VectorTileEncoder的性能
 * 尤其是线类型，与jts的intersection方法对比，性能提高了好几个数量级，jmh测试结果如下:
 * Benchmark                 Mode  Cnt          Score           Error  Units
 * TileClipJmhTest.jts       avgt    5  132439738.660 ± 133440464.161  ns/op
 * TileClipJmhTest.tileClip  avgt    5     260511.811 ±     49188.573  ns/op
 *
 * @author liuyu
 */
public class TileClip {
    private final GeometryFactory gf;

    private final Geometry clipGeometry;
    private final double xmin;
    private final double ymin;
    private final double xmax;
    private final double ymax;

    public TileClip(double xmin, double ymin, double xmax, double ymax, GeometryFactory gf) {
        this.gf = gf;
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(xmin, ymin),
                new Coordinate(xmax, ymin),
                new Coordinate(xmax, ymax),
                new Coordinate(xmin, ymax),
                new Coordinate(xmin, ymin)
        };
        clipGeometry = gf.createPolygon(coords);
    }

    /**
     * 获取瓦片和图形的交集
     *
     * @param geometry geometry
     * @return 交集
     */
    public Geometry intersection(Geometry geometry) {
        if (geometry instanceof Point) {
            return intersectionPoint((Point) geometry);
        }

        if (geometry instanceof LineString) {
            return intersectionLineString((LineString) geometry);
        }

        if (geometry instanceof MultiLineString) {
            MultiLineString multiLineString = (MultiLineString) geometry;
            List<LineString> subs = new LinkedList<>();
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                LineString line = (LineString) multiLineString.getGeometryN(i);
                Geometry sub = intersectionLineString(line);
                if (sub instanceof LineString) {
                    subs.add((LineString) sub);
                } else if (sub instanceof MultiLineString) {
                    for (int i1 = 0; i1 < sub.getNumGeometries(); i1++) {
                        subs.add((LineString) sub.getGeometryN(i1));
                    }
                }
            }
            LineString[] lines = new LineString[subs.size()];
            subs.toArray(lines);
            return gf.createMultiLineString(lines);
        }

        return clipGeometry.intersection(geometry);
    }

    /////////////////////////点的处理
    private Point intersectionPoint(Point point) {
        if (inTile(point.getX(), point.getY())) {
            return point;
        }
        return null;
    }
    /////////////////////////end点的处理

    /////////////////////////线的处理
    enum Indexed {
        start, middle, end
    }

    static final class IndexedCoordinate {
        Indexed indexed;
        final Coordinate coordinate;

        public IndexedCoordinate(Indexed indexed, Coordinate coordinate) {
            this.indexed = indexed;
            this.coordinate = coordinate;
        }
    }

    private static final class IntersectionLineStringCtx {
        LinkedList<IndexedCoordinate> coordinates = new LinkedList<>();
        Coordinate beforeCoord;//上一个点
        boolean beforeIn;//上一个点是否在线片内
    }

    private Geometry intersectionLineString(LineString line) {
        Coordinate[] coords = line.getCoordinates();
        IntersectionLineStringCtx ctx = new IntersectionLineStringCtx();
        Coordinate coord = coords[0];
        ctx.beforeCoord = coord;
        ctx.beforeIn = inTile(coord.x, coord.y);
        if (ctx.beforeIn) {
            ctx.coordinates.add(new IndexedCoordinate(Indexed.middle, coord));
        }
        for (int i = 1; i < coords.length; i++) {
            coord = coords[i];
            double x = coord.x;
            double y = coord.y;
            boolean in = inTile(x, y);
            if (in) {//按当前点和前一个点是否在tile内分别处理
                if (ctx.beforeIn) {
                    lineIn2In(ctx, coord, x, y);
                } else {
                    lineOut2In(ctx, coord, x, y);
                }
            } else {
                if (ctx.beforeIn) {
                    lineIn2Out(ctx, coord, x, y);
                } else {
                    lineOut2Out(ctx, coord, x, y);
                }
            }
            ctx.beforeCoord = coord;
            ctx.beforeIn = in;
        }
        if (ctx.coordinates.size() < 2) {
            return null;
        }

        ctx.coordinates.getFirst().indexed = Indexed.start;
        ctx.coordinates.getLast().indexed = Indexed.end;
        LinkedList<LinkedList<Coordinate>> lineCoordsList = new LinkedList<>();
        LinkedList<Coordinate> beforeLineCoords = null;

        for (IndexedCoordinate indexedCoordinate : ctx.coordinates) {
            if (indexedCoordinate.indexed == Indexed.start) {
                beforeLineCoords = new LinkedList<>();
                beforeLineCoords.add(indexedCoordinate.coordinate);
            } else if (indexedCoordinate.indexed == Indexed.end) {
                beforeLineCoords.add(indexedCoordinate.coordinate);
                lineCoordsList.add(beforeLineCoords);
            } else {
                beforeLineCoords.add(indexedCoordinate.coordinate);
            }
        }

        if (lineCoordsList.size() == 1) {
            LinkedList<Coordinate> lineCoords = lineCoordsList.getLast();
            Coordinate[] coordinates = new Coordinate[lineCoords.size()];
            lineCoords.toArray(coordinates);
            return gf.createLineString(coordinates);
        } else {
            LineString[] lineStringArr = new LineString[lineCoordsList.size()];
            int i = 0;
            for (LinkedList<Coordinate> lineCoords : lineCoordsList) {
                Coordinate[] coordinates = new Coordinate[lineCoords.size()];
                lineCoords.toArray(coordinates);
                lineStringArr[i] = gf.createLineString(coordinates);
                i++;
            }
            return gf.createMultiLineString(lineStringArr);
        }


    }

    /**
     * 从内部到内部
     *
     * @param ctx
     * @param coord
     */
    private void lineIn2In(IntersectionLineStringCtx ctx, Coordinate coord, double x, double y) {
        ctx.coordinates.add(new IndexedCoordinate(Indexed.middle, coord));
    }

    /**
     * 从内部到外部
     *
     * @param ctx
     * @param coord
     */
    private void lineIn2Out(IntersectionLineStringCtx ctx, Coordinate coord, double x, double y) {
        Coordinate intersection = getLineInOutIntersection(ctx.beforeCoord.x, ctx.beforeCoord.y, x, y);
        ctx.coordinates.add(new IndexedCoordinate(Indexed.end, intersection));
    }

    /**
     * 从外部到内部
     *
     * @param ctx
     * @param coord
     */
    private void lineOut2In(IntersectionLineStringCtx ctx, Coordinate coord, double x, double y) {
        Coordinate intersection = getLineInOutIntersection(x, y, ctx.beforeCoord.x, ctx.beforeCoord.y);
        ctx.coordinates.add(new IndexedCoordinate(Indexed.start, intersection));
        ctx.coordinates.add(new IndexedCoordinate(Indexed.middle, coord));
    }

    /**
     * 从外部到外部
     *
     * @param ctx
     * @param coord
     */
    private void lineOut2Out(IntersectionLineStringCtx ctx, Coordinate coord, double x, double y) {
        if (!intersects(new Coordinate[]{coord, ctx.beforeCoord})) {
            return;
        }
        Coordinate[] coords = getLineOutIntersection(ctx.beforeCoord.x, ctx.beforeCoord.y, x, y);
        if (null == coords) {
            return;
        }
        ctx.coordinates.add(new IndexedCoordinate(Indexed.start, coords[0]));
        ctx.coordinates.add(new IndexedCoordinate(Indexed.end, coords[coords.length - 1]));
    }
    /////////////////////////end线的处理


    private boolean inTile(double x, double y) {
        return x >= xmin && x <= xmax && y >= ymin && y <= ymax;
    }

    private Coordinate getLineInOutIntersection(double inX, double inY, double outX, double outY) {
        //逐一判断和四条边中的哪条相交
        //上
        double xUp = getLineX(inX, inY, outX, outY, ymax);
        if (outY > ymax && xUp >= xmin && xUp <= xmax) {
            return new Coordinate(xUp, ymax);
        }
        //下
        double xDown = getLineX(inX, inY, outX, outY, ymin);
        if (outY < ymin && xDown >= xmin && xDown <= xmax) {
            return new Coordinate(xDown, ymin);
        }
        double verticalY = outY > ymax ? ymax : ymin;
        //左
        double yLeft = getLineY(inX, inY, outX, outY, xmin, verticalY);
        if (outX < xmin && yLeft >= ymin && yLeft <= ymax) {
            return new Coordinate(xmin, yLeft);
        }
        //右
        double yRigth = getLineY(inX, inY, outX, outY, xmax, verticalY);
        if (outX > xmax && yRigth >= ymin && yRigth <= ymax) {
            return new Coordinate(xmax, yRigth);
        }
        return new Coordinate(inX, inY);
    }

    protected Coordinate[] getLineOutIntersection(double x1, double y1, double x2, double y2) {
        //横穿
        if (y1 == y2) {
            if (y1 > ymax || y1 < ymin) {//无交集
                return null;
            }
            Coordinate c1 = new Coordinate(xmin, y1);
            Coordinate c2 = new Coordinate(xmax, y1);
            if (x1 < x2) {
                return new Coordinate[]{c1, c2};
            } else {
                return new Coordinate[]{c2, c1};
            }
        }
        //纵穿
        if (x1 == x2) {
            if (x1 > xmax || x1 < xmin) {//无交集
                return null;
            }
            Coordinate c1 = new Coordinate(x1, xmin);
            Coordinate c2 = new Coordinate(x1, xmax);
            if (y1 < y2) {
                return new Coordinate[]{c1, c2};
            } else {
                return new Coordinate[]{c2, c1};
            }
        }
        double yLeft = getLineY(x1, y1, x2, y2, xmin, 0);
        if ((x1 <= xmin || x2 <= xmin) && yLeft >= ymin && yLeft <= ymax) {//与左边界相交
            Coordinate c1, c2;
            double yRight = getLineY(x1, y1, x2, y2, xmax, 0);
            if (yRight > ymax) {//与上边界相交
                double xUp = getLineX(x1, y1, x2, y2, ymax);
                if (xUp < xmin || xUp > xmax) {
                    return null;
                }
                c1 = new Coordinate(xmin, yLeft);
                c2 = new Coordinate(xUp, ymax);
            } else if (yRight < ymin) {//与下边界相交
                double xDown = getLineX(x1, y1, x2, y2, ymin);
                if (xDown < xmin || xDown > xmax) {
                    return null;
                }
                c1 = new Coordinate(xmin, yLeft);
                c2 = new Coordinate(xDown, ymin);
            } else {//与右边界相交
//                if (x1 < xmax && x2 < xmax){
//                    return null;
//                }
                c1 = new Coordinate(xmin, yLeft);
                c2 = new Coordinate(xmax, yRight);
            }
            if (x1 < x2) {
                return new Coordinate[]{c1, c2};
            } else {
                return new Coordinate[]{c2, c1};
            }
        } else {//与右边界相交或上下边界相交或无交集
            Coordinate c1, c2;
            double xUp = getLineX(x1, y1, x2, y2, ymax);
            double yRight = getLineY(x1, y1, x2, y2, xmax, 0);
            if (yRight > ymax || yRight < ymin) {
                double xDown = getLineX(x1, y1, x2, y2, ymin);
                if (xDown < xmin || xDown > xmax) {//无交集
                    return null;
                }
                //上下边界相交
                c1 = new Coordinate(xUp, ymax);
                c2 = new Coordinate(xDown, ymin);
                if (y1 > y2) {
                    return new Coordinate[]{c1, c2};
                } else {
                    return new Coordinate[]{c2, c1};
                }
            }
            if (xUp < xmax && xUp > xmin) {//与上边界相交
                c1 = new Coordinate(xUp, ymax);
            } else {//与下边界相交
                double xDown = getLineX(x1, y1, x2, y2, ymin);
                if (xDown < xmin || xDown > xmax) {//无交集
                    return null;
                }
                c1 = new Coordinate(xDown, ymin);
            }
            c2 = new Coordinate(xmax, yRight);
            if (x1 < x2) {
                return new Coordinate[]{c1, c2};
            } else {
                return new Coordinate[]{c2, c1};
            }
        }

    }

    /**
     * 已知直线上两点及y，求x
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param y
     * @return
     */
    private double getLineX(double x1, double y1, double x2, double y2, double y) {
        if (x1 == x2) {
            return x1;
        }
        double k = (y1 - y2) / (x1 - x2);
        double b = y1 - k * x1;
        return (y - b) / k;
    }

    /**
     * 已知直线上两点及x，求y
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param x
     * @param verticalY 当线竖直时y的取值
     * @return
     */
    private double getLineY(double x1, double y1, double x2, double y2, double x, double verticalY) {
        if (x1 == x2) {
            return verticalY;
        }
        double k = (y1 - y2) / (x1 - x2);
        double b = y1 - k * x1;
        return k * x + b;
    }

    /**
     * 判断bbox是否与范围相交
     *
     * @return
     */
    private boolean intersects(Coordinate[] coords) {
        double x, y;
        Coordinate coordinate = coords[0];
        x = coordinate.x;
        y = coordinate.y;
        double xmin = x;
        double ymin = y;
        double xmax = x;
        double ymax = y;
        for (int i = 1; i < coords.length; i++) {
            coordinate = coords[i];
            x = coordinate.x;
            y = coordinate.y;
            if (x > xmax) {
                xmax = x;
            } else if (x < xmin) {
                xmin = x;
            }
            if (y > ymax) {
                ymax = y;
            } else if (y < ymin) {
                ymin = y;
            }
        }
        if (this.xmin > xmax) {
            return false;
        }
        if (this.xmax < xmin) {
            return false;
        }
        if (this.ymin > ymax) {
            return false;
        }
        return !(this.ymax < ymin);
    }

}
