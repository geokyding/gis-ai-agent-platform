package com.dingky.gis.ai.platform.worker.service;

import com.dingky.gis.ai.platform.common.model.FieldDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: TableService
 * Package: com.dingky.gis.ai.platform.worker.service
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/16 13:47
 * @Version 1.0
 **/
@Service
@Slf4j
public class TableService {
    private final JdbcTemplate jdbcTemplate;
    public TableService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void createTableIfNotExists(String tableName, List<FieldDef> fields){
        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE IF NOT EXISTS \"")
                .append(tableName)
                .append("\" (")
                .append("id SERIAL PRIMARY KEY, ")
                .append("fid VARCHAR(100) UNIQUE, ")  // ← 添加 fid 列并设置唯一约束
                .append("geom geometry");  // ← 不指定 SRID，接受任意坐标系

        for (FieldDef f : fields) {
            // 跳过 fid，因为已经在上面定义了
            if ("fid".equalsIgnoreCase(f.getName())) {
                continue;
            }
            sql.append(", \"")
                    .append(f.getName())
                    .append("\" ")
                    .append(f.getTypeName());
        }

        sql.append(")");

        jdbcTemplate.execute(sql.toString());

        // 👉 建空间索引（只建一次）
        createSpatialIndex(tableName);
        
        log.info("✅ 表 {} 创建成功，fid 列已设置唯一约束，geom 接受任意坐标系", tableName);
    }

    private void createSpatialIndex(String tableName) {
        String indexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_geom " +
                "ON \"" + tableName + "\" USING GIST (geom)";

        jdbcTemplate.execute(indexSql);
    }
}
