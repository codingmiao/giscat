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
abstract class Node {

    protected final long id;

    public Node(long id) {
        this.id = id;
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
    public abstract Node add(RectNd t);

    /**
     * Remove t from the index
     *
     * @param t - value to remove from index
     */
    public abstract Node remove(RectNd t);

    /**
     * update an existing t in the index
     *
     * @param told - old index to be updated
     * @param tnew - value to update old index to
     */
    public abstract Node update(RectNd told, RectNd tnew);

    /**
     * Search for rect within this node
     *
     * @param rect - HyperRect to search for
     * @param t    - array of found results
     * @param n    - total result count so far (from recursive call)
     * @return result count from search of this node
     */
    public abstract int search(RectNd rect, RectNd[] t, int n);

    /**
     * Visitor pattern:
     * <p>
     * Consumer "accepts" every node contained by the given rect
     *
     * @param rect     - limiting rect
     * @param consumer
     */
    public abstract void search(RectNd rect, Consumer<RectNd> consumer);

    /**
     * intersect rect with this node
     *
     * @param rect - HyperRect to search for
     * @param t    - array of found results
     * @param n    - total result count so far (from recursive call)
     * @return result count from search of this node
     */
    public abstract int intersects(RectNd rect, RectNd[] t, int n);

    /**
     * Visitor pattern:
     * <p>
     * Consumer "accepts" every node intersecting the given rect
     *
     * @param rect     - limiting rect
     * @param consumer
     */
    public abstract void intersects(RectNd rect, Consumer<RectNd> consumer);


    /**
     * @param rect
     * @param t
     * @return boolean true if subtree contains t
     */
    public abstract boolean contains(RectNd rect, RectNd t);

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
    public abstract int totalSize();

    /**
     * Consumer "accepts" every node in the entire index
     *
     * @param consumer
     */
    public abstract void forEach(Consumer<RectNd> consumer);

    /**
     * Recurses over index collecting stats
     *
     * @param stats - Stats object being populated
     * @param depth - current depth in tree
     */
    public abstract void collectStats(Stats stats, int depth);


}
