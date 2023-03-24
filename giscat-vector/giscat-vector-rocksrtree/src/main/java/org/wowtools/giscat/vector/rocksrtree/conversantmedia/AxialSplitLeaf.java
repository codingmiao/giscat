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

/**
 * Fast RTree split suggested by Yufei Tao taoyf@cse.cuhk.edu.hk
 * <p>
 * Perform an axial split
 * <p>
 * Created by jcairns on 5/5/15.
 */
final class AxialSplitLeaf extends Leaf {

    protected AxialSplitLeaf(final RectBuilder builder, final int mMin, final int mMax) {
        super(builder, mMin, mMax, RTree.Split.AXIAL);
    }

    @Override
    protected Node split(final RectNd t) {
        final Branch pNode = new Branch(builder, mMin, mMax, splitType);
        final Node l1Node = create(builder, mMin, mMax, splitType);
        final Node l2Node = create(builder, mMin, mMax, splitType);
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

}
