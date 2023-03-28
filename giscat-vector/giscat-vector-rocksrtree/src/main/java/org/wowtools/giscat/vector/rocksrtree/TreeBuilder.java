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

import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;

import java.util.ArrayList;

/**
 * Created by jcairns on 4/30/15.
 */
public abstract class TreeBuilder {


//    private final Map<Long, byte[]> branchMap = new HashMap<>();

//    private final Map<Long, Leaf> leafMap = new HashMap<>();

    private long leafIdIndex = 0;

    private long branchIdIndex = 0;

    protected final int mMin;
    protected final int mMax;

    private final RocksDB db;

    private static Options buildDefaultRocksDbOptions() {
        Options options = new Options();
        final Filter bloomFilter = new BloomFilter(10);
        final Statistics stats = new Statistics();
        final RateLimiter rateLimiter = new RateLimiter(10000000, 10000, 10);

        options.setCreateIfMissing(true)
                .setStatistics(stats)
                .setWriteBufferSize(8 * SizeUnit.KB)
                .setMaxWriteBufferNumber(3)
                .setMaxBackgroundJobs(10)
                .setCompressionType(CompressionType.SNAPPY_COMPRESSION)
                .setCompactionStyle(CompactionStyle.UNIVERSAL);

        final BlockBasedTableConfig table_options = new BlockBasedTableConfig();
        Cache cache = new LRUCache(512, 6);
        table_options.setBlockCache(cache)
                .setFilterPolicy(bloomFilter)
                .setBlockSizeDeviation(5)
                .setBlockRestartInterval(10)
                .setCacheIndexAndFilterBlocks(true)
                .setBlockCacheCompressed(new LRUCache(512, 10));
        options.setTableFormatConfig(table_options);
        options.setRateLimiter(rateLimiter);
        return options;
    }


    public TreeBuilder(String fileDir, @Nullable Options options, int mMin, int mMax) {
        if (null == options) {
            options = buildDefaultRocksDbOptions();
        }
        try {
            db = RocksDB.open(options, fileDir);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        this.mMin = mMin;
        this.mMax = mMax;
    }

    public abstract String getFeatureKey(Feature feature);


    protected RectNd buildFeatureRect(Feature feature) {
        RectNd rectNd = getFeatureRect(feature);
        rectNd.feature = feature;
        return rectNd;
    }

    public abstract RectNd getFeatureRect(Feature feature);

    public TreeTransaction newTx() {
        return new TreeTransaction(db);
    }

    protected void clearCache() {
//        branchMap.clear();
//        leafMap.clear();
    }

    protected Branch newBranch(TreeTransaction tx) {
        branchIdIndex++;
        String branchId = "B" + branchIdIndex;
        Branch node = new Branch(this, branchId);
        tx.put(branchId, node.toBytes());
        return node;
    }

    protected Leaf newLeaf(TreeTransaction tx) {
        leafIdIndex++;
        String leafId = "L" + leafIdIndex;
        Leaf node = new Leaf(this, leafId);
        tx.put(leafId, node.toBytes());
//        leafMap.put(nodeIdIndex, node);
        FeatureCollection fc = new FeatureCollection();
        fc.setFeatures(new ArrayList<>(mMax));
        return node;
    }

    protected Branch getBranch(String branchId, TreeTransaction tx) {
//        byte[] bytes = branchMap.get(branchId);
        byte[] bytes = tx.get(branchId);
        if (null == bytes) {
            return null;
        }
        return Branch.fromBytes(this, branchId, bytes);
    }

    protected Leaf getLeaf(String leafId, TreeTransaction tx) {
        byte[] bytes = tx.get(leafId);
        if (null == bytes) {
            return null;
        }
        return Leaf.fromBytes(this, leafId, bytes);
    }


    protected Node getNode(String nodeId, TreeTransaction tx) {
        char type = nodeId.charAt(0);
        if ('L' == type) {
            return getLeaf(nodeId, tx);
        } else if ('B' == type) {
            return getBranch(nodeId, tx);
        } else {
            return null;
        }
    }

    /**
     * Build a bounding rectangle for the given element
     *
     * @param t - element to bound
     * @return HyperRect impl for this entry
     */
    protected RectNd getBBox(RectNd t) {
        return t;
    }

    /**
     * Build a bounding rectangle for given points (min and max, usually)
     *
     * @param p1 - first point (top-left point, for example)
     * @param p2 - second point (bottom-right point, for example)
     * @return HyperRect impl defined by two points
     */
    protected RectNd getMbr(PointNd p1, PointNd p2) {
        return new RectNd(p1, p2);
    }


}
