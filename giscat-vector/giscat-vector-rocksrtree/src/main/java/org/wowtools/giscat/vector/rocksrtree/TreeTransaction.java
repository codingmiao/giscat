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
import java.util.Map;

import static org.wowtools.giscat.vector.rocksrtree.RTree.TreeDbKey;

/**
 * 操作树的事务
 *
 * @author liuyu
 * @date 2023/3/27
 */
public class TreeTransaction implements AutoCloseable {

    private final RocksDB db;
    private final WriteOptions writeOpt;
    private final WriteBatch batch;

    private final HashMap<String, ProtoAble> txAdded = new HashMap<>();
    private final HashSet<String> txDeleted = new HashSet<>();

    private final TreeBuilder builder;

    private final String treeRootId;

    protected TreeTransaction(RocksDB db, TreeBuilder builder) {
        this.db = db;
        this.builder = builder;
        writeOpt = new WriteOptions();
        batch = new WriteBatch();
        treeRootId = builder.getRTree().root;
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
        byte[] bytes;
        try {
            bytes = db.get(key.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
        txb = ProtoAble.fromBytes(t, builder, key, bytes);
        return txb;
    }


    public void commit() {
        try {
            for (Map.Entry<String, ProtoAble> e : txAdded.entrySet()) {
                batch.put(e.getKey().getBytes(StandardCharsets.UTF_8), e.getValue().toBytes());
            }
            for (String k : txDeleted) {
                batch.delete(k.getBytes(StandardCharsets.UTF_8));
            }
            if (null == treeRootId || !treeRootId.equals(builder.getRTree().root)) {
                //rtree发生过变化，存储一次rtree
                batch.put(TreeDbKey, builder.getRTree().toBytes());
            }
            db.write(writeOpt, batch);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        close();
    }


    @Override
    public void close() {

    }


}
