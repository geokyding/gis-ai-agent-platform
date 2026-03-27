package com.dingky.gis.ai.vectorcore.vectorparsing.service;

// import com.alibaba.fastjson.JSONObject;
import com.dingky.gis.ai.vectorcore.vectorparsing.dto.response.ShpParseResponse;
// import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ShpService
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing.service
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/17 16:25
 * @Version 1.0
 **/
@Service
public class ShpService {
    private static Logger log = LogManager.getLogger(ShpService.class);
    static {
        log.info("ShpService 初始化完成");
    }
    public ShpParseResponse parse(String path){
        log.info("执行ShpService.parse");
        ShpParseResponse result = new ShpParseResponse();
        DataSource dataSource = ogr.Open(path, 0);
        if (dataSource == null) {
            System.out.println("无法打开文件: "+ path);
        }
        assert dataSource != null;
        Layer layer = dataSource.GetLayer(0);
        if (layer == null) {
            System.out.println("无法打开图层: "+ path);
        }
        assert layer != null;
        result.setName(layer.GetName());
        result.setGeomType(layer.GetGeomType());
        result.setGeomTypeName(ogr.GeometryTypeToName(layer.GetGeomType()));
        result.setSrs(layer.GetSpatialRef().toString());
        result.setExtent(layer.GetExtent());
        result.setFeatureCount(layer.GetFeatureCount());
        // 解析字段信息
        Map<String,String> fieldInfo = new HashMap<>();
        for (int i = 0; i < layer.GetLayerDefn().GetFieldCount(); i++) {
            fieldInfo.put(layer.GetLayerDefn().GetFieldDefn(i).GetNameRef(), layer.GetLayerDefn().GetFieldDefn(i).GetTypeName());
        }
        result.setFieldInfo(fieldInfo);

        return result;
    }
}
