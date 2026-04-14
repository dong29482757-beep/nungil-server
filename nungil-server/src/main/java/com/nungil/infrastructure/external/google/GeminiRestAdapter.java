package com.nungil.infrastructure.external.google;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiRestAdapter { // [변경] ApiClient -> RestAdapter (역할 명확화)

    @Value("${google.ai.api-key}")
    private String apiKey;

    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1";

    public String sendRequest(String prompt, String base64Image, String contentType){
        RestTemplate restTemplate = new RestTemplate();
        // 2.5-flash 모델 경로 유지
        String finalUrl = BASE_URL + "/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // 텍스트 프롬프트 설정
        parts.add(Map.of("text", prompt != null ? prompt : "이 사진에 대해 설명해줘."));

        // 이미지 데이터가 있으면 추가
        if (base64Image != null && !base64Image.isEmpty()) {
            parts.add(Map.of("inline_data", Map.of(
                // 고정된 "image/jpeg" 대신 contentType 변수 사용
                "mime_type", (contentType != null) ? contentType : "image/jpeg",
                "data", base64Image
            )));
        }

        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        try {
            Map<String, Object> response = restTemplate.postForObject(finalUrl, requestBody, Map.class);
            return parseResponse(response);
        } catch (Exception e) {
            // [진국 포인트] 에러 발생 시 시스템이 멈추지 않도록 로그를 남기고 빈 JSON 형태 반환
            System.err.println("[Gemini Error] " + e.getMessage());
            return "{\"answer\": \"AI 통신 중 오류가 발생했습니다.\", \"suggestedQuestions\": []}";
        }
    }

    private String parseResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "{\"answer\": \"응답 형식이 올바르지 않습니다.\", \"suggestedQuestions\": []}";
        }
    }
}