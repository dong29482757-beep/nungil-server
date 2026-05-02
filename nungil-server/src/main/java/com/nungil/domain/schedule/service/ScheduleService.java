package com.nungil.domain.schedule.service;

import com.nungil.domain.schedule.mapper.ScheduleMapper;
import com.nungil.domain.schedule.vo.ScheduleVO;
import org.springframework.stereotype.Service;

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

    public void delete(Long scheduleId) {
        scheduleMapper.deleteById(scheduleId);
    }
}
