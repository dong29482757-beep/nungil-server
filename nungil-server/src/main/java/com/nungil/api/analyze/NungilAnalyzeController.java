package com.nungil.api.analyze;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.domain.analysis.service.AnalysisOrchestrator;

@RestController
@RequestMapping("/api/v1/nungil")
public class NungilAnalyzeController {

    private final AnalysisOrchestrator orchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NungilAnalyzeController(AnalysisOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * [통합 분석 API]
     * 안드로이드에서 음성, 이미지, 텍스트 중 있는 것만 실어서 보내면 됨.
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyze(
            @RequestPart(value = "info", required = false) String infoJson, // userId 등이 담긴 JSON
            @RequestPart(value = "voice", required = false) MultipartFile voiceFile,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "text", required = false) String textPrompt) {

        Map<String, Object> response = new HashMap<>();
        String userId = "unknown";

        try {
            // 1. 사용자 정보 파싱 (필요한 경우)
            if (infoJson != null && !infoJson.isEmpty()) {
                Map<String, Object> info = objectMapper.readValue(infoJson, Map.class);
                userId = String.valueOf(info.getOrDefault("id", "unknown"));
            }

            // 2. 오케스트레이터 호출 (모든 비즈니스 로직은 여기서 처리)
            Map<String, Object> result = orchestrator.execute(userId, voiceFile, imageFile, textPrompt);

            response.put("status", "SUCCESS");
            response.put("result", result);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", "요청 처리 중 오류 발생: " + e.getMessage());
            return response;
        }
    }
}