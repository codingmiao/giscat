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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jcairns on 4/30/15.
 */
public abstract class TreeBuilder {

    private final Map<Long, Branch> branchMap = new HashMap<>();

    private final Map<Long, Leaf> leafMap = new HashMap<>();

    private long nodeIdIndex = 0;
    protected final int mMin;
    protected final int mMax;

    public TreeBuilder(int mMin, int mMax) {
        this.mMin = mMin;
        this.mMax = mMax;
    }

    protected Branch newBranch() {
        nodeIdIndex++;
        Branch node = new Branch(this, nodeIdIndex);
        branchMap.put(nodeIdIndex, node);
        return node;
    }

    protected Leaf newLeaf() {
        nodeIdIndex++;
        Leaf node = new Leaf(this, nodeIdIndex);
        leafMap.put(nodeIdIndex, node);
        return node;
    }

    protected Branch getBranch(long branchId) {
        return branchMap.get(branchId);
    }

    protected Leaf getLeaf(long leafId) {
        return leafMap.get(leafId);
    }

    protected Node getNode(long nodeId) {
        Node node = getLeaf(nodeId);
        if (null != node) {
            return node;
        }
        return getBranch(nodeId);
    }

    /**
     * Build a bounding rectangle for the given element
     *
     * @param t - element to bound
     * @return HyperRect impl for this entry
     */
    protected abstract RectNd getBBox(RectNd t);


    /**
     * Build a bounding rectangle for given points (min and max, usually)
     *
     * @param p1 - first point (top-left point, for example)
     * @param p2 - second point (bottom-right point, for example)
     * @return HyperRect impl defined by two points
     */
    protected abstract RectNd getMbr(PointNd p1, PointNd p2);
}
