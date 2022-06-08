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

import lombok.Getter;
import lombok.Setter;


/**
 * utm坐标与wgs84互转，代码从stackoverflow抄过来的，很多魔术数字未做处理
 * <p>
 * <a href="https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm">...</a>
 *
 * @author liuyu
 * @date 2022/6/7
 */
public class Utm2Wgs84 {
    /**
     * utm坐标
     */
    @Setter
    @Getter
    public static final class UtmCoord {

        /**
         * utm东经
         */
        private double easting;
        /**
         * utm北纬
         */
        private double northing;
        /**
         * utm经度带号 1-60
         */
        private byte zone;
        /**
         * utm纬度带号  C 到 X 标识（其中没有字母 I 和 O）
         */
        private char letter;

        public UtmCoord() {

        }

        public UtmCoord(byte zone, char letter, double easting, double northing) {
            this.easting = easting;
            this.northing = northing;
            this.zone = zone;
            this.letter = letter;
        }

        public UtmCoord(String utmStr) {
            String[] parts = utmStr.split(" ");
            if (parts.length == 4) {
                zone = Byte.parseByte(parts[0]);
                letter = parts[1].charAt(0);
                if (letter >= 'a') {
                    letter -= 32;
                }
                easting = Double.parseDouble(parts[2]);
                northing = Double.parseDouble(parts[3]);
            } else if (parts.length == 3) {
                String area = parts[0];
                int n = area.length() - 1;
                zone = Byte.parseByte(area.substring(0, n));
                letter = area.charAt(n);
                if (letter >= 'a') {
                    letter -= 32;
                }
                easting = Double.parseDouble(parts[1]);
                northing = Double.parseDouble(parts[2]);
            } else {
                throw new RuntimeException("utmStr格式不合法 " + utmStr);
            }

        }

    }


    //100000 弧度 = 6366197.72xxxxx 梯度
    private static final double r2d = 6366197.723675814;
    //UTM中央经线的长度比 https://www.zhihu.com/question/22231208
    private static final double cll = 0.9996;
    //wgs84椭球参数
    private static final double e21 = 0.006739496742;

    //曲率极半径  https://patents.google.com/patent/US6195609B1/en
    private static final double prc = 6399593.625;

    //http://docs.ros.org/en/diamondback/api/art_common/html/UTM_8h_source.html
    private static final double ep = 0.0820944379;

    /**
     * utm坐标转wgs84坐标
     *
     * @param utmStr utm坐标字符串，形如 "18 G 615471.66 4789269.78" "18 g 615471.66 4789269.78" "18G 615471.66 4789269.78" "18g 615471.66 4789269.78"
     * @return wgs84坐标
     */
    public static LonLat utm2wgs84(String utmStr) {
        UtmCoord utmCoord = new UtmCoord(utmStr);
        return utm2wgs84(utmCoord);
    }

    /**
     * utm坐标转wgs84坐标
     *
     * @param utmCoord utm坐标
     * @return wgs84坐标
     */
    public static LonLat utm2wgs84(UtmCoord utmCoord) {
        byte zone = utmCoord.zone;
        char letter = utmCoord.letter;
        double Easting = utmCoord.easting;
        double Northing = utmCoord.northing;
        double north;
        if (letter <= 'M') {
            north = Northing - 10000000;
        } else {
            north = Northing;
        }
        double d7 = Math.pow(Math.cos(north / r2d / cll), 2);
        double d1 = 1 + e21 * d7;
        double d9 = cll * prc / Math.sqrt((1 + e21 * d7));
        double d5 = (Easting - 500000) / d9;
        double d11 = e21 * Math.pow(d5, 2) / 2 * d7;
        double d10 = d11 / 3;
        double d8 = 1 - d10;
        double d2 = d5 * d8;
        double d4 = -(Easting - 500000) / d9 * (1 - d10);
        double d12 = north / r2d / cll + Math.sin(2 * north / r2d / cll) / 2;
        double d15 = 3 * d12 + Math.sin(2 * north / r2d / cll) * d7;
        double d6 = (north - cll * prc * (north / r2d / cll - e21 * 3 / 4 * d12 + Math.pow(e21 * 3 / 4, 2) * 5 / 3 * d15 / 4 - Math.pow(e21 * 3 / 4, 3) * 35 / 27 * (5 * d15 / 4 + Math.sin(2 * north / r2d / cll) * d7 * d7) / 3)) / d9 * (1 - d11) + north / r2d / cll;
        double d3 = (Math.exp(d2) - Math.exp(d4)) / 2 / Math.cos(d6);
        double latitude = (north / r2d / cll + (d1 - e21 * Math.sin(north / r2d / cll) * Math.cos(north / r2d / cll) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (cll * prc / Math.sqrt(d1)) * (1 - d10)) - Math.exp(d4)) / 2 / Math.cos(d6))) * Math.tan(d6)) - north / r2d / cll) * 3 / 2) * (Math.atan(Math.cos(Math.atan(d3)) * Math.tan(d6)) - north / r2d / cll)) * 180 / Math.PI;
        latitude = Math.round(latitude * 10000000);
        latitude = latitude / 10000000;
        double longitude = Math.atan(d3) * 180 / Math.PI + zone * 6 - 183;
        longitude = Math.round(longitude * 10000000);
        longitude = longitude / 10000000;
        return new LonLat(longitude, latitude);
    }

    public static UtmCoord wgs842utm(double lon, double lat) {
        byte Zone = (byte) Math.floor(lon / 6 + 31);
        char letter;
        if (lat < -72)
            letter = 'C';
        else if (lat < -64)
            letter = 'D';
        else if (lat < -56)
            letter = 'E';
        else if (lat < -48)
            letter = 'F';
        else if (lat < -40)
            letter = 'G';
        else if (lat < -32)
            letter = 'H';
        else if (lat < -24)
            letter = 'J';
        else if (lat < -16)
            letter = 'K';
        else if (lat < -8)
            letter = 'L';
        else if (lat < 0)
            letter = 'M';
        else if (lat < 8)
            letter = 'N';
        else if (lat < 16)
            letter = 'P';
        else if (lat < 24)
            letter = 'Q';
        else if (lat < 32)
            letter = 'R';
        else if (lat < 40)
            letter = 'S';
        else if (lat < 48)
            letter = 'T';
        else if (lat < 56)
            letter = 'U';
        else if (lat < 64)
            letter = 'V';
        else if (lat < 72)
            letter = 'W';
        else
            letter = 'X';
        double d3 = lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180;
        double d2 = Math.cos(lat * Math.PI / 180) * Math.sin(d3);
        double d1 = (1 + d2) / (1 - d2);
        double d4 = Math.pow(Math.cos(lat * Math.PI / 180), 2);
        double d5 = Math.pow((0.5 * Math.log(d1)), 2);
        double easting = 0.5 * Math.log(d1) * cll * prc / Math.pow((1 + Math.pow(ep, 2) * d4), 0.5) * (1 + Math.pow(ep, 2) / 2 * d5 * d4 / 3) + 500000;
        easting = Math.round(easting * 100) * 0.01;
        double d8 = lat * Math.PI / 180 + Math.sin(2 * lat * Math.PI / 180) / 2;
        double d7 = 3 * d8;
        double d9 = Math.sin(2 * lat * Math.PI / 180) * d4;
        double d6 = d7 + d9;
        double northing = (Math.atan(Math.tan(lat * Math.PI / 180) / Math.cos(d3)) - lat * Math.PI / 180) * cll * prc / Math.sqrt(1 + e21 * d4) * (1 + e21 / 2 * d5 * d4) + cll * prc * (lat * Math.PI / 180 - 0.005054622556 * d8 + 4.258201531e-05 * d6 / 4 - 1.674057895e-07 * (5 * (d7 + d9) / 4 + Math.sin(2 * lat * Math.PI / 180) * d4 * d4) / 3);
        if (letter <= 'M')
            northing = northing + 10000000;
        northing = Math.round(northing * 100) * 0.01;
        return new UtmCoord(Zone, letter, easting, northing);
    }
}
