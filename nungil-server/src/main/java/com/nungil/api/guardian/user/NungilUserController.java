package com.nungil.api.guardian.user;

import com.nungil.domain.task.service.TaskService;
import com.nungil.domain.task.vo.TaskVO;
import com.nungil.domain.user.service.NungilUserService;
import com.nungil.domain.user.vo.NungilUserVO;
import com.nungil.infrastructure.external.google.GeminiRestAdapter;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/guardian")
public class NungilUserController {

    private final NungilUserService nungilUserService;
    private final TaskService taskService;
    private final GeminiRestAdapter geminiRestAdapter;

    public NungilUserController(NungilUserService nungilUserService,
                                TaskService taskService,
                                GeminiRestAdapter geminiRestAdapter) {
        this.nungilUserService = nungilUserService;
        this.taskService = taskService;
        this.geminiRestAdapter = geminiRestAdapter;
    }

    /** 사용자 등록 POST /api/v1/guardian/users */
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String guardianId = (String) body.get("guardianId");
            NungilUserVO user = nungilUserService.createUser(guardianId);

            Map<String, Object> result = new HashMap<>();
            result.put("guardianId", user.getId());
            result.put("idx", user.getIdx());

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 과업 검색 (Gemini 유사어 매칭) GET /api/v1/guardian/tasks/search?item=빨래 */
    @GetMapping("/tasks/search")
    public Map<String, Object> searchTask(@RequestParam("item") String item) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. DB에서 전체 과업 목록 조회
            List<TaskVO> allTasks = taskService.findAll();
            Map<String, Object> result = new HashMap<>();

            if (allTasks == null || allTasks.isEmpty()) {
                result.put("found", false);
                result.put("message", "등록된 과업이 없어요.");
                response.put("status", "SUCCESS");
                response.put("result", result);
                return response;
            }

            // 2. 과업 이름 목록 문자열 생성
            String taskListStr = allTasks.stream()
                    .map(TaskVO::getName)
                    .collect(Collectors.joining(", "));

            // 3. Gemini 프롬프트 구성 및 호출
            String prompt = "다음 과업 목록 중에서 '" + item + "'과 가장 유사한 항목을 딱 1개만 골라줘.\n"
                    + "반드시 목록에 있는 항목 이름만 정확히 반환해줘. 다른 설명은 하지 마.\n"
                    + "목록: " + taskListStr;

            String geminiAnswer = geminiRestAdapter.sendRequest(prompt, null, null);

            // 4. Gemini 응답에서 일치하는 TaskVO 찾기
            TaskVO matchedTask = null;
            if (geminiAnswer != null && !geminiAnswer.isBlank()) {
                String matched = geminiAnswer.trim();
                matchedTask = allTasks.stream()
                        .filter(t -> matched.contains(t.getName()))
                        .findFirst()
                        .orElse(null);
            }

            if (matchedTask != null) {
                result.put("found", true);
                result.put("taskId", matchedTask.getTaskId());
                result.put("item", matchedTask.getName());
                result.put("message", matchedTask.getName() + "를 등록했어요!");
            } else {
                result.put("found", false);
                result.put("message", "아직 " + item + "는 지원하지 않아요. 다른 활동을 알려주세요.");
            }

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 화이트리스트 조회 GET /api/v1/guardian/settings/user/{guardianId}/{idx}/whitelist */
    @GetMapping("/settings/user/{guardianId}/{idx}/whitelist")
    public Map<String, Object> getWhitelist(@PathVariable("guardianId") String guardianId,
                                             @PathVariable("idx") int idx) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> taskIds = nungilUserService.getWhiteList(guardianId, idx);
            List<Map<String, Object>> items = toItemList(taskIds);

            Map<String, Object> result = new HashMap<>();
            result.put("guardianId", guardianId);
            result.put("idx", idx);
            result.put("allowedItems", items);

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 화이트리스트 추가 POST /api/v1/guardian/settings/user/{guardianId}/{idx}/whitelist */
    @PostMapping("/settings/user/{guardianId}/{idx}/whitelist")
    public Map<String, Object> addWhitelist(@PathVariable("guardianId") String guardianId,
                                             @PathVariable("idx") int idx,
                                             @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long taskId = Long.valueOf(body.get("taskId").toString());
            List<Long> taskIds = nungilUserService.addToWhiteList(guardianId, idx, taskId);

            Map<String, Object> result = new HashMap<>();
            result.put("guardianId", guardianId);
            result.put("idx", idx);
            result.put("taskId", taskId);
            result.put("registeredCount", taskIds.size());
            result.put("canComplete", taskIds.size() >= 2);
            result.put("message", taskIds.size() >= 2
                    ? "잘하고 있어요! 더 추가하거나 다음으로 넘어갈 수 있어요."
                    : "하나 더 알려주세요! 최소 2개가 필요해요.");

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (IllegalArgumentException e) {
            response.put("status", "ERROR");
            response.put("errorCode", e.getMessage());
            response.put("message", getErrorMessage(e.getMessage()));
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 화이트리스트 삭제 DELETE /api/v1/guardian/settings/user/{guardianId}/{idx}/whitelist/{taskId} */
    @DeleteMapping("/settings/user/{guardianId}/{idx}/whitelist/{taskId}")
    public Map<String, Object> removeWhitelist(@PathVariable("guardianId") String guardianId,
                                                @PathVariable("idx") int idx,
                                                @PathVariable("taskId") Long taskId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> remaining = nungilUserService.removeFromWhiteList(guardianId, idx, taskId);

            Map<String, Object> result = new HashMap<>();
            result.put("guardianId", guardianId);
            result.put("idx", idx);
            result.put("allowedItems", toItemList(remaining));

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 특이사항 저장 POST /api/v1/guardian/settings/user/{guardianId}/{idx}/profile */
    @PostMapping("/settings/user/{guardianId}/{idx}/profile")
    public Map<String, Object> saveProfile(@PathVariable("guardianId") String guardianId,
                                            @PathVariable("idx") int idx,
                                            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String specialNote = (String) body.get("specialNote");
            nungilUserService.updateSpecialNote(guardianId, idx, specialNote);

            Map<String, Object> result = new HashMap<>();
            result.put("guardianId", guardianId);
            result.put("idx", idx);
            result.put("message", "특이사항이 저장됐어요!");

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 사용자 정보 조회 GET /api/v1/guardian/settings/user/{guardianId}/{idx} */
    @GetMapping("/settings/user/{guardianId}/{idx}")
    public Map<String, Object> getUser(@PathVariable("guardianId") String guardianId,
                                        @PathVariable("idx") int idx) {
        Map<String, Object> response = new HashMap<>();
        try {
            NungilUserVO user = nungilUserService.getUser(guardianId, idx);
            if (user == null) {
                response.put("status", "ERROR");
                response.put("errorCode", "USER_NOT_FOUND");
                return response;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("guardianId", user.getId());
            result.put("idx", user.getIdx());
            result.put("specialNote", user.getSpecialNote());
            result.put("whiteList", parseWhiteListToItems(user.getWhiteList()));

            response.put("status", "SUCCESS");
            response.put("result", result);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    private List<Map<String, Object>> toItemList(List<Long> taskIds) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Long taskId : taskIds) {
            TaskVO task = taskService.findById(taskId);
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", taskId);
            item.put("item", task != null ? task.getName() : "");
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> parseWhiteListToItems(String whiteList) {
        List<Long> ids = new ArrayList<>();
        if (whiteList != null && !whiteList.trim().isEmpty()) {
            for (String s : whiteList.split(",")) {
                try { ids.add(Long.parseLong(s.trim())); } catch (Exception ignored) {}
            }
        }
        return toItemList(ids);
    }

    private String getErrorMessage(String errorCode) {
        switch (errorCode) {
            case "ITEM_EXISTS":       return "이미 등록된 과업입니다";
            case "MAX_ITEM_EXCEEDED": return "과업은 최대 4개까지 등록 가능합니다";
            default:                  return "오류가 발생했습니다";
        }
    }
}
