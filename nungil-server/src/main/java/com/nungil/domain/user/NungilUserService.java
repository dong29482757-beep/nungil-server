package com.nungil.domain.user;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NungilUserService {

    private final NungilUserMapper nungilUserMapper;

    public NungilUserService(NungilUserMapper nungilUserMapper) {
        this.nungilUserMapper = nungilUserMapper;
    }

    /** 사용자 등록 (idx 자동 계산) */
    public NungilUserVO createUser(String guardianId) {
        int nextIdx = nungilUserMapper.getNextIdx(guardianId);
        NungilUserVO user = new NungilUserVO();
        user.setId(guardianId);
        user.setIdx(nextIdx);
        nungilUserMapper.insert(user);
        return user;
    }

    /** 보호자의 사용자 목록 */
    public List<NungilUserVO> getUsersByGuardian(String guardianId) {
        return nungilUserMapper.findByGuardianId(guardianId);
    }

    /** 단일 사용자 조회 */
    public NungilUserVO getUser(String guardianId, int idx) {
        return nungilUserMapper.findByIdAndIdx(guardianId, idx);
    }

    /** 화이트리스트 추가 */
    public List<Long> addToWhiteList(String guardianId, int idx, Long taskId) {
        List<Long> taskIds = nungilUserMapper.findWhitelistTaskIds(guardianId, idx);
        if ((taskIds == null || taskIds.isEmpty()) && nungilUserMapper.findByIdAndIdx(guardianId, idx) != null) {
            taskIds = parseWhiteList(nungilUserMapper.findByIdAndIdx(guardianId, idx).getWhiteList());
            for (Long id : taskIds) {
                if (nungilUserMapper.existsWhitelistTask(guardianId, idx, id) == 0) {
                    nungilUserMapper.insertWhitelistTask(guardianId, idx, id);
                }
            }
            nungilUserMapper.updateWhiteList(guardianId, idx, null);
            taskIds = nungilUserMapper.findWhitelistTaskIds(guardianId, idx);
        }

        if (nungilUserMapper.existsWhitelistTask(guardianId, idx, taskId) > 0) throw new IllegalArgumentException("ITEM_EXISTS");
        if (taskIds.size() >= 4)     throw new IllegalArgumentException("MAX_ITEM_EXCEEDED");

        nungilUserMapper.insertWhitelistTask(guardianId, idx, taskId);
        return nungilUserMapper.findWhitelistTaskIds(guardianId, idx);
    }

    /** 화이트리스트 삭제 */
    public List<Long> removeFromWhiteList(String guardianId, int idx, Long taskId) {
        nungilUserMapper.deleteWhitelistTask(guardianId, idx, taskId);
        return nungilUserMapper.findWhitelistTaskIds(guardianId, idx);
    }

    /** 화이트리스트 조회 */
    public List<Long> getWhiteList(String guardianId, int idx) {
        List<Long> taskIds = nungilUserMapper.findWhitelistTaskIds(guardianId, idx);
        if (taskIds != null && !taskIds.isEmpty()) return taskIds;

        NungilUserVO user = nungilUserMapper.findByIdAndIdx(guardianId, idx);
        return user != null ? parseWhiteList(user.getWhiteList()) : new ArrayList<>();
    }

    /** 특이사항 저장 */
    public void updateSpecialNote(String guardianId, int idx, String specialNote) {
        nungilUserMapper.updateSpecialNote(guardianId, idx, specialNote);
    }

    private List<Long> parseWhiteList(String whiteList) {
        List<Long> result = new ArrayList<>();
        if (whiteList == null || whiteList.trim().isEmpty()) return result;
        for (String s : whiteList.split(",")) {
            try { result.add(Long.parseLong(s.trim())); } catch (Exception ignored) {}
        }
        return result;
    }

}
