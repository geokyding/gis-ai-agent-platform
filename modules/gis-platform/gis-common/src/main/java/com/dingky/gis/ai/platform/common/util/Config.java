package com.dingky.gis.ai.platform.common.util;

/**
 * ProjectName: dimageproc
 * ClassName: DBConfig
 * Package: com.diit.dimageproc.pointstopg.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/1/5 11:02
 * @Version 1.0
 **/
public class Config {
    // "jdbc:postgresql://citusdb-0.admin.svc.cluster.local/data-storage"
    public static final String DB_URL = ConfigLoader.getProperty("db.url","jdbc:postgresql://citusdb-0.admin.svc.cluster.local/data-storage");
    public static final String DB_USER = ConfigLoader.getProperty("db.user","postgres");
    public static final String DB_PASSWORD = ConfigLoader.getProperty("db.password","zalando");
    // 目标SRID
    public static final int TARGET_SRID = 4326;
    // 省市边界数据库信息
    public static final String IMAGE_DB_URL = ConfigLoader.getProperty("db.boundary.url","jdbc:postgresql://citusdb-0.admin.svc.cluster.local:5432/image");
    public static final String IMAGE_DB_USER = ConfigLoader.getProperty("db.boundary.user","postgres");
    public static final String IMAGE_DB_PASSWORD = ConfigLoader.getProperty("db.boundary.password","zalando");

    public static final String TABLENAME = ConfigLoader.getProperty("db.tablename","ground_control_point_3d");

    public static final int BATCH_SIZE = ConfigLoader.getIntProperty("db.batch.size", 2000);

}
