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

import java.util.function.Consumer;

/**
 * Created by jcovert on 6/18/15.
 */
final class CounterNode implements Node {
    private final Node node;

    static int searchCount = 0;
    static int bboxEvalCount = 0;

    CounterNode(final Node node) {
        this.node = node;
    }

    @Override
    public boolean isLeaf() {
        return node.isLeaf();
    }

    @Override
    public RectNd getBound() {
        return node.getBound();
    }

    @Override
    public Node add(RectNd t) {
        return node.add(t);
    }

    @Override
    public Node remove(RectNd t) { return node.remove(t); }

    @Override
    public Node update(RectNd told, RectNd tnew) { return node.update(told, tnew); }

    @Override
    public int search(RectNd rect, RectNd[] t, int n) {
        searchCount++;
        bboxEvalCount += node.size();
        return node.search(rect, t, n);
    }

    @Override
    public int size() {
        return node.size();
    }

    @Override
    public int totalSize() {
        return node.totalSize();
    }

    @Override
    public void forEach(Consumer<RectNd> consumer) {
        node.forEach(consumer);
    }

    @Override
    public void search(RectNd rect, Consumer<RectNd> consumer) {
        node.search(rect, consumer);
    }

    @Override
    public int intersects(RectNd rect, RectNd[] t, int n) {
        return node.intersects(rect, t, n);
    }

    @Override
    public void intersects(RectNd rect, Consumer<RectNd> consumer) {
        node.intersects(rect, consumer);
    }

    @Override
    public boolean contains(RectNd rect, RectNd t) {
        return node.contains(rect, t);
    }

    @Override
    public void collectStats(Stats stats, int depth) {
        node.collectStats(stats, depth);
    }

    @Override
    public Node instrument() {
        return this;
    }
}
