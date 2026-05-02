package com.nungil.domain.user.mapper;

import com.nungil.domain.user.vo.NungilUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NungilUserMapper {

    // 등록 (idx는 보호자당 최대값+1 자동 계산)
    void insert(NungilUserVO user);

    // 보호자의 다음 idx 조회
    int getNextIdx(@Param("id") String id);

    // 보호자 id로 전체 사용자 목록 조회
    List<NungilUserVO> findByGuardianId(@Param("id") String id);

    // 단일 사용자 조회
    NungilUserVO findByIdAndIdx(@Param("id") String id, @Param("idx") int idx);

    // 화이트리스트 업데이트
    void updateWhiteList(@Param("id") String id, @Param("idx") int idx,
                         @Param("whiteList") String whiteList);

    // 특이사항 업데이트
    void updateSpecialNote(@Param("id") String id, @Param("idx") int idx,
                           @Param("specialNote") String specialNote);
}
