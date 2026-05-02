package com.nungil.domain.guardian.mapper;

import com.nungil.domain.guardian.vo.GuardianVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GuardianMapper {

    void insert(GuardianVO guardian);

    GuardianVO findById(@Param("id") String id);

    GuardianVO findByEmail(@Param("email") String email);

    void updateFcmToken(@Param("id") String id, @Param("fcmToken") String fcmToken);
}
