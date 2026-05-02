package com.nungil.domain.schedule.mapper;

import com.nungil.domain.schedule.vo.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleMapper {

    void insert(ScheduleVO schedule);

    ScheduleVO findById(@Param("scheduleId") Long scheduleId);

    List<ScheduleVO> findByUser(@Param("id") String id, @Param("idx") int idx,
                                 @Param("status") String status);

    void updateStatus(@Param("scheduleId") Long scheduleId, @Param("status") String status);

    void updateSuccessAt(@Param("scheduleId") Long scheduleId);

    void deleteById(@Param("scheduleId") Long scheduleId);
}
