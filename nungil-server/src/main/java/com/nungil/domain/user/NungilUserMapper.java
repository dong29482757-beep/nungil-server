package com.nungil.domain.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NungilUserMapper {

    void insert(NungilUserVO user);

    int getNextIdx(@Param("id") String id);

    List<NungilUserVO> findByGuardianId(@Param("id") String id);

    NungilUserVO findByIdAndIdx(@Param("id") String id, @Param("idx") int idx);

    void updateWhiteList(@Param("id") String id, @Param("idx") int idx,
                         @Param("whiteList") String whiteList);

    void updateSpecialNote(@Param("id") String id, @Param("idx") int idx,
                           @Param("specialNote") String specialNote);

    List<Long> findWhitelistTaskIds(@Param("id") String id, @Param("idx") int idx);

    int existsWhitelistTask(@Param("id") String id, @Param("idx") int idx,
                            @Param("taskId") Long taskId);

    void insertWhitelistTask(@Param("id") String id, @Param("idx") int idx,
                             @Param("taskId") Long taskId);

    void deleteWhitelistTask(@Param("id") String id, @Param("idx") int idx,
                             @Param("taskId") Long taskId);
}
