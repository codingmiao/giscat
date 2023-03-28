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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 可以转为proto对象的接口标识
 *
 * @author liuyu
 * @date 2023/3/28
 */
abstract class ProtoAble {

    protected final TreeBuilder builder;
    protected final String id;

    private static final Map<Class<? extends ProtoAble>, Constructor<? extends ProtoAble>> implConstructors;

    static {
        List<Class<? extends ProtoAble>> impls = List.of(
                Branch.class,
                Leaf.class
        );
        try {
            Map<Class<? extends ProtoAble>, Constructor<? extends ProtoAble>> constructors = new HashMap<>();
            for (Class<? extends ProtoAble> impl : impls) {
                Constructor<? extends ProtoAble> constructor = impl.getConstructor(TreeBuilder.class, String.class);
                constructors.put(impl, constructor);
            }
            implConstructors = Map.copyOf(constructors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProtoAble(TreeBuilder builder, String id) {
        this.builder = builder;
        this.id = id;
    }

    public abstract void fill(byte[] bytes);

    protected abstract byte[] toBytes();

    public static <T extends ProtoAble> T fromBytes(Class<T> t, TreeBuilder builder, String id, byte[] bytes) {
        Constructor<T> constructor = (Constructor<T>) implConstructors.get(t);
        T instance;
        try {
            instance = constructor.newInstance(builder, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        instance.fill(bytes);
        return instance;
    }
}
