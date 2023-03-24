/*
 *
 *  * Copyright (c) 2022- "giscat (https://github.com/codingmiao/giscat)"
 *  *
 *  * 本项目采用自定义版权协议，在不同行业使用时有不同约束，详情参阅：
 *  *
 *  * https://github.com/codingmiao/giscat/blob/main/LICENSE
 *
 */

package org.wowtools.giscat.vector.rocksrtree.conversantmedia;

/**
 * @author liuyu
 * @date 2023/3/24
 */
public class RectNdBuilder implements RectBuilder{
    @Override
    public RectNd getBBox(RectNd t) {
        return t;
    }

    @Override
    public RectNd getMbr(PointNd p1, PointNd p2) {
        return new RectNd(p1, p2);
    }
}
