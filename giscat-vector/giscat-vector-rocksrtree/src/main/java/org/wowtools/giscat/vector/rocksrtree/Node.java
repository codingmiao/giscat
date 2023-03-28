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


import java.util.function.Consumer;

/**
 * Created by jcairns on 4/30/15.
 */
abstract class Node extends ProtoAble{

    public Node(TreeBuilder builder, String id) {
        super(builder, id);
    }

    /**
     * @return boolean - true if this node is a leaf
     */
    abstract boolean isLeaf();

    /**
     * @return Rect - the bounding rectangle for this node
     */
    public abstract RectNd getBound();

    /**
     * Add t to the index
     *
     * @param t - value to add to index
     */
    abstract Node add(RectNd t, TreeTransaction tx);

    /**
     * Remove t from the index
     *
     * @param t - value to remove from index
     */
    abstract Node remove(RectNd t, TreeTransaction tx);

    /**
     * update an existing t in the index
     *
     * @param told - old index to be updated
     * @param tnew - value to update old index to
     */
    abstract Node update(RectNd told, RectNd tnew, TreeTransaction tx);


    /**
     * Visitor pattern:
     * <p>
     * Consumer "accepts" every node intersecting the given rect, if consumer return false, break it.
     *
     * @param rect     - limiting rect
     * @param consumer consumer
     */
    public abstract boolean intersects(RectNd rect, FeatureConsumer consumer, TreeTransaction tx);

    /**
     * Visitor pattern:
     * <p>
     * Consumer "accepts" every node contained by the given rect
     *
     * @param rect     - limiting rect
     * @param consumer
     */
    public abstract boolean contains(RectNd rect, FeatureConsumer consumer, TreeTransaction tx);

    /**
     * @param rect
     * @param t
     * @return boolean true if subtree contains t
     */
    protected abstract boolean contains(RectNd rect, RectNd t, TreeTransaction tx);

    /**
     * The number of entries in the node
     *
     * @return int - entry count
     */
    public abstract int size();

    /**
     * The number of entries in the subtree
     *
     * @return int - entry count
     */
    public abstract int totalSize(TreeTransaction tx);

    /**
     * Consumer "accepts" every node in the entire index
     *
     * @param consumer
     */
    public abstract void forEach(Consumer<RectNd> consumer, TreeTransaction tx);

    /**
     * Recurses over index collecting stats
     *
     * @param stats - Stats object being populated
     * @param depth - current depth in tree
     */
    public abstract void collectStats(Stats stats, int depth, TreeTransaction tx);


}
