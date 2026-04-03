package com.dingky.gis.ai.platform.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.ogr;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: GdalService
 * Package: com.dingky.gis.ai.platform.worker.service
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/3 16:30
 * @Version 1.0
 **/
@Service
@Slf4j
public class GdalService {
    public List<String> getLayerNames(String filePath){
        log.info("获取图层名：{}", filePath);
        List<String> layerNames = new ArrayList<>();
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource != null){
            int layerCount = dataSource.GetLayerCount();
            for (int i = 0; i < layerCount; i++){
                layerNames.add(dataSource.GetLayer(i).GetName());
            }
        }else {
            log.info("无法打开文件: {}", filePath);
        }
        if (dataSource != null) dataSource.delete();
        return layerNames;
    }
}
