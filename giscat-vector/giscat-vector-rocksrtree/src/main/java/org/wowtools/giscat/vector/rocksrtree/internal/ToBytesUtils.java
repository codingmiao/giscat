/*
 *
 *  * Copyright (c) 2022- "giscat (https://github.com/codingmiao/giscat)"
 *  *
 *  * 本项目采用自定义版权协议，在不同行业使用时有不同约束，详情参阅：
 *  *
 *  * https://github.com/codingmiao/giscat/blob/main/LICENSE
 *
 */

package org.wowtools.giscat.vector.rocksrtree.internal;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 字节码转换对象，注意，由于没有指定大小端，如果文件序列化后在其它机器上使用可能会有大小端问题而出错
 *
 * @author liuyu
 * @date 2023/3/15
 */
public class ToBytesUtils {
    public static byte[] string2Bytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytes2String(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] long2Bytes(long value) {
        byte[] bytes = new byte[Long.BYTES];
//        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putLong(value);
        ByteBuffer.wrap(bytes).putLong(value);
        return bytes;
    }

    public static long bytes2Long(byte[] bytes) {
        long value = ByteBuffer.wrap(bytes).getLong();
        return value;
    }

    public static byte[] double2Bytes(long value) {
        long longBits = Double.doubleToLongBits(value);
        byte[] bytes = new byte[Long.BYTES];
        ByteBuffer.wrap(bytes).putLong(longBits);
        return bytes;
    }

    public static double bytes2Double(byte[] bytes) {
        long longBits = ByteBuffer.wrap(bytes).getLong();
        double value = Double.longBitsToDouble(longBits);
        return value;
    }
}
