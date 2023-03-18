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
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.function.Consumer;
//
///**
// * <p>Data structure to make range searching more efficient. Indexes multi-dimensional information
// * such as geographical coordinates or rectangles. Groups information and represents them with a
// * minimum bounding rectangle (mbr). When searching through the tree, any query that does not
// * intersect an mbr can ignore any data entries in that mbr.</p>
// * <p>More information can be found here @see <a href="https://en.wikipedia.org/wiki/R-tree">https://en.wikipedia.org/wiki/R-tree</a></p>
// * <p>
// * Created by jcairns on 4/30/15.</p>
// */
//public final class RTree implements SpatialSearch {
//    private static final double EPSILON = 1e-12;
//
//    private final int mMin;
//    private final int mMax;
//    private final RectBuilder builder;
//
//    private final TxCell txCell;
//
//
//    private long rootNodeId = -1;
//    private final long metadataNodeId;
//
//    public RTree(final RectBuilder builder, String name, final int mMin, final int mMax, TxCell txCell) {
//        this.mMin = mMin;
//        this.mMax = mMax;
//        this.builder = builder;
//        this.txCell = txCell;
//
//        //构造METADATA节点
//        org.neo4j.graphdb.Node metadataNode = txCell.getTx().createNode(Labels.METADATA);
//        metadataNode.setProperty("mMin", mMin);
//        metadataNode.setProperty("mMax", mMax);
//        metadataNode.setProperty("name", name);
//        metadataNodeId = metadataNode.getId();
//    }
//
//    public RTree(final RectBuilder builder, long metadataNodeId, final int mMin, final int mMax, TxCell txCell) {
//        this.mMin = mMin;
//        this.mMax = mMax;
//        this.builder = builder;
//        this.txCell = txCell;
//        this.metadataNodeId = metadataNodeId;
//
//        //查找root
//        org.neo4j.graphdb.Node metadataNode = txCell.getTx().getNodeById(metadataNodeId);
//        Iterator<Relationship> iterator = metadataNode.getRelationships(Direction.OUTGOING, Relationships.RTREE_METADATA_TO_ROOT).iterator();
//        if (iterator.hasNext()) {
//            rootNodeId = iterator.next().getEndNodeId();
//        }
//    }
//
//    @Override
//    public int search(final RectNd rect, final RectNd[] t) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void search(RectNd rect, Consumer consumer) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void search(RectNd rect, Collection collection) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int intersects(final RectNd rect, final RectNd[] t) {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            return root.intersects(rect, t, 0);
//        }
//        return 0;
//    }
//
//    @Override
//    public void intersects(RectNd rect, Consumer consumer) {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            root.intersects(rect, consumer);
//        }
//    }
//
//    @Override
//    public void add(final RectNd t) {
//        Node root;
//        if (rootNodeId != -1) {
//            root = txCell.getNodeFromNeo4j(rootNodeId);
//            root = root.add(t);
//        } else {
//            root = NodeOfLeaf.create(builder, mMin, mMax, txCell);
//            root.add(t);
//        }
//        long newRootId = root.getNeoNodeId();
//        if (rootNodeId != newRootId) {
//            //root节点变化，修改metadata节点指向
//            rootNodeId = newRootId;
//            Transaction tx = txCell.getTx();
//            org.neo4j.graphdb.Node metadataNode = tx.getNodeById(metadataNodeId);
//            for (Relationship relationship : metadataNode.getRelationships(Relationships.RTREE_METADATA_TO_ROOT)) {
//                relationship.delete();
//            }
//            metadataNode.createRelationshipTo(tx.getNodeById(rootNodeId), Relationships.RTREE_METADATA_TO_ROOT);
//        }
//    }
//
//    @Override
//    public void remove(final RectNd t) {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            root.remove(t);
//        }
//    }
//
//    @Override
//    public void update(final RectNd told, final RectNd tnew) {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            root.update(told, tnew);
//        }
//    }
//
//    @Override
//    public int getEntryCount() {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            return root.totalSize();
//        }
//        return 0;
//    }
//
//    /**
//     * returns whether or not the HyperRect will enclose all of the data entries in t
//     *
//     * @param t Data entries to be evaluated
//     * @return boolean - Whether or not all entries lie inside rect
//     */
//    @Override
//    public boolean contains(final RectNd t) {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            final RectNd bbox = builder.getBBox(t);
//            return root.contains(bbox, t);
//        }
//        return false;
//    }
//
//    public static boolean isEqual(final double a, final double b) {
//        return isEqual(a, b, EPSILON);
//    }
//
//    static boolean isEqual(final double a, final double b, final double eps) {
//        return Math.abs(a - b) <= ((Math.abs(a) < Math.abs(b) ? Math.abs(b) : Math.abs(a)) * eps);
//    }
//
//    @Override
//    public void forEach(Consumer consumer) {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        if (root != null) {
//            root.forEach(consumer);
//        }
//    }
//
//
//    public Node getRoot() {
//        Node root = txCell.getNodeFromNeo4j(rootNodeId);
//        return root;
//    }
//
//
//    public long getMetadataNodeId() {
//        return metadataNodeId;
//    }
//
//    public int getmMin() {
//        return mMin;
//    }
//
//    public int getmMax() {
//        return mMax;
//    }
//
//    public long getRootNodeId() {
//        return rootNodeId;
//    }
//
//    /**
//     * Different methods for splitting nodes in an RTree.
//     * <p>
//     * AXIAL has been shown to give good performance for many general spatial problems,
//     *
//     * <p>
//     * Created by ewhite on 10/28/15.
//     */
//    public enum Split {
//        AXIAL,
////        LINEAR,
////        QUADRATIC,
//    }
//}
