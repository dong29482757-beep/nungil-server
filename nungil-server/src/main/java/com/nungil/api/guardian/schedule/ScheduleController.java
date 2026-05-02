package com.nungil.api.guardian.schedule;

import com.nungil.domain.schedule.service.ScheduleService;
import com.nungil.domain.schedule.vo.ScheduleVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/guardian/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /** 일정 등록 POST /api/v1/guardian/schedules */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            ScheduleVO schedule = new ScheduleVO();
            schedule.setId((String) body.get("guardianId"));
            schedule.setIdx(Integer.parseInt(body.get("idx").toString()));
            schedule.setTaskId(Long.valueOf(body.get("taskId").toString()));
            schedule.setScheduledAt(LocalDateTime.parse((String) body.get("scheduledAt")));

            scheduleService.create(schedule);

            response.put("status", "SUCCESS");
            response.put("message", "일정이 등록됐어요!");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 일정 목록 조회 GET /api/v1/guardian/schedules?guardianId=xxx&idx=1&status=pending */
    @GetMapping
    public Map<String, Object> getList(@RequestParam("guardianId") String guardianId,
                                        @RequestParam("idx") int idx,
                                        @RequestParam(value = "status", required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScheduleVO> schedules = scheduleService.findByUser(guardianId, idx, status);

            response.put("status", "SUCCESS");
            response.put("result", schedules);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 일정 완료 PUT /api/v1/guardian/schedules/{scheduleId}/complete */
    @PutMapping("/{scheduleId}/complete")
    public Map<String, Object> complete(@PathVariable("scheduleId") Long scheduleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            scheduleService.complete(scheduleId);
            response.put("status", "SUCCESS");
            response.put("message", "일정이 완료됐어요!");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 일정 삭제 DELETE /api/v1/guardian/schedules/{scheduleId} */
    @DeleteMapping("/{scheduleId}")
    public Map<String, Object> delete(@PathVariable("scheduleId") Long scheduleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            scheduleService.delete(scheduleId);
            response.put("status", "SUCCESS");
            response.put("message", "일정이 삭제됐어요!");
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }
}
