package com.nungil.domain.analysis.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.infrastructure.external.google.GeminiRestAdapter;
import com.nungil.infrastructure.external.google.GoogleSttClient;

@Service
public class AnalysisOrchestrator {

    private final GoogleSttClient sttClient;
    private final GeminiRestAdapter geminiAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisOrchestrator(GoogleSttClient sttClient, GeminiRestAdapter geminiAdapter) {
        this.sttClient = sttClient;
        this.geminiAdapter = geminiAdapter;
    }

    /**
     * 눈길 프로젝트의 핵심 파이프라인: 음성/이미지/텍스트를 받아서 최종 분석 결과를 반환
     */
    public Map<String, Object> execute(String userId, MultipartFile voiceFile, MultipartFile imageFile, String textPrompt) {
        try {
            // 1. 음성 데이터 처리
            String finalQuestion = (voiceFile != null && !voiceFile.isEmpty()) 
                    ? sttClient.transcribe(voiceFile) 
                    : textPrompt;

            if (finalQuestion == null || finalQuestion.trim().isEmpty()) {
                finalQuestion = "....";
            }

            // 2. 이미지 데이터 및 타입 처리 (여기가 핵심!)
            String base64Image = null;
            String contentType = "image/jpeg"; // 기본값 설정

            if (imageFile != null && !imageFile.isEmpty()) {
                // 파일을 바이트로 변환 후 Base64 인코딩
                base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
                // [진국 포인트] 파일의 실제 MIME 타입을 추출 (예: image/webp, image/png 등)
                contentType = imageFile.getContentType(); 
                
                System.out.println("[Debug] 업로드된 파일 타입: " + contentType);
            }

            // 3. AI 분석 (프롬프트 구성 및 Gemini 호출)
            String prompt = String.format("""
                너는 시각 보조 AI '똘똘'이야. 지적 장애 사용자를 위해 아주 쉽고 친절하게 설명해줘.
                사용자 질문: "%s"
                반드시 아래 JSON 형식으로만 응답해:
                {
                  "answer": "친절한 설명 내용",
                  "suggestedQuestions": ["짧은 질문1", "짧은 질문2", "짧은 질문3"]
                }
                """, finalQuestion);

            // [수정 완료] 이제 contentType 변수가 선언되었으므로 에러 없이 전달됨
            String rawResponse = geminiAdapter.sendRequest(prompt, base64Image, contentType);
            
            // 4. 응답 가공 및 JSON 파싱
            String cleanJson = rawResponse.replaceAll("```json|```", "").trim();
            Map<String, Object> aiResult = objectMapper.readValue(cleanJson, Map.class);

            // 5. 최종 결과 조립 (유저 정보 및 변환된 텍스트 포함)
            Map<String, Object> finalResult = new HashMap<>(aiResult);
            finalResult.put("userId", userId);
            finalResult.put("transcribedText", finalQuestion);
            
            return finalResult;

        } catch (Exception e) {
            System.err.println("[Orchestrator Error] " + e.getMessage());
            return createErrorResponse("분석 중 오류가 발생했습니다.");
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("answer", message);
        errorMap.put("suggestedQuestions", java.util.Arrays.asList("다시 시도해볼까요?"));
        return errorMap;
    }
}