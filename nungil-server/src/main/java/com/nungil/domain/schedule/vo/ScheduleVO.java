package com.nungil.domain.schedule.vo;

import java.time.LocalDateTime;

public class ScheduleVO {

    private Long scheduleId;
    private Long taskId;
    private String id;       // 보호자 id
    private int idx;         // 사용자 순번
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime successAt;

    // JOIN 조회용
    private String taskName;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getIdx() { return idx; }
    public void setIdx(int idx) { this.idx = idx; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSuccessAt() { return successAt; }
    public void setSuccessAt(LocalDateTime successAt) { this.successAt = successAt; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
}
