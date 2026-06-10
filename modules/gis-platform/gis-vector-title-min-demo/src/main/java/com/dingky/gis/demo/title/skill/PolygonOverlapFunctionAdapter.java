package com.dingky.gis.demo.title.skill;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Function Calling 适配器
 * 
 * 将 AI Agent 的 Function Calling 请求转换为 Skill 执行
 */
public class PolygonOverlapFunctionAdapter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final PolygonOverlapSkill skill;
    
    public PolygonOverlapFunctionAdapter() {
        this.skill = new PolygonOverlapSkill();
    }
    
    /**
     * 执行 Function Calling
     * 
     * @param arguments Function 参数（JSON 格式）
     * @return 执行结果（JSON 格式）
     * @throws Exception 解析或执行异常
     */
    public String execute(Map<String, Object> arguments) throws Exception {
        // Step 1: 解析参数
        OverlapAnalysisRequest request = parseArguments(arguments);
        
        // Step 2: 执行 Skill
        OverlapAnalysisResponse response = skill.execute(request);
        
        // Step 3: 序列化结果
        return objectMapper.writeValueAsString(response);
    }
    
    /**
     * 从 Map 中解析请求参数
     * 
     * @param arguments 参数 Map
     * @return 重叠分析请求
     */
    private OverlapAnalysisRequest parseArguments(Map<String, Object> arguments) {
        OverlapAnalysisRequest request = new OverlapAnalysisRequest();
        
        // 解析 polygonSetA
        if (arguments.containsKey("polygonSetA")) {
            request.setPolygonSetA(parsePolygonSet((Map<?, ?>) arguments.get("polygonSetA")));
        } else {
            throw new IllegalArgumentException("缺少必需参数: polygonSetA");
        }
        
        // 解析 polygonSetB
        if (arguments.containsKey("polygonSetB")) {
            request.setPolygonSetB(parsePolygonSet((Map<?, ?>) arguments.get("polygonSetB")));
        } else {
            throw new IllegalArgumentException("缺少必需参数: polygonSetB");
        }
        
        // 解析 includeDetails（可选）
        if (arguments.containsKey("includeDetails")) {
            request.setIncludeDetails((Boolean) arguments.get("includeDetails"));
        }
        
        return request;
    }
    
    /**
     * 解析多边形集合
     * 
     * @param polygonData 多边形数据 Map
     * @return 多边形输入列表
     */
    @SuppressWarnings("unchecked")
    private java.util.List<PolygonInput> parsePolygonSet(Map<?, ?> polygonData) {
        java.util.List<PolygonInput> polygons = new java.util.ArrayList<>();
        
        // 假设数据格式为: { "0": {...}, "1": {...} } 或 [ {...}, {...} ]
        if (polygonData instanceof java.util.List) {
            for (Object item : (java.util.List<?>) polygonData) {
                polygons.add(parsePolygonInput((Map<?, ?>) item));
            }
        } else {
            // Map 格式
            for (Object key : polygonData.keySet()) {
                polygons.add(parsePolygonInput((Map<?, ?>) polygonData.get(key)));
            }
        }
        
        return polygons;
    }
    
    /**
     * 解析单个多边形输入
     * 
     * @param data 多边形数据
     * @return 多边形输入对象
     */
    @SuppressWarnings("unchecked")
    private PolygonInput parsePolygonInput(Map<?, ?> data) {
        PolygonInput input = new PolygonInput();
        
        // 解析 fid
        if (data.containsKey("fid")) {
            Object fidValue = data.get("fid");
            if (fidValue instanceof Number) {
                input.setFid(((Number) fidValue).longValue());
            } else {
                input.setFid(Long.parseLong(fidValue.toString()));
            }
        }
        
        // 解析 code
        if (data.containsKey("code")) {
            input.setCode(data.get("code").toString());
        }
        
        // 解析 bounds
        if (data.containsKey("bounds")) {
            input.setBounds(parseBoundingBox((Map<?, ?>) data.get("bounds")));
        }
        
        return input;
    }
    
    /**
     * 解析边界框
     * 
     * @param boundsData 边界框数据
     * @return 边界框对象
     */
    private BoundingBox parseBoundingBox(Map<?, ?> boundsData) {
        double minX = ((Number) boundsData.get("minX")).doubleValue();
        double minY = ((Number) boundsData.get("minY")).doubleValue();
        double maxX = ((Number) boundsData.get("maxX")).doubleValue();
        double maxY = ((Number) boundsData.get("maxY")).doubleValue();
        
        return new BoundingBox(minX, minY, maxX, maxY);
    }
    
    /**
     * 获取 Function 定义（用于 OpenAI Function Calling）
     * 
     * @return Function 定义的 JSON 字符串
     */
    public String getFunctionDefinition() {
        return "{\n" +
                "  \"name\": \"polygon_overlap_analysis\",\n" +
                "  \"description\": \"执行两个多边形集合的重叠分析，自动检测和转换四叉树编码\",\n" +
                "  \"parameters\": {\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"polygonSetA\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"description\": \"第一个多边形集合\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"fid\": { \"type\": \"integer\", \"description\": \"要素唯一标识符\" },\n" +
                "            \"code\": { \"type\": \"string\", \"description\": \"编码（可以是任意格式）\" },\n" +
                "            \"bounds\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"minX\": { \"type\": \"number\" },\n" +
                "                \"minY\": { \"type\": \"number\" },\n" +
                "                \"maxX\": { \"type\": \"number\" },\n" +
                "                \"maxY\": { \"type\": \"number\" }\n" +
                "              },\n" +
                "              \"required\": [\"minX\", \"minY\", \"maxX\", \"maxY\"]\n" +
                "            }\n" +
                "          },\n" +
                "          \"required\": [\"fid\", \"code\", \"bounds\"]\n" +
                "        }\n" +
                "      },\n" +
                "      \"polygonSetB\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"description\": \"第二个多边形集合\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"fid\": { \"type\": \"integer\" },\n" +
                "            \"code\": { \"type\": \"string\" },\n" +
                "            \"bounds\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"minX\": { \"type\": \"number\" },\n" +
                "                \"minY\": { \"type\": \"number\" },\n" +
                "                \"maxX\": { \"type\": \"number\" },\n" +
                "                \"maxY\": { \"type\": \"number\" }\n" +
                "              },\n" +
                "              \"required\": [\"minX\", \"minY\", \"maxX\", \"maxY\"]\n" +
                "            }\n" +
                "          },\n" +
                "          \"required\": [\"fid\", \"code\", \"bounds\"]\n" +
                "        }\n" +
                "      },\n" +
                "      \"includeDetails\": {\n" +
                "        \"type\": \"boolean\",\n" +
                "        \"description\": \"是否包含详细信息（默认 true）\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"required\": [\"polygonSetA\", \"polygonSetB\"]\n" +
                "  }\n" +
                "}";
    }
}
