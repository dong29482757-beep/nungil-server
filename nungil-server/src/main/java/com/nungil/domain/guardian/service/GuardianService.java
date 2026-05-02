package com.nungil.domain.guardian.service;

import com.nungil.domain.guardian.mapper.GuardianMapper;
import com.nungil.domain.guardian.vo.GuardianVO;
import org.springframework.stereotype.Service;

@Service
public class GuardianService {

    private final GuardianMapper guardianMapper;

    public GuardianService(GuardianMapper guardianMapper) {
        this.guardianMapper = guardianMapper;
    }

    public void join(GuardianVO guardian) {
        if (guardianMapper.findById(guardian.getId()) != null) {
            throw new IllegalArgumentException("ID_EXISTS");
        }
        guardianMapper.insert(guardian);
    }

    public GuardianVO login(String id, String pw) {
        GuardianVO guardian = guardianMapper.findById(id);
        if (guardian == null || !guardian.getPw().equals(pw)) {
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }
        return guardian;
    }

    public GuardianVO findById(String id) {
        return guardianMapper.findById(id);
    }

    public void updateFcmToken(String id, String fcmToken) {
        guardianMapper.updateFcmToken(id, fcmToken);
    }
}
