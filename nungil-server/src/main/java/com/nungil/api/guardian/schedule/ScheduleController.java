package com.nungil.api.guardian.schedule;

import com.nungil.domain.schedule.ScheduleService;
import com.nungil.domain.schedule.ScheduleVO;
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

    /** 일정 등록 POST /api/v1/guardian/schedules
     *  Body: { guardianId, idx, taskId, scheduledAt, location, specialNote }
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        System.out.println("[API] POST /api/v1/guardian/schedules | guardianId=" + body.get("guardianId") + ", taskId=" + body.get("taskId"));
        Map<String, Object> response = new HashMap<>();
        try {
            ScheduleVO schedule = new ScheduleVO();
            schedule.setId((String) body.get("guardianId"));
            schedule.setIdx(Integer.parseInt(body.get("idx").toString()));
            schedule.setTaskId(Long.valueOf(body.get("taskId").toString()));
            schedule.setScheduledAt(LocalDateTime.parse((String) body.get("scheduledAt")));
            schedule.setLocation((String) body.get("location"));         // SC-004
            schedule.setSpecialNote((String) body.get("specialNote"));   // SC-005

            scheduleService.create(schedule);

            System.out.println("[결과] 일정 등록 완료 scheduleId=" + schedule.getScheduleId());
            response.put("status", "SUCCESS");
            response.put("message", "일정이 등록됐어요!");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
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
        System.out.println("[API] GET /api/v1/guardian/schedules | guardianId=" + guardianId + ", idx=" + idx + ", status=" + status);
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScheduleVO> schedules = scheduleService.findByUser(guardianId, idx, status);

            System.out.println("[결과] 일정 " + schedules.size() + "개 조회");
            response.put("status", "SUCCESS");
            response.put("result", schedules);
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 일정 완료 PUT /api/v1/guardian/schedules/{scheduleId}/complete */
    @PutMapping("/{scheduleId}/complete")
    public Map<String, Object> complete(@PathVariable("scheduleId") Long scheduleId) {
        System.out.println("[API] PUT /api/v1/guardian/schedules/" + scheduleId + "/complete");
        Map<String, Object> response = new HashMap<>();
        try {
            scheduleService.complete(scheduleId);
            System.out.println("[결과] 일정 완료 처리");
            response.put("status", "SUCCESS");
            response.put("message", "일정이 완료됐어요!");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** SC-007: 드래그로 시간 변경 PUT /api/v1/guardian/schedules/{scheduleId}/time
     *  Body: { "scheduledAt": "2026-05-03T14:30" }
     */
    @PutMapping("/{scheduleId}/time")
    public Map<String, Object> updateTime(@PathVariable("scheduleId") Long scheduleId,
                                           @RequestBody Map<String, Object> body) {
        System.out.println("[API] PUT /api/v1/guardian/schedules/" + scheduleId + "/time | scheduledAt=" + body.get("scheduledAt"));
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDateTime scheduledAt = LocalDateTime.parse((String) body.get("scheduledAt"));
            scheduleService.updateScheduledAt(scheduleId, scheduledAt);
            System.out.println("[결과] 일정 시간 변경 완료");
            response.put("status", "SUCCESS");
            response.put("message", "시간이 변경됐어요!");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** 일정 삭제 DELETE /api/v1/guardian/schedules/{scheduleId} */
    @DeleteMapping("/{scheduleId}")
    public Map<String, Object> delete(@PathVariable("scheduleId") Long scheduleId) {
        System.out.println("[API] DELETE /api/v1/guardian/schedules/" + scheduleId);
        Map<String, Object> response = new HashMap<>();
        try {
            scheduleService.delete(scheduleId);
            System.out.println("[결과] 일정 삭제 완료");
            response.put("status", "SUCCESS");
            response.put("message", "일정이 삭제됐어요!");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }
}
