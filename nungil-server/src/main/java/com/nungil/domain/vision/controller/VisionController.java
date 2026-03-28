package com.nungil.domain.vision.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nungil.domain.vision.service.VisionService;

@RestController
@RequestMapping("/api/v1/vision")
public class VisionController {

    private final VisionService visionService;

    public VisionController(VisionService visionService) {
        this.visionService = visionService;
    }

    @PostMapping("/ask")
    public Map<String, Object> askVision(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "question", required = false) String question) {
        
        Map<String, Object> response = new HashMap<>();
        
      
        if (file != null && !file.isEmpty() && question != null && !question.isEmpty()) {
            System.out.println("✅ 사진과 질문이 모두 도착함");
        } else if (file != null && !file.isEmpty()) {
            System.out.println("📸 사진만 도착함");
        } else if (question != null && !question.isEmpty()) {
            System.out.println("💬 질문만 도착함");
        } else {
            // 둘 다 없는 경우에 대한 방어 코드
            response.put("status", "FAIL");
            response.put("message", "데이터가 아무것도 전송되지 않았습니다.");
            return response;
        }

        // 2. 서비스 호출 (서비스에서도 null 처리가 되어야 함)
        String aiResult = visionService.analyze(file, question);

        response.put("status", "SUCCESS");
        response.put("result", aiResult);
        
        // 파일이 있을 때만 파일명 리턴
        if (file != null) {
            response.put("fileName", file.getOriginalFilename());
        }

        return response;
    }
    
    
}