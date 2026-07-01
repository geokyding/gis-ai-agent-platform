package com.dingky.gis.ai.platform.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * ProjectName: dimageproc
 * ClassName: ConfigLoader
 * Package: com.diit.dimageproc.pointstopg.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/1/5 11:28
 * @Version 1.0
 **/
public class ConfigLoader {
    private static final Properties props = new Properties();
    // logger
    public static Logger logger = LogManager.getLogger(ConfigLoader.class);
    static {
        // 先加载内部配置
        try(InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            logger.info("加载内部配置文件: {}", "application.properties");
            if (input != null) {
                props.load(input);
            }
        }catch (IOException e){
            throw new RuntimeException("无法加载内部配置",e);
        }
        // 加载外部配置（并覆盖掉内部配置）
        String[] externalPaths = {
                "./config/application.properties",
                "./application.properties",
                System.getProperty("config.file") // 支持 -Dconfig.file=/path/to/app.properties
        };
        for (String path : externalPaths) {
            if (path == null){
                continue;
            }
            Path externalPath = Paths.get(path);
            if (externalPath.toFile().exists()) {
                try(InputStream input = Files.newInputStream(externalPath)) {
                    Properties externalProps = new Properties();
                    externalProps.load(input);
                    // 覆盖默认值
                    props.putAll(externalProps);
                    logger.info("加载外部配置文件: {}", externalPath);
                    break;
                }catch (IOException e){
                    logger.error("无法加载外部配置文件: {}", externalPath);
                }
            }
        }
    }

    public static String getProperty(String key){
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue){
        return props.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key){
        String value = props.getProperty(key);
        if (value == null){
            throw new RuntimeException("配置项不存在: " + key);
        }
        return Integer.parseInt(value.trim());
    }

    public static int getIntProperty(String key, int defaultValue){
        String value = props.getProperty(key);
        if (value == null){
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

}
