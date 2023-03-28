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

import org.jetbrains.annotations.NotNull;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

    private final HashMap<String, byte[]> txAdded = new HashMap<>();
    private final HashSet<String> txDeleted = new HashSet<>();


    protected TreeTransaction(RocksDB db) {
        this.db = db;
        writeOpt = new WriteOptions();
        batch = new WriteBatch();
    }

    protected void put(String key, byte[] value) {
        txDeleted.remove(key);
        txAdded.put(key, value);
    }


    protected void remove(String key) {
        txAdded.remove(key);
        txDeleted.add(key);
    }


    protected byte[] get(String key) {
        if (txDeleted.contains(key)) {
            return null;
        }
        byte[] txb = txAdded.get(key);
        if (null != txb) {
            return txb;
        }
        try {
            return db.get(key.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }


    public void commit() {
        try {
            for (Map.Entry<String, byte[]> e : txAdded.entrySet()) {
                batch.put(e.getKey().getBytes(StandardCharsets.UTF_8), e.getValue());
            }
            for (String k : txDeleted) {
                batch.delete(k.getBytes(StandardCharsets.UTF_8));
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
