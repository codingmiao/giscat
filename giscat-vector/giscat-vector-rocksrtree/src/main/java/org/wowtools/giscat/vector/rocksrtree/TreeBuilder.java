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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;
import org.wowtools.giscat.vector.pojo.Feature;
import org.wowtools.giscat.vector.pojo.FeatureCollection;
import org.wowtools.giscat.vector.util.analyse.Bbox;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static org.wowtools.giscat.vector.rocksrtree.RTree.TreeDbKey;

/**
 * RTree构建器，利用TreeBuilder构建出RTree，继而进行后续的修改、查询操作
 */
@Slf4j
public class TreeBuilder implements Closeable {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private long leafIdIndex = 0;

    private long branchIdIndex = 0;

    protected final int mMin;
    protected final int mMax;

    private final int cache1Size;
    private final RocksDB db;

    private final RTree rTree;

    protected String rootId;

    private final Function<Feature, RectNd> featureRectNdFunction;

    private final Map<String, ProtoAble> protoAbleCaches2;

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


    public static final class TreeBuilderConfig {
        /**
         * rocksdb配置参数
         */
        public @Nullable Options options;
        /**
         * 每个非叶子节点最少有几个子节点
         */
        public int mMin = 16;
        /**
         * 每个非叶子节点最多有几个子节点
         */
        public int mMax = 64;

        /**
         * 缓存的节点数，数值越大从磁盘读数据的概率越小，但越吃内存
         */
        public int cacheSize = 100000;

        /**
         * 如何取得feature的外接矩形，可以从geometry或属性入手进行构建，例如，默认值是取二维矩形范围:
         * <pre>
         *  (feature) -&gt; {
         *                 Bbox bbox = new Bbox(feature.getGeometry());
         *                 return new RectNd(new double[]{bbox.xmin, bbox.ymin}, new double[]{bbox.xmax, bbox.ymax});
         *      }
         * </pre>
         */
        public @Nullable Function<Feature, RectNd> featureRectNdFunction;
    }


    /**
     * 获得一个TreeBuilder示例
     *
     * @param dir    RTree持久化存储到本地磁盘的路径
     * @param config rtree的配置信息
     * @see TreeBuilderConfig
     */
    public TreeBuilder(@NotNull String dir, @Nullable TreeBuilderConfig config) {
        if (null == config) {
            config = new TreeBuilderConfig();
        }
        Options options = config.options;
        if (null == options) {
            options = buildDefaultRocksDbOptions();
        }
        try {
            db = RocksDB.open(options, dir);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        byte[] bytes;
        try {
            bytes = db.get(TreeDbKey);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }

        if (null == bytes) {
            if (config.mMin < 2) {
                log.warn("mMin的值过小，自动调整为2");
                config.mMin = 2;
            }
            if (config.mMax < config.mMin) {
                log.warn("mMax的值过小，自动调整为{}", config.mMin);
                config.mMax = config.mMin;
            }
            mMin = config.mMin;
            mMax = config.mMax;
        } else {
            RocksRtreePb.RTreePb pbTree;
            try {
                pbTree = RocksRtreePb.RTreePb.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
            mMin = pbTree.getMMin();
            mMax = pbTree.getMMax();
            log.debug("rtree已存在，使用已有值 mMin {} mMax {}", mMin, mMax);
            rootId = pbTree.getRootId();
        }

        rTree = new RTree(this);
        if (null == config.featureRectNdFunction) {
            featureRectNdFunction = (feature) -> {
                Bbox bbox = new Bbox(feature.getGeometry());
                return new RectNd(new double[]{bbox.xmin, bbox.ymin}, new double[]{bbox.xmax, bbox.ymax});
            };
        } else {
            featureRectNdFunction = config.featureRectNdFunction;
        }

        if (config.cacheSize < 128) {
            log.warn("cacheSize过小，调整为128");
            config.cacheSize = 128;
        }
        final int cacheSize = config.cacheSize;
        cache1Size = config.cacheSize / 10;

        protoAbleCaches2 = new LinkedHashMap(cacheSize, 0.75f, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > cacheSize;
            }
        };
    }

    public RTree getRTree() {
        return rTree;
    }

    protected RectNd buildFeatureRect(Feature feature) {
        RectNd rectNd = featureRectNdFunction.apply(feature);
        rectNd.feature = feature;
        return rectNd;
    }

    public TreeTransaction newTx() {
        return new TreeTransaction(db, this, cache1Size, protoAbleCaches2, lock);
    }

    protected void clearCache() {
//        branchMap.clear();
//        leafMap.clear();
    }

    protected Branch newBranch(TreeTransaction tx) {
        branchIdIndex++;
        String branchId = "B" + branchIdIndex;
        Branch node = new Branch(this, branchId);
        tx.put(branchId, node);
        return node;
    }

    protected Leaf newLeaf(TreeTransaction tx) {
        leafIdIndex++;
        String leafId = "L" + leafIdIndex;
        Leaf node = new Leaf(this, leafId);
        tx.put(leafId, node);
//        leafMap.put(nodeIdIndex, node);
        FeatureCollection fc = new FeatureCollection();
        fc.setFeatures(new ArrayList<>(mMax));
        return node;
    }

    protected Branch getBranch(String branchId, TreeTransaction tx) {
        return tx.get(Branch.class, branchId);
    }

    protected Leaf getLeaf(String leafId, TreeTransaction tx) {
        return tx.get(Leaf.class, leafId);
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


    public void close() {
        db.close();
    }
}
