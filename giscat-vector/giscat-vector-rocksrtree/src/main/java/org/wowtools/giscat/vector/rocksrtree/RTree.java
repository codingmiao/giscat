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

import java.util.Collection;
import java.util.function.Consumer;

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

    private Node root = null;

    protected RTree(final TreeBuilder builder) {
        this.builder = builder;
    }

    /**
     * Search for entries contained by the given bounding rect
     *
     * @param rect - Bounding rectangle to use for querying
     * @param consumer - callback to receive intersecting objects
     *
     */
    public void search(RectNd rect, Consumer<RectNd> consumer) {
        if (root != null) {
            root.search(rect, consumer);
        }
    }

    /**
     * Search for entries contained by the given bounding rect
     *
     * @param rect - Bounding rectangle to use for querying
     * @param collection - collection to receive results
     *
     */
    public void search(RectNd rect, Collection<RectNd> collection) {
        if (root != null) {
            root.search(rect, t -> collection.add(t));
        }
    }

    /**
     * Search for entries intersecting given bounding rect
     *
     * @param rect - Bounding rectangle to use for querying
     * @param t - Array to store found entries
     *
     * @return Number of results found
     */
    public int intersects(final RectNd rect, final RectNd[] t) {
        if (root != null) {
            return root.intersects(rect, t, 0);
        }
        return 0;
    }

    /**
     * Search for entries intersecting given bounding rect
     *
     * @param rect - Bounding rectangle to use for querying
     * @param consumer - callback to receive intersecting objects
     *
     */
    public void intersects(RectNd rect, Consumer<RectNd> consumer) {
        if (root != null) {
            root.intersects(rect, consumer);
        }
    }

    /**
     * Add the data entry to the SpatialSearch structure
     *
     * @param t Data entry to be added
     */
    public void add(final RectNd t, Transaction tx) {
        if (root != null) {
            root = root.add(t,tx);
        } else {
            root = builder.newLeaf(tx);
            root.add(t,tx);
        }
    }

    /**
     * Remove the data entry from the SpatialSearch structure
     *
     * @param t Data entry to be removed
     */
    public void remove(final RectNd t, Transaction tx) {
        if (root != null) {
            root = root.remove(t,tx);
        }
    }

    /**
     * Update entry in tree
     *
     * @param told - Entry to update
     * @param tnew - Entry to update it to
     */
    public void update(final RectNd told, final RectNd tnew, Transaction tx) {
        if (root != null) {
            root = root.update(told, tnew,tx);
        }
    }

    /**
     * Get the number of entries in the tree
     *
     * @return entry count
     */
    public int getEntryCount() {
        if (root != null) {
            return root.totalSize();
        }
        return 0;
    }

    /**
     * returns whether or not the HyperRect will enclose all of the data entries in t
     *
     * @param t Data entries to be evaluated
     * @return boolean - Whether or not all entries lie inside rect
     */
    public boolean contains(final RectNd t) {
        if (root != null) {
            final RectNd bbox = builder.getBBox(t);
            return root.contains(bbox, t);
        }
        return false;
    }

    public static boolean isEqual(final double a, final double b) {
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
    public void forEach(Consumer<RectNd> consumer) {
        if (root != null) {
            root.forEach(consumer);
        }
    }

    public Stats collectStats() {
        Stats stats = new Stats();
        stats.setMaxFill(builder.mMax);
        stats.setMinFill(builder.mMin);
        root.collectStats(stats, 0);
        return stats;
    }

    Node getRoot() {
        return root;
    }

}
