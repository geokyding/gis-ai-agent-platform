package com.dingky.gis.ai.vectorcore.vectorparsing.config;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${gdal.library.path}")
    private String gdalLibraryPath;

    @PostConstruct
    public void init() {
        try {
//            System.load(gdalLibraryPath);
            // 永远跨平台加载
            // 它的规则：
            //Windows → 自动找 gdaljni.dll 或 gdalalljni.dll
            //Linux → 自动找 libgdaljni.so，会自动添加lib 前缀和 .so 后缀
            //不需要路径！不需要路径！不需要路径！
            //Docker 里也不用改配置！
            log.info("开始加载 gdal/ogr 库: {}", gdalLibraryPath);
            System.loadLibrary(gdalLibraryPath);
            // 1.注册驱动
            gdal.AllRegister();
            ogr.RegisterAll();
            // 2. 设置编码（非常关键，防止中文乱码）
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
            gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");

            log.info("gdal/ogr 初始化完成");
        } catch (UnsatisfiedLinkError e) {
            log.error("GDAL原生库加载失败: {}", e.getMessage());
            throw new RuntimeException("GDAL原生库加载失败，请检查gdal.library.path配置: " + gdalLibraryPath, e);
        } catch (Exception e) {
            log.error("gdal/ogr 初始化失败: {}", e.getMessage());
            throw new RuntimeException("gdal/ogr 初始化失败", e);
        }
    }
}
