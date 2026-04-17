package com.dingky.gis.ai.platform.worker.service;

import com.dingky.gis.ai.platform.common.model.FieldDef;
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
                .append("geom geometry");

        for (FieldDef f : fields) {
            sql.append(", \"")
                    .append(f.getName())
                    .append("\" ")
                    .append(f.getTypeName());
        }

        sql.append(")");

        jdbcTemplate.execute(sql.toString());

        // 👉 建空间索引（只建一次）
        createSpatialIndex(tableName);
    }

    private void createSpatialIndex(String tableName) {
        String indexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_geom " +
                "ON \"" + tableName + "\" USING GIST (geom)";

        jdbcTemplate.execute(indexSql);
    }
}
