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

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Node that will contain the data entries. Implemented by different type of SplitType leaf classes.
 *
 * Created by jcairns on 4/30/15.
 */
 final class Leaf implements Node {

    protected final int mMax;       // max entries per node

    protected final int mMin;       // least number of entries per node


    protected final RectNd[] r;

    protected final RectNd[] entry;

    protected final RectBuilder builder;

    protected RectNd mbr;

    protected int size;

    protected Leaf(final RectBuilder builder, final int mMin, final int mMax) {
        this.mMin = mMin;
        this.mMax = mMax;
        this.mbr = null;
        this.builder = builder;
        this.r  = new RectNd[mMax];
        this.entry = new RectNd[mMax];
        this.size = 0;
    }

    @Override
    public Node add(final RectNd t) {
        if(size < mMax) {
            final RectNd tRect = builder.getBBox(t);
            if(mbr != null) {
                mbr = mbr.getMbr(tRect);
            } else {
                mbr = tRect;
            }

            r[size] = tRect;
            entry[size++] = t;
        } else {
            return split(t);
        }

        return this;
    }

    @Override
    public Node remove(final RectNd t)  {

        int i=0;
        int j;

        while(i<size && (entry[i]!=t) && (!entry[i].equals(t))) {
            i++;
        }

        j=i;

        while(j<size && ((entry[j]==t) || entry[j].equals(t))) {
            j++;
        }

        if(i < j) {
            final int nRemoved = j-i;
            if (j < size) {
                final int nRemaining = size-j;
                System.arraycopy(r, j, r, i, nRemaining);
                System.arraycopy(entry, j, entry, i, nRemaining);
                for (int k=size-nRemoved; k < size; k++) {
                    r[k] = null;
                    entry[k] = null;
                }
            } else {
                if(i==0) {
                    // clean sweep
                    return null;
                }
                for (int k=i; k < size; k++) {
                    r[k] = null;
                    entry[k] = null;
                }
            }

            size -= nRemoved;

            for(int k=0; k<size; k++) {
                if(k==0) {
                    mbr = r[k];
                } else {
                    mbr = mbr.getMbr(r[k]);
                }
            }

        }

        return this;

    }

    @Override
    public Node update(final RectNd told, final RectNd tnew) {
        final RectNd bbox = builder.getBBox(tnew);

        for(int i=0; i<size; i++) {
            if (entry[i].equals(told)) {
                r[i] = bbox;
                entry[i] = tnew;
            }

            if (i == 0) {
                mbr = r[i];
            } else {
                mbr = mbr.getMbr(r[i]);
            }
        }

        return this;
    }

    @Override
    public int search(final RectNd rect, final RectNd[] t, int n) {
        final int tLen = t.length;
        final int n0 = n;

        for(int i=0; i<size && n<tLen; i++) {
            if(rect.contains(r[i])) {
                t[n++] = entry[i];
            }
        }
        return n - n0;
    }

    @Override
    public void search(RectNd rect, Consumer<RectNd> consumer) {
        for(int i = 0; i < size; i++) {
            if(rect.contains(r[i])) {
                consumer.accept(entry[i]);
            }
        }
    }

    @Override
    public int intersects(final RectNd rect, final RectNd[] t, int n) {
        final int tLen = t.length;
        final int n0 = n;

        for(int i=0; i<size && n<tLen; i++) {
            if(rect.intersects(r[i])) {
                t[n++] = entry[i];
            }
        }
        return n - n0;
    }

    @Override
    public void intersects(RectNd rect, Consumer<RectNd> consumer) {
        for(int i = 0; i < size; i++) {
            if(rect.intersects(r[i])) {
                consumer.accept(entry[i]);
            }
        }
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
    protected Node split(final RectNd t) {
        final Branch pNode = new Branch(builder, mMin, mMax);
        final Node l1Node = new Leaf(builder, mMin, mMax);
        final Node l2Node = new Leaf(builder, mMin, mMax);
        final int nD = r[0].getNDim();

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
        final RectNd[] sortedMbr = Arrays.copyOf(r, r.length);

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
                if (r[j] == sortedMbr[i]) {
                    l1Node.add(entry[j]);
                    break outerLoop;
                }
            }
        }

        for (int i = size / 2; i < size; i++) {
            outerLoop:
            for (int j = 0; j < size; j++) {
                if (r[j] == sortedMbr[i]) {
                    l2Node.add(entry[j]);
                    break outerLoop;
                }
            }
        }

        classify(l1Node, l2Node, t);

        pNode.addChild(l1Node);
        pNode.addChild(l2Node);

        return pNode;
    }

    @Override
    public void forEach(Consumer<RectNd> consumer) {
        for(int i = 0; i < size; i++) {
            consumer.accept(entry[i]);
        }
    }

    @Override
    public boolean contains(RectNd rect, RectNd t) {
        for(int i = 0; i < size; i++) {
            if(rect.contains(r[i])) {
                if(entry[i].equals(t)) {
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
     * @param t data entry to be added
     */
    protected final void classify(final Node l1Node, final Node l2Node, final RectNd t) {
        final RectNd tRect = builder.getBBox(t);
        final RectNd l1Mbr = l1Node.getBound().getMbr(tRect);
        final RectNd l2Mbr = l2Node.getBound().getMbr(tRect);
        final double l1CostInc = Math.max(l1Mbr.cost() - (l1Node.getBound().cost() + tRect.cost()), 0.0);
        final double l2CostInc = Math.max(l2Mbr.cost() - (l2Node.getBound().cost() + tRect.cost()), 0.0);
        if(l2CostInc > l1CostInc) {
            l1Node.add(t);
        } else if(RTree.isEqual(l1CostInc, l2CostInc)) {
            final double l1MbrCost = l1Mbr.cost();
            final double l2MbrCost = l2Mbr.cost();
            if(l1MbrCost < l2MbrCost) {
                l1Node.add(t);
            } else if(RTree.isEqual(l1MbrCost, l2MbrCost)) {
                final double l1MbrMargin = l1Mbr.perimeter();
                final double l2MbrMargin = l2Mbr.perimeter();
                if(l1MbrMargin < l2MbrMargin) {
                    l1Node.add(t);
                } else if(RTree.isEqual(l1MbrMargin, l2MbrMargin)) {
                    // break ties with least number
                    if (l1Node.size() < l2Node.size()) {
                        l1Node.add(t);
                    } else {
                        l2Node.add(t);
                    }
                } else {
                    l2Node.add(t);
                }
            } else {
                l2Node.add(t);
            }
        }
        else {
            l2Node.add(t);
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