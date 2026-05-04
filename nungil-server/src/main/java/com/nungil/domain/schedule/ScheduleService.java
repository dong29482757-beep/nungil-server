package com.nungil.domain.schedule;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;

    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    public void create(ScheduleVO schedule) {
        scheduleMapper.insert(schedule);
    }

    public ScheduleVO findById(Long scheduleId) {
        return scheduleMapper.findById(scheduleId);
    }

    public List<ScheduleVO> findByUser(String guardianId, int idx, String status) {
        return scheduleMapper.findByUser(guardianId, idx, status);
    }

    public void complete(Long scheduleId) {
        scheduleMapper.updateSuccessAt(scheduleId);
    }

    // SC-007: 드래그로 시간 변경
    public void updateScheduledAt(Long scheduleId, LocalDateTime scheduledAt) {
        scheduleMapper.updateScheduledAt(scheduleId, scheduledAt);
    }

    public void delete(Long scheduleId) {
        scheduleMapper.deleteById(scheduleId);
    }
}
