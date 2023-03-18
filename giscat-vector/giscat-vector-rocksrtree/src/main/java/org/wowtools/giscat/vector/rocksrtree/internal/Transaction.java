package org.wowtools.giscat.vector.rocksrtree.internal;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author liuyu
 * @date 2023/3/15
 */
public class Transaction {
    private final WriteOptions writeOpt = new WriteOptions();
    private final WriteBatch batch = new WriteBatch();
    private final RocksDB db;
    private final HashMap<Long, byte[]> cacheAdded = new HashMap<>();
    private final HashSet<Long> cacheDeleted = new HashSet<>();

    public Transaction(RocksDB db) {
        this.db = db;
    }

    public void put(Long key, byte[] value) {
        cacheAdded.put(key, value);
        cacheDeleted.remove(key);
    }

    public void delete(Long key) {
        cacheDeleted.add(key);
        cacheAdded.remove(key);
    }

    public byte[] get(Long key) {
        byte[] v = cacheAdded.get(key);
        if (null != v) {
            return v;
        }
        byte[] bytesKey = ToBytesUtils.long2Bytes(key);
        try {
            return db.get(bytesKey);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            db.write(writeOpt, batch);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }
}
