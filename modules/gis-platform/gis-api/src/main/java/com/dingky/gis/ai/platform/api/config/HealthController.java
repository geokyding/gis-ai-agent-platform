package com.dingky.gis.ai.platform.api.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: HealthController
 * Package: com.dingky.gis.ai.platform.api.config
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/31 17:48
 * @Version 1.0
 **/
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/check")
    public Map<String , Object> check(){
        Map<String, Object> result = new HashMap<>();
        result.put("status", "good");
        result.put("message", "启动成功^_^");
        return result;
    }
}
