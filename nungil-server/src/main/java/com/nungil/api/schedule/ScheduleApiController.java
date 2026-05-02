package com.nungil.api.schedule;

import com.nungil.domain.schedule.service.ScheduleService;
import com.nungil.domain.schedule.vo.ScheduleVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    public ScheduleApiController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 일정 등록
     * POST /api/schedule
     * {
     *   "guardianId": "hong123",
     *   "idx": 1,
     *   "taskId": 1,
     *   "date": "2026-04-20",
     *   "time": "14:00"
     * }
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String guardianId  = (String) body.get("guardianId");
            int    idx         = Integer.parseInt(body.get("idx").toString());
            Long   taskId      = Long.valueOf(body.get("taskId").toString());
            String date        = (String) body.get("date");
            String time        = (String) body.get("time");

            // date + time → LocalDateTime
            LocalDateTime scheduledAt = LocalDateTime.parse(
                date + "T" + time,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
            );

            // 중복 일정 확인
            List<ScheduleVO> existing = scheduleService.findByUser(guardianId, idx, "pending");
            boolean conflict = existing.stream()
                .anyMatch(s -> s.getScheduledAt() != null && s.getScheduledAt().equals(scheduledAt));

            if (conflict) {
                response.put("status", "CONFLICT");
                response.put("conflict", true);
                response.put("message", "같은 시간에 이미 일정이 있어요");
                return response;
            }

            ScheduleVO schedule = new ScheduleVO();
            schedule.setId(guardianId);
            schedule.setIdx(idx);
            schedule.setTaskId(taskId);
            schedule.setScheduledAt(scheduledAt);

            scheduleService.create(schedule);

            response.put("status", "SUCCESS");
            response.put("scheduleId", schedule.getScheduleId());
            response.put("conflict", false);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 일정 목록 조회 (쿼리 파라미터)
     * GET /api/schedule?guardianId=hong123&idx=1&status=pending
     */
    @GetMapping
    public Map<String, Object> getList(@RequestParam("guardianId") String guardianId,
                                        @RequestParam("idx") int idx,
                                        @RequestParam(value = "status", required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScheduleVO> schedules = scheduleService.findByUser(guardianId, idx, status);
            response.put("status", "SUCCESS");
            response.put("schedules", schedules);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 일정 목록 조회 (경로 파라미터)
     * GET /api/schedule/{guardianId}/{idx}
     * GET /api/schedule/{guardianId}/{idx}?status=pending
     */
    @GetMapping("/{guardianId}/{idx}")
    public Map<String, Object> getListByPath(@PathVariable("guardianId") String guardianId,
                                              @PathVariable("idx") int idx,
                                              @RequestParam(value = "status", required = false) String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ScheduleVO> schedules = scheduleService.findByUser(guardianId, idx, status);
            response.put("status", "SUCCESS");
            response.put("schedules", schedules);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }
}
