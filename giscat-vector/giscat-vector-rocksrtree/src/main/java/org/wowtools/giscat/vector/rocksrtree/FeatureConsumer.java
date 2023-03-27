/*
 *
 *  * Copyright (c) 2022- "giscat (https://github.com/codingmiao/giscat)"
 *  *
 *  * 本项目采用自定义版权协议，在不同行业使用时有不同约束，详情参阅：
 *  *
 *  * https://github.com/codingmiao/giscat/blob/main/LICENSE
 *
 */

package org.wowtools.giscat.vector.rocksrtree;

import org.wowtools.giscat.vector.pojo.Feature;

/**
 * 结果消费者
 * @author liuyu
 * @date 2023/3/24
 */
@FunctionalInterface
public interface FeatureConsumer {

    boolean accept(Feature feature);

    default boolean accept(RectNd rectNd) {
        return accept(rectNd.feature);
    }
}
