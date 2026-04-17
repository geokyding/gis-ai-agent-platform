package com.dingky.gis.ai.platform.common.util;

import org.gdal.ogr.ogr;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: PgUtil
 * Package: com.dingky.gis.ai.platform.common.util
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/16 11:26
 * @Version 1.0
 **/
public class PgUtil {
    public static String generateTableName(String baseName, String layerName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            return "gis_" + layerName;
        }
        String raw = baseName + "|" + layerName;
        // 生成 MD5，只取前 16 位
        return "gis_" +  md5Hex(raw).substring(0, 16);
    }

    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
