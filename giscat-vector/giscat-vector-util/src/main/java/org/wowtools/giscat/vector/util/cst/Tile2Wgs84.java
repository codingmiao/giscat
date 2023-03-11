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
package org.wowtools.giscat.vector.util.cst;

import org.apache.commons.math.util.FastMath;
import org.jetbrains.annotations.NotNull;

/**
 * wgs84经纬度坐标与瓦片index互转
 *
 * @author liuyu
 * @date 2020/1/13
 */
public class Tile2Wgs84 {

    private static final int @NotNull [] pow2;//2的n次方

    static {
        int n = 30;
        pow2 = new int[n];
        int s = 1;
        for (int i = 0; i < n; i++) {
            pow2[i] = s;
            s = s * 2;
        }
    }

    /**
     * 瓦片转换纬度(左上角)
     *
     * @param y 瓦片y
     * @param z 瓦片z
     * @return 纬度
     */
    public static double tileY2lat(int y, byte z) {
        double n = Math.PI - (2.0 * Math.PI * y) / pow2[z];
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    /**
     * 瓦片转换经度(左上角)
     *
     * @param x 瓦片x
     * @param z 瓦片z
     * @return 经度
     */
    public static double tileX2lon(int x, byte z) {
        return x * 360d / pow2[z] - 180;
    }

    /**
     * 经度转瓦片x
     *
     * @param lon 经度
     * @param z   瓦片z
     * @return 瓦片x，整数部分表示列号，小数部分表示从左边开始到处于瓦片位置的百分比
     */
    public static double lon2tileX(double lon, byte z) {
        return ((lon + 180.0) / 360.0 * pow2[z]);
    }

    /**
     * 经度转瓦片y
     *
     * @param lat 经度
     * @param z   瓦片z
     * @return 瓦片y，整数部分表示行，小数部分表示从上边开始到处于瓦片位置的百分比
     */
    public static double lat2tileY(double lat, byte z) {
        return (Math.PI - FastMath.asinh(Math.tan(lat * Math.PI / 180.0))) * pow2[z] / (2 * Math.PI);
    }


}
