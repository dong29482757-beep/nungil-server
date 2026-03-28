package com.nungil.domain.vision.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiApiClient {

    @Value("${google.ai.api-key}")
    private String apiKey;

    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    // 1. 기존: 이미지 분석 요청
    public String sendRequest(String prompt, String base64Image) {
        RestTemplate restTemplate = new RestTemplate();
        String finalUrl = BASE_URL + "/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        parts.add(Map.of("text", prompt != null ? prompt : "이 사진에 대해 설명해줘."));

        if (base64Image != null) {
            parts.add(Map.of("inline_data", Map.of(
                "mime_type", "image/jpeg",
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
            return "구글 AI 통신 실패: " + e.getMessage();
        }
    }

    // --- 2. 추가: 전용 금고(File Search Store) 생성 ---
    public String createNungilStore(String displayName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + "/fileSearchStores?key=" + apiKey;

        Map<String, String> body = new HashMap<>();
        body.put("displayName", displayName);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            String storeName = (String) response.get("name"); // fileSearchStores/xxx 형태
            System.out.println("금고 생성 완료: " + storeName);
            return storeName;
        } catch (Exception e) {
            return "금고 생성 실패: " + e.getMessage();
        }
    }

    // --- 3. 추가: 금고에 크롤링 텍스트 파일 업로드 ---
    public void uploadTextToStore(String storeName, String fileName, String textContent) {
        // 업로드용 URL은 형식이 조금 다름 (v1beta/upload/...)
        String uploadUrl = "https://generativelanguage.googleapis.com/upload/v1beta/" 
                           + storeName + "/files?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 구글 API 문서에 맞춘 파일 데이터 구조
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("display_name", fileName);
        
        // 텍스트를 바로 전송할 때 사용하는 필드
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("file", fileData);
        // 실제 구현 시에는 Multipart 혹은 지정된 스펙에 맞춰 파일 소스를 전달해야 함
        // 여기서는 개념적으로 텍스트 내용을 전달하는 구조를 잡았음
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(uploadUrl, entity, Map.class);
            System.out.println("파일 업로드 요청 성공: " + fileName);
        } catch (Exception e) {
            System.err.println("파일 업로드 실패: " + e.getMessage());
        }
    }

    private String parseResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "응답 해석 중 오류 발생";
        }
    }
}