package com.dingky.gis.ai.platform.featureworker.service;

import lombok.extern.slf4j.Slf4j;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: GdalService
 * Package: com.dingky.gis.ai.platform.featureworker.service
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/7 15:37
 * @Version 1.0
 **/
@Service
@Slf4j
public class GdalService {
    public List<Long> readFeature(String filePath, String layerName, int offSet, int limit) {
        List<Long> result = new ArrayList<>();
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource == null) {
            log.error("无法打开文件：{}", filePath);
            return result;
        }
        Layer layer = dataSource.GetLayer(layerName);
        layer.SetNextByIndex(offSet);
        for (int i = 0; i < limit; i++) {
            Feature feature = layer.GetNextFeature();
            if (feature == null) break;
            result.add(feature.GetFID());
            feature.delete();
        }
        layer.delete();
        dataSource.delete();
        return result;
    }
}
