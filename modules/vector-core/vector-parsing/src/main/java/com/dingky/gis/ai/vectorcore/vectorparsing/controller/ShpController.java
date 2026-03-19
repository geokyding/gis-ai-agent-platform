package com.dingky.gis.ai.vectorcore.vectorparsing.controller;

import com.dingky.gis.ai.vectorcore.vectorparsing.dto.request.ShpParseRequest;
import com.dingky.gis.ai.vectorcore.vectorparsing.dto.response.ShpParseResponse;
import com.dingky.gis.ai.vectorcore.vectorparsing.service.ShpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: ShpController
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing.controller
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/17 16:57
 * @Version 1.0
 **/
@RestController
@RequestMapping("/vector/shp")
public class ShpController {
    private static Logger log = LogManager.getLogger(ShpController.class);
    private final ShpService shpService;
    public ShpController(ShpService shpService) {
        log.info("ShpController 初始化完成");
        this.shpService = shpService;
    }
    @Autowired
    private Environment env;

//    @PostMapping(value = "/parse", consumes = "application/json") // consumes 精确匹配请求类型。
//    public ResponseEntity<Map<String, Object>> parseJson(@RequestBody Map<String, Object> params){
//        return parse((String) params.get("path"));
//    }
    /**
     * JSON 请求（推荐生产使用）
     * Content-Type: application/json
     */
    @PostMapping(value = "/parse", consumes = "application/json")
    public ResponseEntity<?> parseJson(@Validated @RequestBody ShpParseRequest params){
        log.info("json 请求: /vector/shp/parse");
        return parse(params.getPath());
    }

    /**
     * 表单请求（用于浏览器 form 测试）
     * Content-Type: application/x-www-form-urlencoded
     */
    @PostMapping(value = "/parse", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> parseForm(@RequestParam("path") String path){
        log.info("浏览器form请求: /vector/shp/parse?path={}", path);
        return parse(path);
    }

    public ResponseEntity<?> parse(String path){
        if (path == null || path.isEmpty()){
            return ResponseEntity.badRequest().body("参数错误: 文件路径不能为空");
        }
        try{
            log.info("开始解析: {}", path);
            ShpParseResponse parseResult = shpService.parse(path);
            // 用来确认请求打到哪个实例
            Map<String, Object> result = new HashMap<>();
            result.put("port", env.getProperty("server.port"));
            result.put("data", parseResult);
            return ResponseEntity.ok(result);
        }catch (Exception e){
            log.error("解析失败: {}", e.getMessage());
            return ResponseEntity.status(500).body("解析失败: " + e.getMessage());
        }
    }

//    public ResponseEntity<Map<String, Object>> parse(String path){
//        Map<String, Object> result = new HashMap<>();
//
//        try{
//            if (path == null || path.isEmpty()){
//                log.error("参数错误");
//                result.put("message", "参数错误: 文件路径不能为空");
//                result.put("code", 400);
//                return ResponseEntity.badRequest().body(result);
//            }
//            log.info("开始解析: {}", path);
//            Map<String, Object> parseResult = shpService.parse(path);
//            result.put("message", "解析成功");
//            result.put("code", 200);
//            result.put("data", parseResult);
//            return ResponseEntity.ok(result);
//        }catch (Exception e){
//            log.error("解析失败: {}", e.getMessage());
//            result.put("message", "解析失败: " + e.getMessage());
//            result.put("code", 500);
//            result.put("data", null);
//            return ResponseEntity.status(500).body(result);
//        }
//
//    }

//    @GetMapping("/parse")
//    public Map<String, Object> parse(@RequestParam String path){
//        Map<String, Object> result = new HashMap<>();
//        if (path == null || path.isEmpty()){
//            log.error("参数错误");
//            result.put("message", "参数错误: 文件路径不能为空");
//            result.put("code", 400);
//            return result;
//        }
//        log.info("开始解析: {}", path);
//        Map<String, Object> parseResult = shpService.parse(path);
//        result.put("message", "解析成功");
//        result.put("code", 200);
//        result.put("data", parseResult);
//        return result;
//    }

}
