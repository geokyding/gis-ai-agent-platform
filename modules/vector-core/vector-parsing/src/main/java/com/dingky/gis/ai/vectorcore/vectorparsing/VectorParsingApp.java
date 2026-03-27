package com.dingky.gis.ai.vectorcore.vectorparsing;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ProjectName: gis-ai-agent-platform
 * ClassName: VectorParsingApp
 * Package: com.dingky.gis.ai.vectorcore.vectorparsing
 * Description:
 *
 * @Author: ding
 * @Create 2026/3/17 15:36
 * @Version 1.0
 **/
@SpringBootApplication(scanBasePackages = {"com.dingky.gis.ai.vectorcore.vectorparsing"})
public class VectorParsingApp {
    public static void main(String[] args){
        SpringApplication.run(VectorParsingApp.class, args);
    }
}
