package com.nungil.domain.vision.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VisionService {
    public String analyze(MultipartFile file, String question) {
        if (file == null && question != null) {
            return "텍스트 질문 분석: " + question;
        } else if (file != null && question == null) {
            return "사진 분석 중... (파일명: " + file.getOriginalFilename() + ")";
        } else {
            return "사진과 질문 복합 분석: " + question;
        }
    }
}
