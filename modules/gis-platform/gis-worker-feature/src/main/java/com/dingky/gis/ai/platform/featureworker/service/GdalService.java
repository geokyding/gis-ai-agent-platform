package com.dingky.gis.ai.platform.featureworker.service;

import com.dingky.gis.ai.platform.common.model.FeatureDTO;
import lombok.extern.slf4j.Slf4j;
import org.gdal.ogr.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<FeatureDTO> readFeature(String filePath, String layerName, int offSet, int limit) {
        List<FeatureDTO> result = new ArrayList<>();
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource == null) {
            log.error("无法打开文件：{}", filePath);
            throw new RuntimeException("无法打开文件：" + filePath);
        }
        Layer layer = dataSource.GetLayer(layerName);
        layer.SetNextByIndex(offSet);
        int count = 0;
        Feature feature;
        while ((feature = layer.GetNextFeature()) != null && count < limit){
            long fid = feature.GetFID();
            Geometry geometry = feature.GetGeometryRef();
            byte[] wkb = geometry.ExportToWkb();
            Map<String, Object> properties = featurePropsExport(feature);
//            List<String> fieldNames = getFieldNames(feature);
            FeatureDTO featureDTO = new FeatureDTO(fid, wkb, properties);
            result.add(featureDTO);
            feature.delete();
            count++;
        }

//        for (int i = 0; i < limit; i++) {
//            Feature feature = layer.GetNextFeature();
//            if (feature == null) break;
//            long fid = feature.GetFID();
//            FeatureDTO featureDTO = new FeatureDTO(fid, feature.GetGeometryRef().ExportToWkb(), featureExportToJson(feature));
//            result.add(featureDTO);
//            feature.delete();
//        }

        layer.delete();
        dataSource.delete();
        return result;
    }

    private List<String> getFieldValues(Feature feature) {
        if (feature == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < feature.GetFieldCount(); i++) {
            String v = feature.GetFieldAsString(i);
            if (v == null || v.isEmpty()){
                result.add(null);
            }else {
                result.add(feature.GetFieldAsString(i));
            }

        }
        return result;
    }

    private Map<String, Object> featurePropsExport(Feature  feature) {
        // 创建json对象
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode result = mapper.createObjectNode();
        Map<String, Object> row = new HashMap<>();
        // 获取字段值
        for (int i = 0; i < feature.GetFieldCount(); i++) {
            log.debug("字段索引: {}, 字段名: {}, 字段值: {}", i, feature.GetFieldDefnRef(i).GetNameRef(), feature.GetFieldAsString(i));
            // 获取fid
            long fid = feature.GetFID();
            row.put("fid", fid);
            String fieldName = feature.GetFieldDefnRef(i).GetNameRef();
            String fieldValue = feature.GetFieldAsString(i);
            int fieldType = feature.GetFieldDefnRef(i).GetType();
            if (!feature.IsFieldSet(i)){
                row.put(fieldName, null);
                continue;
            }
            switch (fieldType) {

                case ogr.OFTInteger:
                case ogr.OFTInteger64:
                    row.put(fieldName, feature.GetFieldAsInteger64(i));
                    break;

                case ogr.OFTReal:
                    row.put(fieldName, feature.GetFieldAsDouble(i));
                    break;

                case ogr.OFTString:
                    row.put(fieldName, fieldValue);
                    break;

                case ogr.OFTDate:
                    row.put(fieldName, parseDate(feature, i));
                    break;

                case ogr.OFTTime:
                    row.put(fieldName, parseTime(feature, i));
                    break;

                case ogr.OFTDateTime:
                    row.put(fieldName, parseDateTime(feature, i));
                    break;

                default:
                    // 兜底类型
                    row.put(fieldName, fieldValue);
            }
        }
        return row;
    }

    /**
     * 解析日期字段
     */
    private String parseDate(Feature feature, int fieldIndex) {
        try {
            int[] year = new int[1];
            int[] month = new int[1];
            int[] day = new int[1];
            feature.GetFieldAsDateTime(fieldIndex, year, month, day, new int[1], new int[1], new float[1], new int[1]);

//            return LocalDate.of(year[0], month[0], day[0]);
//            return String.format("%04d-%02d-%02d", year[0], month[0], day[0]);
            if (year[0] == 0 || month[0] == 0 || day[0] == 0) {
                log.debug("检测到无效日期字段(值为0),返回null");
                return null;
            }

            try {
                LocalDate date = LocalDate.of(year[0], month[0], day[0]);
                return date.toString();
            } catch (Exception e) {
                log.warn("日期值无效: {}-{}-{}, 返回null", year[0], month[0], day[0]);
                return null;
            }
        } catch (Exception e) {
            log.warn("解析日期字段失败，使用字符串值: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析时间字段
     */
    private String parseTime(Feature feature, int fieldIndex) {
        try {
            int[] hour = new int[1];
            int[] minute = new int[1];
            float[] second = new float[1];
            feature.GetFieldAsDateTime(fieldIndex, new int[1], new int[1], new int[1], hour, minute, second, new int[1]);
//            return String.format("%02d:%02d:%02d", hour[0], minute[0], (int) second[0]);
            if (hour[0] == 0 && minute[0] == 0 && second[0] == 0) {
                log.debug("检测到无效时间字段(值为0),返回null");
                return null;
            }

            try {
                LocalTime time = LocalTime.of(hour[0], minute[0], (int) second[0]);
                return time.toString();
            } catch (Exception e) {
                log.warn("时间值无效: {}:{}:{}, 返回null", hour[0], minute[0], (int) second[0]);
                return null;
            }
        } catch (Exception e) {
            log.warn("解析时间字段失败，使用字符串值: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析日期时间字段
     */
    private String parseDateTime(Feature feature, int fieldIndex) {
        try {
            int[] year = new int[1];
            int[] month = new int[1];
            int[] day = new int[1];
            int[] hour = new int[1];
            int[] minute = new int[1];
            float[] second = new float[1];
            int[] tzFlag = new int[1];
            feature.GetFieldAsDateTime(fieldIndex, year, month, day, hour, minute, second, tzFlag);
//            return String.format("%04d-%02d-%02d %02d:%02d:%02d", year[0], month[0], day[0], hour[0], minute[0], (int) second[0]);
            if (year[0] == 0 || month[0] == 0 || day[0] == 0) {
                log.debug("检测到无效日期时间字段(日期为0),返回null");
                return null;
            }

            try {
                LocalDateTime dateTime = LocalDateTime.of(year[0], month[0], day[0], hour[0], minute[0], (int) second[0]);
                return dateTime.toString().replace('T', ' ');
            } catch (Exception e) {
                log.warn("日期时间值无效: {}-{}-{} {}:{}:{}, 返回null",
                        year[0], month[0], day[0], hour[0], minute[0], (int) second[0]);
                return null;
            }
        } catch (Exception e) {
            log.warn("解析日期时间字段失败，使用字符串值: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getFieldNames(String filePath, String layerName) {
        List<String> result = new ArrayList<>();
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource == null) {
            throw new RuntimeException("无法打开文件：" + filePath);
        }
        Layer layer = dataSource.GetLayer(layerName);
        for (int i = 0; i < layer.GetLayerDefn().GetFieldCount(); i++) {
            result.add(layer.GetLayerDefn().GetFieldDefn(i).GetNameRef());
        }
        layer.delete();
        dataSource.delete();
        return result;
    }


    /**
     * 获取字段类型映射（字段名 -> PostgreSQL类型）
     */
    public Map<String, String> getFieldTypes(String filePath, String layerName) {
        Map<String, String> result = new HashMap<>();
        DataSource dataSource = ogr.Open(filePath, 0);
        if (dataSource == null) {
            throw new RuntimeException("无法打开文件：" + filePath);
        }
        Layer layer = dataSource.GetLayer(layerName);
        for (int i = 0; i < layer.GetLayerDefn().GetFieldCount(); i++) {
            FieldDefn fieldDefn = layer.GetLayerDefn().GetFieldDefn(i);
            String fieldName = fieldDefn.GetNameRef();
            int fieldType = fieldDefn.GetType();
            String pgType = convertToPgType(fieldType);
            result.put(fieldName, pgType);
        }
        layer.delete();
        dataSource.delete();
        return result;
    }

    /**
     * 将 GDAL 字段类型转换为 PostgreSQL 类型
     */
    private String convertToPgType(int gdalType) {
        return switch (gdalType) {
            case ogr.OFTInteger -> "integer";
            case ogr.OFTInteger64 -> "bigint";
            case ogr.OFTReal -> "double precision";
            case ogr.OFTString -> "varchar";
            case ogr.OFTDate -> "date";
            case ogr.OFTTime -> "time";
            case ogr.OFTDateTime -> "timestamp";
            default -> "varchar";
        };
    }

}
