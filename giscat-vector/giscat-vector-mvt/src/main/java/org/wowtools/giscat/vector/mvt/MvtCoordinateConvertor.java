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
package org.wowtools.giscat.vector.mvt;

/**
 * 矢量瓦片的坐标系与wgs84坐标系互转
 *
 * @author liuyu
 * @date 2022/4/20
 */
class MvtCoordinateConvertor {
    private static final short TILE_SIZE = 256;

    private static final long[] zoomPow;

    static {
        int n = 30;
        zoomPow = new long[n];
        long s = TILE_SIZE;
        for (int i = 0; i < n; i++) {
            zoomPow[i] = s;
            s = s * 2;
        }
    }

    private final double px;
    private final double py;
    private final long zoomMultiple;// 使用int的话超过22级就溢出了

    /**
     * @param z 瓦片 z
     * @param x 瓦片 x
     * @param y 瓦片 y
     */
    public MvtCoordinateConvertor(byte z, int x, int y) {
        px = x * TILE_SIZE;
        py = y * TILE_SIZE;

        zoomMultiple = zoomPow[z];
    }

    /**
     * wgs84 x 转mvt
     *
     * @param x wgs84 x
     * @return mvt x
     */
    public int wgs84X2mvt(double x) {
        double ppx = (x + 180) / 360 * zoomMultiple;
        return (int) ((ppx - px) * 16 + Math.sin(x) + 0.5);
    }

    /**
     * wgs84 y 转mvt
     *
     * @param y wgs84 y
     * @return mvt y
     */
    public int wgs84Y2mvt(double y) {
        double sinLatitude = Math.sin(y * Math.PI / 180);
        double mp = Math.log((1 + sinLatitude) / (1 - sinLatitude));
        double ppy = (0.5 - mp / (4 * Math.PI)) * zoomMultiple;
        return (int) ((ppy - py) * 16 + Math.cos(y) + 0.5);
    }


    /**
     * mvt x 转 wgs84
     *
     * @param pixelX mvt x
     * @return wgs84
     */
    public double mvtX2wgs84(double pixelX) {
        double ppx = pixelX / 16d + px;
        return ppx / zoomMultiple * 360d - 180d;
    }

    /**
     * mvt y 转 wgs84
     *
     * @param pixelY mvt y
     * @return wgs84
     */
    public double mvtY2wgs84(double pixelY) {
        double ppy = pixelY / 16d + py;
        double mp = (0.5d - ppy / zoomMultiple) * (4d * Math.PI);
        double exp = Math.exp(mp);
        double sinLatitude = (exp - 1d) / (exp + 1d);
        return Math.asin(sinLatitude) * 180d / Math.PI;
    }

}
