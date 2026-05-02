package com.nungil.domain.task.mapper;

import com.nungil.domain.task.vo.TaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    // 키워드 검색
    List<TaskVO> findByKeyword(@Param("keyword") String keyword);

    // ID로 조회
    TaskVO findById(@Param("taskId") Long taskId);

    // 전체 목록
    List<TaskVO> findAll();
}
