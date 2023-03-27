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

/*
 * #%L
 * Conversant RTree
 * ~~
 * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
 * ~~
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.rocksdb.Transaction;
import org.wowtools.giscat.vector.pojo.Feature;

import java.util.function.Consumer;

import static org.wowtools.giscat.vector.rocksrtree.TreeBuilder.emptyId;

/**
 * <p>Data structure to make range searching more efficient. Indexes multi-dimensional information
 * such as geographical coordinates or rectangles. Groups information and represents them with a
 * minimum bounding rectangle (mbr). When searching through the tree, any query that does not
 * intersect an mbr can ignore any data entries in that mbr.</p>
 * <p>More information can be found here @see <a href="https://en.wikipedia.org/wiki/R-tree">https://en.wikipedia.org/wiki/R-tree</a></p>
 * <p>
 * Created by jcairns on 4/30/15.</p>
 */
public final class RTree {
    private static final double EPSILON = 1e-12;

    private final TreeBuilder builder;

    private long root = emptyId;

    public RTree(final TreeBuilder builder) {
        this.builder = builder;
    }

    /**
     * 查询被输入的范围完全覆盖的要素范围
     *
     * @param rect     输入范围
     * @param consumer 查询结果消费者，若accept返回false，则终止查询过程
     */
    public void contains(RectNd rect, FeatureConsumer consumer) {
        if (root != emptyId) {
            builder.getNode(root).contains(rect, consumer);
        }
    }


    /**
     * 查询与输入范围相交的要素范围
     *
     * @param rect     输入范围
     * @param consumer 查询结果消费者，若accept返回false，则终止查询过程
     */
    public void intersects(RectNd rect, FeatureConsumer consumer) {
        if (root != emptyId) {
            builder.getNode(root).intersects(rect, consumer);
        }
    }


    /**
     * 添加一个feature
     *
     * @param feature feature
     * @param tx      事务
     */
    public void add(Feature feature, Transaction tx) {
        RectNd t = builder.buildFeatureRect(feature);
        add(t, tx);
        String key = builder.getFeatureKey(feature);
        builder.putFeatureKeyInLeafId(key, t.leafId);
    }


    /**
     * Add the data entry to the SpatialSearch structure
     *
     * @param t Data entry to be added
     */
    protected void add(final RectNd t, Transaction tx) {
        if (root != emptyId) {
            root = builder.getNode(root).add(t, tx).id;
        } else {
            root = builder.newLeaf(tx).id;
            builder.getNode(root).add(t, tx);
        }
    }

    // TODO 删除和修改由于equals不易判断，暂不开放 后续通过featurekey拿到RectNd再  featureEquals来比较删除和修改

//    /**
//     * 添加一个feature
//     * @param feature feature
//     * @param tx 事务
//     */
//    public void remove(Feature feature,Transaction tx) {
//        RectNd t = builder.getFeatureRect(feature);
//        remove(t,tx);
//    }
//
//    /**
//     * Remove the data entry from the SpatialSearch structure
//     *
//     * @param t Data entry to be removed
//     */
//    protected void remove(final RectNd t, Transaction tx) {
//        if (root != null) {
//            root = root.remove(t, tx);
//        }
//    }
//
//    /**
//     * Update entry in tree
//     *
//     * @param told - Entry to update
//     * @param tnew - Entry to update it to
//     */
//    protected void update(final RectNd told, final RectNd tnew, Transaction tx) {
//        if (root != null) {
//            root = root.update(told, tnew, tx);
//        }
//    }

    /**
     * Get the number of entries in the tree
     *
     * @return entry count
     */
    public int getEntryCount() {
        if (root != emptyId) {
            return  builder.getNode(root).totalSize();
        }
        return 0;
    }

    protected static boolean isEqual(final double a, final double b) {
        return isEqual(a, b, EPSILON);
    }

    static boolean isEqual(final double a, final double b, final double eps) {
        return Math.abs(a - b) <= ((Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math.abs(a)) * eps);
    }

    /**
     * Iterate over all entries in the tree
     *
     * @param consumer - callback for each element
     */
    protected void forEach(Consumer<RectNd> consumer) {
        if (root != emptyId) {
            builder.getNode(root).forEach(consumer);
        }
    }

    protected Stats collectStats() {
        Stats stats = new Stats();
        stats.setMaxFill(builder.mMax);
        stats.setMinFill(builder.mMin);
        builder.getNode(root).collectStats(stats, 0);
        return stats;
    }

    Node getRoot() {
        return  builder.getNode(root);
    }

}
