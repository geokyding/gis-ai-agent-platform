package com.dingky.gis.ai.platform.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: FieldDef
 * Package: com.dingky.gis.ai.platform.common.model
 * Description:
 *
 * @Author: ding
 * @Create 2026/4/16 13:52
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldDef {
    private String name;
    private String typeName;
}
