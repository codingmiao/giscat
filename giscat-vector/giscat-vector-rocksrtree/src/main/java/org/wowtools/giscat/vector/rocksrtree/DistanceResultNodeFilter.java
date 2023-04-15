package org.wowtools.giscat.vector.rocksrtree;


/**
 * 距离查询的node节点过滤器
 */
@FunctionalInterface
public interface DistanceResultNodeFilter {
    /**
     * 过滤
     *
     * @param dr 节点
     * @return 返回false则忽略此节点
     */
    boolean accept(DistanceResult dr);
}
