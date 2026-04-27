package com.dingky.gis.ai.platform.worker.service;

import com.dingky.gis.ai.platform.common.model.FieldDef;
import lombok.extern.slf4j.Slf4j;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
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

    public long getFeatureCount(String filePath, String layerName) {
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource != null){
            Layer layer = dataSource.GetLayer(layerName);
            if (layer != null){
                return layer.GetFeatureCount();
            }
        }
        if (dataSource != null) dataSource.delete();
        return 0;
    }

    public List<FieldDef> getFields(String filePath, String layerName) {
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource == null){
            log.error("无法打开文件：{}", filePath);
        }
        Layer layer = dataSource.GetLayer(layerName);
        List<FieldDef> fields = new ArrayList<>();
        fields.add(new FieldDef("fid", "BIGINT"));
        for (int i = 0; i < layer.GetLayerDefn().GetFieldCount(); i++){
            fields.add(new FieldDef(layer.GetLayerDefn().GetFieldDefn(i).GetNameRef(), mapType(layer.GetLayerDefn().GetFieldDefn(i).GetType())));
        }
        if (layer != null) layer.delete();
        if (dataSource != null) dataSource.delete();
        return fields;
    }
    private String mapType(int gdalType) {
        switch (gdalType) {

            case ogr.OFTInteger:
            case ogr.OFTInteger64:
                return "BIGINT";

            case ogr.OFTReal:
                return "DOUBLE PRECISION";

            case ogr.OFTString:
                return "TEXT";

            case ogr.OFTDate:
                return "DATE";

            case ogr.OFTDateTime:
                return "TIMESTAMP";

            default:
                return "TEXT";
        }
    }
}
