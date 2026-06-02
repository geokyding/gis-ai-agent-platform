package com.dingky.gis.demo.title;

import java.util.List;
import java.util.Map;

/**
 * Shapefile 处理器接口（支持多种实现）
 * 
 * 架构演进：
 * - LocalParallelProcessor: 单机并行处理（当前使用）
 * - SparkDistributedProcessor: Spark 分布式处理（未来升级）
 */
public interface ShapefileProcessor {
    
    /**
     * 处理多个 Shapefile
     * 
     * @param shpPaths Shapefile 路径列表
     * @return 分组后的数据，key 为标准化坐标，value 为排序后的 code 列表
     */
    Map<String, List<String>> process(List<String> shpPaths);
    
    /**
     * 获取处理器名称
     */
    String getProcessorName();
}
