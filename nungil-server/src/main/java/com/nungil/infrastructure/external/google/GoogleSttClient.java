package com.nungil.infrastructure.external.google;

import java.io.FileInputStream;
import java.io.InputStream;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;

@Component
public class GoogleSttClient { // [변경] Service -> Client (도구임을 명시)

    @Value("${google.cloud.credentials.path}")
    private String credentialsPath;

    private SpeechClient speechClient;

    private void initIfNeeded() throws Exception {
        if (speechClient != null) return;

        System.out.println("[STT] Google Cloud Speech 초기화 중...");
        GoogleCredentials credentials;
        
        try {
            InputStream credStream = new FileInputStream(credentialsPath);
            credentials = GoogleCredentials.fromStream(credStream);
        } catch (Exception e) {
            System.out.println("[STT] 파일 로드 실패, 환경변수 GOOGLE_APPLICATION_CREDENTIALS 사용");
            credentials = GoogleCredentials.getApplicationDefault();
        }

        SpeechSettings settings = SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        this.speechClient = SpeechClient.create(settings);
        System.out.println("[STT] 초기화 완료!");
    }

    /**
     * 음성 파일을 텍스트로 변환 (순수하게 변환 작업만 담당)
     */
    public String transcribe(MultipartFile audioFile) {
        try {
            initIfNeeded();
            
            byte[] audioBytes = audioFile.getBytes();
            ByteString audioData = ByteString.copyFrom(audioBytes);

            com.google.cloud.speech.v1.RecognitionAudio audio = 
                com.google.cloud.speech.v1.RecognitionAudio.newBuilder()
                    .setContent(audioData)
                    .build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.MP3) 
                    .setSampleRateHertz(44100) 
                    .setLanguageCode("ko-KR")
                    .setEnableAutomaticPunctuation(true)
                    .setModel("latest_short") 
                    .build();

            System.out.println("[STT] 음성 인식 시작... (파일 크기: " + audioBytes.length + " bytes)");
            RecognizeResponse response = speechClient.recognize(config, audio);
            
            if (response.getResultsCount() == 0) return "";

            return response.getResults(0).getAlternatives(0).getTranscript();

        } catch (Exception e) {
            System.err.println("[STT] 오류 발생: " + e.getMessage());
            return ""; // 에러 시 빈 문자열 반환 (파이프라인 중단 방지)
        }
    }

    @PreDestroy // 서버 종료 시 스프링이 알아서 호출해줌
    public void shutdown() {
        if (speechClient != null) {
            speechClient.close();
            System.out.println("[STT] 클라이언트 리소스 정리 완료");
        }
    }
}