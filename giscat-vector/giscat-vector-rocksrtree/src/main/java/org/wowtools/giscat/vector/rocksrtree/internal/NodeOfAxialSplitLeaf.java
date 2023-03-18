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
//import java.util.Arrays;
//import java.util.Comparator;
//
///**
// * Fast RTree split suggested by Yufei Tao taoyf@cse.cuhk.edu.hk
// * <p>
// * Perform an axial split
// * <p>
// * Created by jcairns on 5/5/15.
// */
//public final class NodeOfAxialSplitLeaf extends NodeOfLeaf {
//
//
//    public static NodeOfAxialSplitLeaf getFromNeo(RectBuilder builder, long neoId, TxCell txCell) {
//        return new NodeOfAxialSplitLeaf(builder, txCell.getTx().getNodeById(neoId), txCell);
//    }
//
//    protected NodeOfAxialSplitLeaf(final RectBuilder builder, org.neo4j.graphdb.Node cacheNode, TxCell txCell) {
//        super(builder, cacheNode, txCell);
//    }
//
//    protected NodeOfAxialSplitLeaf(final RectBuilder builder, final int mMin, final int mMax, TxCell txCell) {
//        super(builder, mMin, mMax, txCell);
//    }
//
//    @Override
//    protected Node split(final RectNd t) {
//        int size = cacheNode.getSize();
//        RectNd[] entry = cacheNode.getEntry();
//        RectNd mbr = cacheNode.getMbr();
//
//        final NodeOfBranch pNode = new NodeOfBranch(builder, mMin, mMax, txCell);
//        final Node l1Node = create(builder, mMin, mMax, txCell);
//        final Node l2Node = create(builder, mMin, mMax, txCell);
//        final int nD = entry[0].getNDim();
//
//        // choose axis to split
//        int axis = 0;
//        double rangeD = mbr.getRange(0);
//        for (int d = 1; d < nD; d++) {
//            // split along the greatest range extent
//            final double dr = mbr.getRange(d);
//            if (dr > rangeD) {
//                axis = d;
//                rangeD = dr;
//            }
//        }
//
//        final int splitDimension = axis;
//
//        // sort along split dimension
//        final RectNd[] sortedMbr = Arrays.copyOf(entry, entry.length);
//
//        Arrays.sort(sortedMbr, new Comparator<RectNd>() {
//            @Override
//            public int compare(final RectNd o1, final RectNd o2) {
//                final PointNd p1 = o1.getCentroid();
//                final PointNd p2 = o2.getCentroid();
//
//                return Double.compare(p1.getCoord(splitDimension), p2.getCoord(splitDimension));
//            }
//        });
//
//        // divide sorted leafs
//        for (int i = 0; i < size / 2; i++) {
//            outerLoop:
//            for (int j = 0; j < size; j++) {
//                if (entry[j] == sortedMbr[i]) {
//                    l1Node.add(entry[j]);
//                    break outerLoop;
//                }
//            }
//        }
//
//        for (int i = size / 2; i < size; i++) {
//            outerLoop:
//            for (int j = 0; j < size; j++) {
//                if (entry[j] == sortedMbr[i]) {
//                    l2Node.add(entry[j]);
//                    break outerLoop;
//                }
//            }
//        }
//
//        classify(l1Node, l2Node, t);
//
//        pNode.addChild(l1Node);
//        pNode.addChild(l2Node);
//
//        return pNode;
//    }
//
//    @Override
//    public long getNeoNodeId() {
//        return cacheNode.getNode().getId();
//    }
//}
