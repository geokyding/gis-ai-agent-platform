package com.dingky.gis.ai.platform.worker.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.gdal.gdal.gdal;
import org.gdal.ogr.ogr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: GdalConfig
 * Package: com.dingky.gis.ai.platform.worker.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/3 16:33
 * @Version 1.0
 **/
@Configuration
@Slf4j
public class GdalConfig {

    @Value("${gdal.library.path}")
    private static String gdalLibraryPath;

    @PostConstruct
    public void init() {
        try {
            log.info("开始加载 gdal/ogr 库: {}", gdalLibraryPath);
            System.loadLibrary(gdalLibraryPath);
            log.info("gdal/ogr 初始化完成");
            // 1.注册驱动
            gdal.AllRegister();
            ogr.RegisterAll();
            // 2. 设置编码（非常关键，防止中文乱码）
            gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "YES");
            gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("GDAL原生库加载失败，请检查gdal.library.path配置: " + gdalLibraryPath, e);
        }catch (Exception e){
            throw new RuntimeException("gdal/ogr 初始化失败", e);
        }
    }
}
