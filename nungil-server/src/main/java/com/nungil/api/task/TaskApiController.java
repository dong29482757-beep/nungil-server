package com.nungil.api.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.domain.task.service.TaskService;
import com.nungil.domain.task.vo.TaskVO;
import com.nungil.infrastructure.external.google.GeminiRestAdapter;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task")
public class TaskApiController {

    private final TaskService taskService;
    private final GeminiRestAdapter geminiRestAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskApiController(TaskService taskService, GeminiRestAdapter geminiRestAdapter) {
        this.taskService = taskService;
        this.geminiRestAdapter = geminiRestAdapter;
    }

    /**
     * 과업 검색 (Gemini 유사어 매칭)
     * GET /api/task/search?name=빨래
     *
     * 1. DB에서 전체 과업 목록 조회
     * 2. 목록 + 사용자 입력을 Gemini 프롬프트로 전송
     * 3. Gemini가 가장 유사한 항목명 반환
     * 4. 해당 항목의 TaskVO 찾아서 응답
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam("name") String name) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. DB에서 전체 과업 목록 조회
            List<TaskVO> allTasks = taskService.findAll();

            if (allTasks == null || allTasks.isEmpty()) {
                response.put("found", false);
                response.put("taskId", null);
                response.put("taskName", null);
                return response;
            }

            // 2. 과업 이름 목록 문자열 생성
            String taskListStr = allTasks.stream()
                    .map(TaskVO::getName)
                    .collect(Collectors.joining(", "));

            // 3. Gemini 프롬프트 구성
            String prompt = "다음 과업 목록 중에서 '" + name + "'과 가장 유사한 항목을 딱 1개만 골라줘.\n"
                    + "반드시 목록에 있는 항목 이름만 정확히 반환해줘. 다른 설명은 하지 마.\n"
                    + "목록: " + taskListStr;

            // 4. Gemini 호출 (이미지 없이 텍스트만)
            String geminiAnswer = geminiRestAdapter.sendRequest(prompt, null, null);

            if (geminiAnswer == null || geminiAnswer.isBlank()) {
                response.put("found", false);
                response.put("taskId", null);
                response.put("taskName", null);
                return response;
            }

            String matched = geminiAnswer.trim();

            // 5. Gemini 응답에서 일치하는 TaskVO 찾기
            TaskVO matchedTask = allTasks.stream()
                    .filter(t -> matched.contains(t.getName()))
                    .findFirst()
                    .orElse(null);

            if (matchedTask != null) {
                response.put("found", true);
                response.put("taskId", matchedTask.getTaskId());
                response.put("taskName", matchedTask.getName());
            } else {
                response.put("found", false);
                response.put("taskId", null);
                response.put("taskName", null);
            }

        } catch (Exception e) {
            response.put("found", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 과업 단계 조회
     * GET /api/task/{taskId}/steps
     * TASK.process 컬럼에 JSON 배열로 저장된 단계 반환
     * 예) [{"order":1,"description":"세탁물을 모아요"}, ...]
     */
    @GetMapping("/{taskId}/steps")
    public Map<String, Object> getSteps(@PathVariable("taskId") Long taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            TaskVO task = taskService.findById(taskId);
            if (task == null) {
                response.put("steps", Collections.emptyList());
                return response;
            }

            List<?> steps = Collections.emptyList();
            if (task.getProcess() != null && !task.getProcess().trim().isEmpty()) {
                steps = objectMapper.readValue(task.getProcess(), List.class);
            }

            response.put("taskId", task.getTaskId());
            response.put("taskName", task.getName());
            response.put("steps", steps);
        } catch (Exception e) {
            response.put("steps", Collections.emptyList());
            response.put("message", e.getMessage());
        }
        return response;
    }
}
