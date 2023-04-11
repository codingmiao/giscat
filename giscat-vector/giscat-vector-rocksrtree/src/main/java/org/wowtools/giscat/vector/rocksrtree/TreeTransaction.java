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

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import static org.wowtools.giscat.vector.rocksrtree.RTree.TreeDbKey;

/**
 * 操作树的事务
 *
 * @author liuyu
 * @date 2023/3/27
 */
public class TreeTransaction implements AutoCloseable {

    private static final byte[] nullBytes = new byte[0];

    private static final ProtoAble nullProtoAble = new ProtoAble(null, null) {
        @Override
        public void fill(byte[] bytes) {

        }

        @Override
        protected byte[] toBytes() {
            return nullBytes;
        }
    };

    private final RocksDB db;
    private final WriteOptions writeOpt;
    private final WriteBatch batch;

    private final HashMap<String, ProtoAble> txAdded = new HashMap<>();
    private final HashSet<String> txDeleted = new HashSet<>();

    private final TreeBuilder builder;

    private final String treeRootId;

    private final ReadWriteLock lock;

    private boolean commited = false;


    //TODO 一二级缓存设计考虑
    private final Map<String, ProtoAble> protoAbleCaches2;
    private final Map<String, ProtoAble> protoAbleCaches1;

    protected TreeTransaction(RocksDB db, TreeBuilder builder, int cache1Size, Map<String, ProtoAble> protoAbleCaches2, ReadWriteLock lock) {
        protoAbleCaches1 = new LinkedHashMap(cache1Size, 0.75f, true) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > cache1Size;
            }
        };
        this.db = db;
        this.builder = builder;
        this.protoAbleCaches2 = protoAbleCaches2;
        writeOpt = new WriteOptions();
        batch = new WriteBatch();
        treeRootId = builder.rootId;
        this.lock = lock;
        lock.readLock().lock();
    }

    protected void put(String key, ProtoAble value) {
        txDeleted.remove(key);
        txAdded.put(key, value);
    }


    protected void remove(String key) {
        txAdded.remove(key);
        txDeleted.add(key);
    }


    protected <T extends ProtoAble> T get(Class<T> t, String key) {
        if (txDeleted.contains(key)) {
            return null;
        }
        T txb = (T) txAdded.get(key);
        if (null != txb) {
            return txb;
        }
        ProtoAble cache;
        cache = protoAbleCaches1.get(key);
        if (null != cache) {
            if (cache == nullProtoAble) {
                return null;
            } else {
                return (T) cache;
            }
        }

        cache = protoAbleCaches2.get(key);
        if (null != cache) {
            if (cache == nullProtoAble) {
                return null;
            } else {
                return (T) cache;
            }
        }


        byte[] bytes;
        try {
            bytes = db.get(key.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        if (null == bytes) {
            protoAbleCaches1.put(key, nullProtoAble);
            return null;
        } else {
            txb = ProtoAble.fromBytes(t, builder, key, bytes);
            protoAbleCaches1.put(key, txb);
            return txb;
        }

    }


    public void commit() {
        if (commited) {
            throw new RuntimeException("事务已经提交过一次了");
        }
        commited = true;
        try {
            for (Map.Entry<String, ProtoAble> e : txAdded.entrySet()) {
                batch.put(e.getKey().getBytes(StandardCharsets.UTF_8), e.getValue().toBytes());
            }
            for (String k : txDeleted) {
                batch.delete(k.getBytes(StandardCharsets.UTF_8));
            }
            if (null == treeRootId || !treeRootId.equals(builder.rootId)) {
                //rtree发生过变化，存储一次rtree
                batch.put(TreeDbKey, builder.getRTree().toBytes());
            }
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                db.write(writeOpt, batch);
                protoAbleCaches2.putAll(txAdded);
                for (String s : txDeleted) {
                    protoAbleCaches2.remove(s);
                }
            } finally {
                lock.writeLock().unlock();
            }
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        txAdded.clear();
        txDeleted.clear();
        protoAbleCaches1.clear();
    }


    @Override
    public void close() {
        if (!commited) {
            lock.readLock().unlock();
            if (protoAbleCaches1.size() > 0) {
                lock.writeLock().lock();
                protoAbleCaches2.putAll(protoAbleCaches1);
                lock.writeLock().unlock();
            }
        }
        batch.close();
        writeOpt.close();
    }


}
