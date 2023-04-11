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

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


/**
 * RTree node that contains leaf nodes
 * <p>
 * Created by jcairns on 4/30/15.
 */
final class Branch extends Node {

    private final TreeBuilder builder;

    private final String[] child;

    private RectNd mbr;

    private int size;

    public Branch(TreeBuilder builder, String id) {
        super(builder, id);
        this.builder = builder;
        this.child = new String[builder.mMax];
    }

    @Override
    public void fill(byte[] bytes) {
        RocksRtreePb.BranchPb branchPb;
        try {
            branchPb = RocksRtreePb.BranchPb.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        List<String> childIdsList = branchPb.getChildIdsList();
        if (childIdsList.size() > 0) {
            int i = 0;
            for (String l : childIdsList) {
                child[i] = l;
                i++;
            }
            size = childIdsList.size();
        }
        if (branchPb.hasMbr()) {
            mbr = new RectNd(branchPb.getMbr());
        }
    }



    @Override
    protected byte[] toBytes() {
        RocksRtreePb.BranchPb.Builder branchBuilder = RocksRtreePb.BranchPb.newBuilder();
        if (null != child) {
            List<String> list = new ArrayList<>(size);
            for (String l : child) {
                if (l == null) {
                    break;
                }
                list.add(l);
            }
            branchBuilder.addAllChildIds(list);
        }
        if (null != mbr) {
            branchBuilder.setMbr(mbr.toBuilder());
        }
        return branchBuilder.build().toByteArray();
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

    private Node getChild(int i, TreeTransaction tx) {
        return builder.getNode(child[i], tx);
    }

    /**
     * Adds a data entry to one of the child nodes of this branch
     *
     * @param t data entry to add
     * @return Node that the entry was added to
     */
    @Override
    Node add(final RectNd t, TreeTransaction tx) {
        final RectNd tRect = builder.getBBox(t);
        if (size < builder.mMin) {
            for (int i = 0; i < size; i++) {
                if (getChild(i, tx).getBound().contains(tRect)) {
                    child[i] = getChild(i, tx).add(t, tx).id;
                    mbr = mbr.getMbr(getChild(i, tx).getBound());
                    tx.put(id, this);
                    return this;
                }
            }
            // no overlapping node - grow
            final Node nextLeaf = builder.newLeaf(tx);
            nextLeaf.add(t, tx);
            final int nextChild = addChild(nextLeaf);
            mbr = mbr.getMbr(getChild(nextChild, tx).getBound());
            tx.put(id, this);
            return this;

        } else {
            final int bestLeaf = chooseLeaf(t, tRect, tx);
            Node c = getChild(bestLeaf, tx);
            child[bestLeaf] = c.add(t, tx).id;
            mbr = mbr.getMbr(c.getBound());
            tx.put(id, this);
            return this;
        }
    }

    @Override
    Node remove(final RectNd t, TreeTransaction tx) {
        //TODO 用tx.remove "GC" 掉没有被引用的node
        final RectNd tRect = builder.getBBox(t);

        for (int i = 0; i < size; i++) {
            if (getChild(i, tx).getBound().intersects(tRect)) {
                child[i] = getChild(i, tx).remove(t, tx).id;

                if (getChild(i, tx) == null) {
                    System.arraycopy(child, i + 1, child, i, size - i - 1);
                    size--;
                    child[size] = null;
                    if (size > 0) i--;
                }
            }
        }

        if (size == 0) {
            tx.put(id, this);
            return null;
        } else if (size == 1) {
            // unsplit branch
            tx.put(id, this);
            Node c = getChild(0, tx);
            return c;
        }

        mbr = getChild(0, tx).getBound();
        for (int i = 1; i < size; i++) {
            mbr = mbr.getMbr(getChild(i, tx).getBound());
        }
        tx.put(id, this);
        return this;
    }

    @Override
    Node update(final RectNd told, final RectNd tnew, TreeTransaction tx) {
        //TODO 用tx.remove "GC" 掉没有被引用的node
        final RectNd tRect = builder.getBBox(told);
        for (int i = 0; i < size; i++) {
            if (tRect.intersects(getChild(i, tx).getBound())) {
                child[i] = getChild(i, tx).update(told, tnew, tx).id;
            }
            if (i == 0) {
                mbr = getChild(i, tx).getBound();
            } else {
                mbr = mbr.getMbr(getChild(i, tx).getBound());
            }
        }
        tx.put(id, this);
        return this;
    }


    @Override
    public boolean intersects(RectNd rect, FeatureConsumer consumer, TreeTransaction tx) {
        for (int i = 0; i < size; i++) {
            Node ci = getChild(i, tx);
            if (rect.intersects(ci.getBound())) {
                if (!ci.intersects(rect, consumer, tx)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean contains(RectNd rect, FeatureConsumer consumer, TreeTransaction tx) {
        for (int i = 0; i < size; i++) {
            Node ci = getChild(i, tx);
            if (rect.intersects(ci.getBound())) {
                if (!ci.contains(rect, consumer, tx)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * @return number of child nodes
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public int totalSize(TreeTransaction tx) {
        int s = 0;
        for (int i = 0; i < size; i++) {
            s += getChild(i, tx).totalSize(tx);
        }
        return s;
    }

    private int chooseLeaf(final RectNd t, final RectNd tRect, TreeTransaction tx) {
        if (size > 0) {
            int bestNode = 0;
            RectNd childMbr = getChild(0, tx).getBound().getMbr(tRect);
            double leastEnlargement = childMbr.cost() - (getChild(0, tx).getBound().cost() + tRect.cost());
            double leastPerimeter = childMbr.perimeter();

            for (int i = 1; i < size; i++) {
                RectNd cRect = getChild(i, tx).getBound();
                childMbr = tRect.getMbr(cRect);
                final double nodeEnlargement = childMbr.cost() - (getChild(i, tx).getBound().cost() + tRect.cost());
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
            n.add(t, tx);
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
    public Node[] getChildren(TreeTransaction tx) {
        Node[] nodes = new Node[size];
        for (int i = 0; i < size; i++) {
            nodes[i] = getChild(i, tx);
        }
        return nodes;
    }

    @Override
    public void forEach(Consumer<RectNd> consumer, TreeTransaction tx) {
        for (int i = 0; i < size; i++) {
            getChild(i, tx).forEach(consumer, tx);
        }
    }

    @Override
    public boolean contains(RectNd rect, RectNd t, TreeTransaction tx) {
        for (int i = 0; i < size; i++) {
            if (rect.intersects(getChild(i, tx).getBound())) {
                getChild(i, tx).contains(rect, t, tx);
            }
        }
        return false;
    }

    @Override
    public void collectStats(Stats stats, int depth, TreeTransaction tx) {
        for (int i = 0; i < size; i++) {
            getChild(i, tx).collectStats(stats, depth + 1, tx);
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
