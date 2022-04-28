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
package org.wowtools.giscat.util.cst;

/**
 * web墨卡托与wgs84坐标互转
 *
 * @author liuyu
 * @date 2019/11/7
 */
public class WebMercator2Wgs84 {

//    public static void main(String[] args) {
//        double lon = 91;
//        double lat = 21;
//
//        double x = lon2WebMercatorX(lon);
//        double y = lat2WebMercator(lat);
//
//        System.out.println(x);
//        System.out.println(y);
//
//        System.out.println(webMercatorX2lon(x));
//        System.out.println(webMercatorY2lat(y));
//
//    }


    private static final double d = 20037508.342789;

    /**
     * wgs84 x 转 web墨卡托 x
     *
     * @param lon wgs84 x
     * @return web墨卡托 x
     */
    public static double lon2WebMercatorX(double lon) {
        double x = lon * d / 180D;
        return x;
    }

    /**
     * wgs84 y 转 web墨卡托 y
     *
     * @param lat wgs84 y
     * @return web墨卡托 y
     */
    public static double lat2WebMercator(double lat) {
        double y = Math.log(Math.tan((90D + lat) * Math.PI / 360D)) / (Math.PI / 180D);
        y = y * d / 180D;
        return y;
    }

    /**
     * web墨卡托 x 转 wgs84 x
     *
     * @param mercatorX web墨卡托 x
     * @return wgs84 x
     */
    public static double webMercatorX2lon(double mercatorX) {
        double x = mercatorX / d * 180D;
        return x;
    }

    /**
     * web墨卡托 y 转 wgs84 y
     *
     * @param mercatorY web墨卡托 y
     * @return wgs84 y
     */
    public static double webMercatorY2lat(double mercatorY) {
        double y = mercatorY / d * 180D;
        y = 180D / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180D)) - Math.PI / 2);
        return y;
    }

}
