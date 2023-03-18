//package org.wowtools.giscat.vector.rocksrtree.internal;
//
///*
// * #%L
// * Conversant RTree
// * ~~
// * Conversantmedia.com © 2016, Conversant, Inc. Conversant® is a trademark of Conversant, Inc.
// * ~~
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * #L%
// */
//
//
//import java.util.function.Consumer;
//
///**
// * Node that will contain the data entries. Implemented by different type of SplitType leaf classes.
// * <p>
// * Created by jcairns on 4/30/15.
// */
//abstract class NodeOfLeaf implements Node {
//
//    protected final TxCell txCell;
//
//    protected final int mMax;       // max entries per node
//
//    protected final int mMin;       // least number of entries per node
//
//    protected final RectBuilder builder;
//
//    protected final CacheNode cacheNode;
//
//    protected NodeOfLeaf(final RectBuilder builder, org.neo4j.graphdb.Node node, TxCell txCell) {
//        this.txCell = txCell;
//        this.cacheNode = txCell.getNode(node.getId());
//
//        this.mMin = txCell.getmMin();
//        this.mMax = txCell.getmMax();
//        this.builder = builder;
//    }
//
//
//    protected NodeOfLeaf(final RectBuilder builder, final int mMin, final int mMax, TxCell txCell) {
//        this.txCell = txCell;
//
//
//        cacheNode = txCell.newNode(Labels.RTREE_LEAF);
//        cacheNode.setSize(0);
//
//        this.mMin = mMin;
//        this.mMax = mMax;
//        this.builder = builder;
//    }
//
//    @Override
//    public Node add(final RectNd t) {
//        int size = cacheNode.getSize();
//        if (size < mMax) {
//            RectNd mbr = cacheNode.getMbr();
//            final RectNd tRect = builder.getBBox(t);
//            if (mbr != null) {
//                mbr = mbr.getMbr(tRect);
//            } else {
//                mbr = tRect;
//            }
//            cacheNode.setMbr((RectNd) mbr);
//            cacheNode.setEntryAtI(size, t);
//            size = size + 1;
//            cacheNode.setSize(size);
//        } else {
//            return split(t);
//        }
//
//        return this;
//    }
//
//    @Override
//    public Node remove(final RectNd t) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//        int i = 0;
//        int j;
//
//        while (i < size && (entry[i] != t) && (!entry[i].equals(t))) {
//            i++;
//        }
//
//        j = i;
//
//        while (j < size && ((entry[j] == t) || entry[j].equals(t))) {
//            j++;
//        }
//
//        if (i < j) {
//            final int nRemoved = j - i;
//            if (j < size) {
//                final int nRemaining = size - j;
//                for (int i1 = 0; i1 < nRemaining; i1++) {
//                    cacheNode.setEntryAtI(i + i1, entry[j + i1]);
//                }
//
//                for (int k = size - nRemoved; k < size; k++) {
//                    cacheNode.setEntryAtI(k, null);
//                }
//            } else {
//                if (i == 0) {
//                    // clean sweep
//                    return null;
//                }
//                for (int k = i; k < size; k++) {
//                    cacheNode.setEntryAtI(k, null);
//                }
//            }
//
//            size -= nRemoved;
//            cacheNode.setSize(size);
//
//            RectNd mbr = null;
//            for (int k = 0; k < size; k++) {
//                if (k == 0) {
//                    mbr = entry[k];
//                } else {
//                    mbr = mbr.getMbr(entry[k]);
//                }
//            }
//            if (null != mbr) {
//                cacheNode.setMbr(mbr);
//            }
//
//        }
//
//        return this;
//
//    }
//
//    @Override
//    public Node update(final RectNd told, final RectNd tnew) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//
//        RectNd mbr = null;
//        for (int i = 0; i < size; i++) {
//            if (entry[i].equals(told)) {
//                cacheNode.setEntryAtI(i, tnew);
//            }
//            if (i == 0) {
//                mbr = entry[i];
//            } else {
//                mbr = mbr.getMbr(entry[i]);
//            }
//        }
//        if (null != mbr) {
//            cacheNode.setMbr((RectNd) mbr);
//        }
//
//        return this;
//    }
//
//    @Override
//    public int search(final RectNd rect, final RectNd[] t, int n) {
//        final int tLen = t.length;
//        final int n0 = n;
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//
//        for (int i = 0; i < size && n < tLen; i++) {
//            if (rect.contains(entry[i])) {
//                t[n++] = entry[i];
//            }
//        }
//        return n - n0;
//    }
//
//    @Override
//    public void search(RectNd rect, Consumer consumer) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//        for (int i = 0; i < size; i++) {
//            if (rect.contains(entry[i])) {
//                consumer.accept(entry[i]);
//            }
//        }
//    }
//
//    @Override
//    public int intersects(final RectNd rect, final RectNd[] t, int n) {
//        final int tLen = t.length;
//        final int n0 = n;
//
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//
//        for (int i = 0; i < size && n < tLen; i++) {
//            if (rect.intersects(entry[i])) {
//                t[n++] = entry[i];
//            }
//        }
//        return n - n0;
//    }
//
//    @Override
//    public void intersects(RectNd rect, Consumer consumer) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//
//        for (int i = 0; i < size; i++) {
//            if (rect.intersects(entry[i])) {
//                consumer.accept(entry[i]);
//            }
//        }
//    }
//
//    @Override
//    public int size() {
//        return cacheNode.getSize();
//    }
//
//    @Override
//    public int totalSize() {
//        return cacheNode.getSize();
//    }
//
//    @Override
//    public boolean isLeaf() {
//        return true;
//    }
//
//    @Override
//    public RectNd getBound() {
//        return cacheNode.getMbr();
//    }
//
//    static Node create(final RectBuilder builder, final int mMin, final int M, TxCell txCell) {
//        return new NodeOfAxialSplitLeaf(builder, mMin, M, txCell);
//    }
//
//    /**
//     * Splits a leaf node that has the maximum number of entries into 2 leaf nodes of the same type with half
//     * of the entries in each one.
//     *
//     * @param t entry to be added to the full leaf node
//     * @return newly created node storing half the entries of this node
//     */
//    protected abstract Node split(final RectNd t);
//
//    @Override
//    public void forEach(Consumer consumer) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//
//        for (int i = 0; i < size; i++) {
//            consumer.accept(entry[i]);
//        }
//    }
//
//    @Override
//    public boolean contains(RectNd rect, RectNd t) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//
//        for (int i = 0; i < size; i++) {
//            if (rect.contains(entry[i])) {
//                if (entry[i].equals(t)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Figures out which newly made leaf node (see split method) to add a data entry to.
//     *
//     * @param l1Node left node
//     * @param l2Node right node
//     * @param t      data entry to be added
//     */
//    protected final void classify(final Node l1Node, final Node l2Node, final RectNd t) {
//        final RectNd tRect = builder.getBBox(t);
//        final RectNd l1Mbr = l1Node.getBound().getMbr(tRect);
//        final RectNd l2Mbr = l2Node.getBound().getMbr(tRect);
//        final double l1CostInc = Math.max(l1Mbr.cost() - (l1Node.getBound().cost() + tRect.cost()), 0.0);
//        final double l2CostInc = Math.max(l2Mbr.cost() - (l2Node.getBound().cost() + tRect.cost()), 0.0);
//        if (l2CostInc > l1CostInc) {
//            l1Node.add(t);
//        } else if (RTree.isEqual(l1CostInc, l2CostInc)) {
//            final double l1MbrCost = l1Mbr.cost();
//            final double l2MbrCost = l2Mbr.cost();
//            if (l1MbrCost < l2MbrCost) {
//                l1Node.add(t);
//            } else if (RTree.isEqual(l1MbrCost, l2MbrCost)) {
//                final double l1MbrMargin = l1Mbr.perimeter();
//                final double l2MbrMargin = l2Mbr.perimeter();
//                if (l1MbrMargin < l2MbrMargin) {
//                    l1Node.add(t);
//                } else if (RTree.isEqual(l1MbrMargin, l2MbrMargin)) {
//                    // break ties with least number
//                    if (l1Node.size() < l2Node.size()) {
//                        l1Node.add(t);
//                    } else {
//                        l2Node.add(t);
//                    }
//                } else {
//                    l2Node.add(t);
//                }
//            } else {
//                l2Node.add(t);
//            }
//        } else {
//            l2Node.add(t);
//        }
//
//    }
//
//
//    @Override
//    public String toString() {
//        final StringBuilder sb = new StringBuilder(128);
//        sb.append('[');
//        sb.append(cacheNode.getMbr());
//        sb.append(']');
//
//        return sb.toString();
//    }
//}
