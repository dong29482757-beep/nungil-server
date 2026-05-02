package com.nungil.api.whitelist;

import com.nungil.domain.task.service.TaskService;
import com.nungil.domain.task.vo.TaskVO;
import com.nungil.domain.user.service.NungilUserService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/whitelist")
public class WhitelistApiController {

    private final NungilUserService nungilUserService;
    private final TaskService taskService;

    public WhitelistApiController(NungilUserService nungilUserService, TaskService taskService) {
        this.nungilUserService = nungilUserService;
        this.taskService = taskService;
    }

    /**
     * 화이트리스트 저장
     * POST /api/whitelist
     * {
     *   "guardianId": "hong123",
     *   "idx": 1,
     *   "taskIds": [1, 2, 3]
     * }
     */
    @PostMapping
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String guardianId = (String) body.get("guardianId");
            int idx = Integer.parseInt(body.get("idx").toString());
            List<?> rawIds = (List<?>) body.get("taskIds");

            for (Object rawId : rawIds) {
                Long taskId = Long.valueOf(rawId.toString());
                try {
                    nungilUserService.addToWhiteList(guardianId, idx, taskId);
                } catch (IllegalArgumentException e) {
                    // ITEM_EXISTS면 그냥 스킵
                    if (!e.getMessage().equals("ITEM_EXISTS")) throw e;
                }
            }

            response.put("status", "SUCCESS");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 화이트리스트 조회
     * GET /api/whitelist/{guardianId}/{idx}
     */
    @GetMapping("/{guardianId}/{idx}")
    public Map<String, Object> get(@PathVariable("guardianId") String guardianId,
                                    @PathVariable("idx") int idx) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> taskIds = nungilUserService.getWhiteList(guardianId, idx);
            List<Map<String, Object>> tasks = new ArrayList<>();

            for (Long taskId : taskIds) {
                TaskVO task = taskService.findById(taskId);
                if (task != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("taskId", task.getTaskId());
                    item.put("taskName", task.getName());
                    tasks.add(item);
                }
            }

            response.put("tasks", tasks);
        } catch (Exception e) {
            response.put("tasks", Collections.emptyList());
            response.put("message", e.getMessage());
        }
        return response;
    }
}
