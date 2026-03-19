package com.dingky.gis.ai.vectorcore.vectorparsing.config;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.springframework.context.annotation.Configuration;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: GdalConfig
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/17 15:47
 * @Version 1.0
 **/
@Configuration
public class GdalConfig {
    private static Logger log = LogManager.getLogger(GdalConfig.class);
    @PostConstruct
    public void init() {
        // 1.注册驱动
        gdal.AllRegister();
        ogr.RegisterAll();
        // 2. 设置编码（非常关键，防止中文乱码）
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");

//        System.loadLibrary("gdaljni");
        log.info("gdal/ogr 初始化完成");
    }
}
