package com.test.java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.nungil.infrastructure.external.google.GeminiRestAdapter;

@SpringJUnitConfig(locations = {
    "file:src/main/webapp/WEB-INF/spring/root-context.xml",
    "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"
})
// 이제 .properties로 바꿨으니까 경로도 맞춰주자!
@TestPropertySource(locations = "classpath:application.properties")
public class GeminiConnectionTest {

    @Autowired
    private GeminiRestAdapter geminiApiClient;

    // 설정 파일에서 키를 잘 읽어오는지 확인용
    @Value("${google.ai.api-key:KEY_NOT_FOUND}")
    private String testApiKey;

    @Test
    void 최종_연결_및_금고생성_확인() {
        System.out.println("\n======= 🔍 [Nungil Project] 최종 연결 디버깅 =======");

        // 1. 설정값 로드 확인
        System.out.println("[1단계] API KEY 로드 상태: [" + testApiKey + "]");
        
        if ("KEY_NOT_FOUND".equals(testApiKey)) {
            System.err.println("🚨 [에러] 아직도 키를 못 읽고 있어. application.properties 확인해봐!");
            return;
        }

        // 2. 실제 구글 서버와 통신 (금고 생성 시도)
        try {
            System.out.println("[2단계] 구글 서버에 'nungil-manual-store' 생성 요청 중...");
            
            // GeminiApiClient에 만든 메서드 호출
            String storeName = geminiApiClient.createNungilStore("nungil-manual-store");
            
            System.out.println("------------------------------------------------");
            if (storeName != null && storeName.contains("fileSearchStores")) {
                System.out.println("🎉 [성공] 구글 서버 연결 및 금고 생성 완료!");
                System.out.println("📍 생성된 금고 ID: " + storeName);
                System.out.println("👉 이 ID를 복사해서 따로 적어둬. 나중에 파일 업로드할 때 써야 해.");
            } else {
                System.err.println("❌ [실패] 응답 메시지: " + storeName);
            }
            System.out.println("------------------------------------------------");
            
        } catch (Exception e) {
            System.err.println("🚨 [치명적 에러] 실행 중 예외 발생:");
            e.printStackTrace();
        }

        System.out.println("======= 🚀 디버깅 종료 =======\n");
    }
}