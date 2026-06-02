package com.dingky.gis.demo.title;

/**
 * Shapefile 处理器工厂
 * 
 * 根据配置或数据规模自动选择合适的处理器
 */
public class ShapefileProcessorFactory {
    
    public enum ProcessorType {
        /** 本地并行处理（默认） */
        LOCAL_PARALLEL,
        
        /** Spark 分布式处理 */
        SPARK_DISTRIBUTED
    }
    
    /**
     * 创建处理器
     * 
     * @param type 处理器类型
     * @return 处理器实例
     */
    public static ShapefileProcessor create(ProcessorType type) {
        switch (type) {
            case LOCAL_PARALLEL:
                return new LocalParallelProcessor();
            
            case SPARK_DISTRIBUTED:
                return new SparkDistributedProcessor();
            
            default:
                throw new IllegalArgumentException("不支持的处理器类型: " + type);
        }
    }
    
    /**
     * 根据数据量自动选择处理器
     * 
     * @param estimatedFeatureCount 预估的 Feature 总数
     * @return 推荐的处理器类型
     */
    public static ProcessorType autoSelect(long estimatedFeatureCount) {
        if (estimatedFeatureCount < 10_000_000) {
            // 千万级以下：使用本地并行
            return ProcessorType.LOCAL_PARALLEL;
        } else {
            // 千万级以上：建议使用 Spark
            System.out.println("警告: 数据量较大 (" + estimatedFeatureCount + ")，建议使用 Spark 分布式处理");
            return ProcessorType.SPARK_DISTRIBUTED;
        }
    }
}
