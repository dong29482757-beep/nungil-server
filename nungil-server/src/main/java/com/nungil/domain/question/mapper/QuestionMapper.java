package com.nungil.domain.question.mapper;

import com.nungil.domain.question.vo.QuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper {

    void insert(QuestionVO question);

    List<QuestionVO> findByUser(@Param("id") String id, @Param("idx") int idx);

    void updateStatus(@Param("questionId") Long questionId, @Param("status") String status);
}
