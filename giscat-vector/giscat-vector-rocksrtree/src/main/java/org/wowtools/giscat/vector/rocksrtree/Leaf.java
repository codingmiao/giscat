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

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

import static org.wowtools.giscat.vector.rocksrtree.TreeBuilder.emptyId;

/**
 * Node that will contain the data entries. Implemented by different type of SplitType leaf classes.
 * <p>
 * Created by jcairns on 4/30/15.
 */
final class Leaf extends Node {
    protected final TreeBuilder builder;


    protected final RectNd[] entryRects;

    protected final RectNd[] entry;

    protected RectNd mbr;

    protected int size;

    protected Leaf(final TreeBuilder builder, long id) {
        super(id);
        this.builder = builder;
        this.entryRects = new RectNd[builder.mMax];
        this.entry = new RectNd[builder.mMax];
    }

//    protected byte[] toBytes() {
//
//    }

    @Override
    Node add(final RectNd t, Transaction tx) {
        if (size < builder.mMax) {
            final RectNd tRect = builder.getBBox(t);
            if (mbr != null) {
                mbr = mbr.getMbr(tRect);
            } else {
                mbr = tRect;
            }

            entryRects[size] = tRect;
            entry[size] = t;
            t.leafId = this.id;
            size++;
        } else {
            return split(t, tx);
        }

        return this;
    }

    @Override
    Node remove(final RectNd t, Transaction tx) {

        int i = 0;
        int j;

        while (i < size && (entry[i] != t) && (!entry[i].featureEquals(t,builder))) {
            i++;
        }

        j = i;

        while (j < size && ((entry[j] == t) || entry[j].featureEquals(t,builder))) {
            j++;
        }

        if (i < j) {
            final int nRemoved = j - i;
            if (j < size) {
                final int nRemaining = size - j;
                System.arraycopy(entryRects, j, entryRects, i, nRemaining);
                System.arraycopy(entry, j, entry, i, nRemaining);
                for (int k = size - nRemoved; k < size; k++) {
                    entryRects[k] = null;
                    entry[k].leafId = emptyId;
                    entry[k] = null;
                }
            } else {
                if (i == 0) {
                    // clean sweep
                    return null;
                }
                for (int k = i; k < size; k++) {
                    entryRects[k] = null;
                    entry[k].leafId = emptyId;
                    entry[k] = null;
                }
            }

            size -= nRemoved;

            for (int k = 0; k < size; k++) {
                if (k == 0) {
                    mbr = entryRects[k];
                } else {
                    mbr = mbr.getMbr(entryRects[k]);
                }
            }

        }

        return this;

    }

    @Override
    Node update(final RectNd told, final RectNd tnew, Transaction tx) {
        final RectNd bbox = builder.getBBox(tnew);

        for (int i = 0; i < size; i++) {
            if (entry[i].featureEquals(told,builder)) {
                entryRects[i] = bbox;
                tnew.leafId = this.id;
                entry[i].leafId = emptyId;
                entry[i] = tnew;
            }

            if (i == 0) {
                mbr = entryRects[i];
            } else {
                mbr = mbr.getMbr(entryRects[i]);
            }
        }

        return this;
    }


    @Override
    public boolean intersects(RectNd rect, FeatureConsumer consumer) {
        for (int i = 0; i < size; i++) {
            if (rect.intersects(entryRects[i])) {
                if (!consumer.accept(entry[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean contains(RectNd rect, FeatureConsumer consumer) {
        for (int i = 0; i < size; i++) {
            if (rect.contains(entryRects[i])) {
                if (!consumer.accept(entry[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int totalSize() {
        return size;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public RectNd getBound() {
        return mbr;
    }

//    static  Node create(final RectBuilder builder, final int mMin, final int M) {
//        return new Leaf(builder, mMin, M);
//    }

    /**
     * Splits a leaf node that has the maximum number of entries into 2 leaf nodes of the same type with half
     * of the entries in each one.
     *
     * @param t entry to be added to the full leaf node
     * @return newly created node storing half the entries of this node
     */
    protected Node split(final RectNd t, Transaction tx) {
        final Branch pNode = builder.newBranch(tx);
        final Node l1Node = builder.newLeaf(tx);
        final Node l2Node = builder.newLeaf(tx);
        final int nD = entryRects[0].getNDim();

        // choose axis to split
        int axis = 0;
        double rangeD = mbr.getRange(0);
        for (int d = 1; d < nD; d++) {
            // split along the greatest range extent
            final double dr = mbr.getRange(d);
            if (dr > rangeD) {
                axis = d;
                rangeD = dr;
            }
        }

        final int splitDimension = axis;

        // sort along split dimension
        final RectNd[] sortedMbr = Arrays.copyOf(entryRects, entryRects.length);

        Arrays.sort(sortedMbr, new Comparator<RectNd>() {
            @Override
            public int compare(final RectNd o1, final RectNd o2) {
                final PointNd p1 = o1.getCentroid();
                final PointNd p2 = o2.getCentroid();

                return Double.compare(p1.getCoord(splitDimension), p2.getCoord(splitDimension));
            }
        });

        // divide sorted leafs
        for (int i = 0; i < size / 2; i++) {
            outerLoop:
            for (int j = 0; j < size; j++) {
                if (entryRects[j] == sortedMbr[i]) {
                    l1Node.add(entry[j], tx);
                    break outerLoop;
                }
            }
        }

        for (int i = size / 2; i < size; i++) {
            outerLoop:
            for (int j = 0; j < size; j++) {
                if (entryRects[j] == sortedMbr[i]) {
                    l2Node.add(entry[j], tx);
                    break outerLoop;
                }
            }
        }

        classify(l1Node, l2Node, t, tx);

        pNode.addChild(l1Node);
        pNode.addChild(l2Node);

        return pNode;
    }

    @Override
    public void forEach(Consumer<RectNd> consumer) {
        for (int i = 0; i < size; i++) {
            consumer.accept(entry[i]);
        }
    }

    @Override
    public boolean contains(RectNd rect, RectNd t) {
        for (int i = 0; i < size; i++) {
            if (rect.contains(entryRects[i])) {
                if (entry[i].equals(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void collectStats(Stats stats, int depth) {
        if (depth > stats.getMaxDepth()) {
            stats.setMaxDepth(depth);
        }
        stats.countLeafAtDepth(depth);
        stats.countEntriesAtDepth(size, depth);
    }

    /**
     * Figures out which newly made leaf node (see split method) to add a data entry to.
     *
     * @param l1Node left node
     * @param l2Node right node
     * @param t      data entry to be added
     */
    protected final void classify(final Node l1Node, final Node l2Node, final RectNd t, Transaction tx) {
        final RectNd tRect = builder.getBBox(t);
        final RectNd l1Mbr = l1Node.getBound().getMbr(tRect);
        final RectNd l2Mbr = l2Node.getBound().getMbr(tRect);
        final double l1CostInc = Math.max(l1Mbr.cost() - (l1Node.getBound().cost() + tRect.cost()), 0.0);
        final double l2CostInc = Math.max(l2Mbr.cost() - (l2Node.getBound().cost() + tRect.cost()), 0.0);
        if (l2CostInc > l1CostInc) {
            l1Node.add(t, tx);
        } else if (RTree.isEqual(l1CostInc, l2CostInc)) {
            final double l1MbrCost = l1Mbr.cost();
            final double l2MbrCost = l2Mbr.cost();
            if (l1MbrCost < l2MbrCost) {
                l1Node.add(t, tx);
            } else if (RTree.isEqual(l1MbrCost, l2MbrCost)) {
                final double l1MbrMargin = l1Mbr.perimeter();
                final double l2MbrMargin = l2Mbr.perimeter();
                if (l1MbrMargin < l2MbrMargin) {
                    l1Node.add(t, tx);
                } else if (RTree.isEqual(l1MbrMargin, l2MbrMargin)) {
                    // break ties with least number
                    if (l1Node.size() < l2Node.size()) {
                        l1Node.add(t, tx);
                    } else {
                        l2Node.add(t, tx);
                    }
                } else {
                    l2Node.add(t, tx);
                }
            } else {
                l2Node.add(t, tx);
            }
        } else {
            l2Node.add(t, tx);
        }

    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append('[');
        sb.append(mbr);
        sb.append(']');

        return sb.toString();
    }

}
