package com.dingky.gis.ai.vectorcore.vectorparsing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: HealthController
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing.controller
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/18 15:41
 * @Version 1.0
 **/
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping("/check")
    public Map<String , Object> check(){
        Map<String, Object> result = new HashMap<>();
        result.put("status", "good");
        return result;
    }
}
