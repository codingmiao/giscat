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

import java.util.function.Consumer;

/**
 * RTree node that contains leaf nodes
 * <p>
 * Created by jcairns on 4/30/15.
 */
final class Branch extends Node {

    private final TreeBuilder builder;

    private final long[] child;

    private RectNd mbr;

    private int size;

    Branch(final TreeBuilder builder, long id) {
        super(id);
        this.builder = builder;
        this.child = new long[builder.mMax];
    }

    /**
     * Add a new node to this branch's list of children
     *
     * @param n node to be added (can be leaf or branch)
     * @return position of the added node
     */
    protected int addChild(final Node n) {
        if (size < builder.mMax) {
            child[size] = n.id;
            size++;

            if (mbr != null) {
                mbr = mbr.getMbr(n.getBound());
            } else {
                mbr = n.getBound();
            }

            return size - 1;
        } else {
            throw new RuntimeException("Too many children");
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public RectNd getBound() {
        return mbr;
    }

    private Node getChild(int i) {
        return builder.getNode(child[i]);
    }

    /**
     * Adds a data entry to one of the child nodes of this branch
     *
     * @param t data entry to add
     * @return Node that the entry was added to
     */
    @Override
    Node add(final RectNd t, Transaction tx) {
        final RectNd tRect = builder.getBBox(t);
        if (size < builder.mMin) {
            for (int i = 0; i < size; i++) {
                if (getChild(i).getBound().contains(tRect)) {
                    child[i] = getChild(i).add(t,tx).id;
                    mbr = mbr.getMbr(getChild(i).getBound());
                    return this;
                }
            }
            // no overlapping node - grow
            final Node nextLeaf = builder.newLeaf(tx);
            nextLeaf.add(t,tx);
            final int nextChild = addChild(nextLeaf);
            mbr = mbr.getMbr(getChild(nextChild).getBound());

            return this;

        } else {
            final int bestLeaf = chooseLeaf(t, tRect,tx);

            child[bestLeaf] = getChild(bestLeaf).add(t,tx).id;
            mbr = mbr.getMbr(getChild(bestLeaf).getBound());

            return this;
        }
    }

    @Override
    Node remove(final RectNd t, Transaction tx) {
        final RectNd tRect = builder.getBBox(t);

        for (int i = 0; i < size; i++) {
            if (getChild(i).getBound().intersects(tRect)) {
                child[i] = getChild(i).remove(t,tx).id;

                if (getChild(i) == null) {
                    System.arraycopy(child, i + 1, child, i, size - i - 1);
                    size--;
                    child[size] = 0;
                    if (size > 0) i--;
                }
            }
        }

        if (size == 0) {
            return null;
        } else if (size == 1) {
            // unsplit branch
            return getChild(0);
        }

        mbr = getChild(0).getBound();
        for (int i = 1; i < size; i++) {
            mbr = mbr.getMbr(getChild(i).getBound());
        }

        return this;
    }

    @Override
    Node update(final RectNd told, final RectNd tnew, Transaction tx) {
        final RectNd tRect = builder.getBBox(told);
        for (int i = 0; i < size; i++) {
            if (tRect.intersects(getChild(i).getBound())) {
                child[i] = getChild(i).update(told, tnew,tx).id;
            }
            if (i == 0) {
                mbr = getChild(i).getBound();
            } else {
                mbr = mbr.getMbr(getChild(i).getBound());
            }
        }
        return this;
    }

    @Override
    public void search(RectNd rect, Consumer<RectNd> consumer) {
        for (int i = 0; i < size; i++) {
            if (rect.intersects(getChild(i).getBound())) {
                getChild(i).search(rect, consumer);
            }
        }
    }

    @Override
    public int search(final RectNd rect, final RectNd[] t, int n) {
        final int tLen = t.length;
        final int n0 = n;
        for (int i = 0; i < size && n < tLen; i++) {
            if (rect.intersects(getChild(i).getBound())) {
                n += getChild(i).search(rect, t, n);
            }
        }
        return n - n0;
    }

    @Override
    public void intersects(RectNd rect, Consumer<RectNd> consumer) {
        for (int i = 0; i < size; i++) {
            if (rect.intersects(getChild(i).getBound())) {
                getChild(i).intersects(rect, consumer);
            }
        }
    }

    @Override
    public int intersects(final RectNd rect, final RectNd[] t, int n) {
        final int tLen = t.length;
        final int n0 = n;
        for (int i = 0; i < size && n < tLen; i++) {
            if (rect.intersects(getChild(i).getBound())) {
                n += getChild(i).intersects(rect, t, n);
            }
        }
        return n - n0;
    }

    /**
     * @return number of child nodes
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public int totalSize() {
        int s = 0;
        for (int i = 0; i < size; i++) {
            s += getChild(i).totalSize();
        }
        return s;
    }

    private int chooseLeaf(final RectNd t, final RectNd tRect,Transaction tx) {
        if (size > 0) {
            int bestNode = 0;
            RectNd childMbr = getChild(0).getBound().getMbr(tRect);
            double leastEnlargement = childMbr.cost() - (getChild(0).getBound().cost() + tRect.cost());
            double leastPerimeter = childMbr.perimeter();

            for (int i = 1; i < size; i++) {
                childMbr = getChild(i).getBound().getMbr(tRect);
                final double nodeEnlargement = childMbr.cost() - (getChild(i).getBound().cost() + tRect.cost());
                if (nodeEnlargement < leastEnlargement) {
                    leastEnlargement = nodeEnlargement;
                    leastPerimeter = childMbr.perimeter();
                    bestNode = i;
                } else if (RTree.isEqual(nodeEnlargement, leastEnlargement)) {
                    final double childPerimeter = childMbr.perimeter();
                    if (childPerimeter < leastPerimeter) {
                        leastEnlargement = nodeEnlargement;
                        leastPerimeter = childPerimeter;
                        bestNode = i;
                    }
                } // else its not the least

            }
            return bestNode;
        } else {
            final Node n = builder.newLeaf(tx);
            n.add(t,tx);
            child[size] = n.id;
            size++;

            if (mbr == null) {
                mbr = n.getBound();
            } else {
                mbr = mbr.getMbr(n.getBound());
            }

            return size - 1;
        }
    }

    /**
     * Return child nodes of this branch.
     *
     * @return array of child nodes (leaves or branches)
     */
    public Node[] getChildren() {
        Node[] nodes = new Node[size];
        for (int i = 0; i < size; i++) {
            nodes[i] = getChild(i);
        }
        return nodes;
    }

    @Override
    public void forEach(Consumer<RectNd> consumer) {
        for (int i = 0; i < size; i++) {
            getChild(i).forEach(consumer);
        }
    }

    @Override
    public boolean contains(RectNd rect, RectNd t) {
        for (int i = 0; i < size; i++) {
            if (rect.intersects(getChild(i).getBound())) {
                getChild(i).contains(rect, t);
            }
        }
        return false;
    }

    @Override
    public void collectStats(Stats stats, int depth) {
        for (int i = 0; i < size; i++) {
            getChild(i).collectStats(stats, depth + 1);
        }
        stats.countBranchAtDepth(depth);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("BRANCH[");
        sb.append(mbr);
        sb.append(']');

        return sb.toString();
    }

}
