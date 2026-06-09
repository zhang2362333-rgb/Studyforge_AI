package com.studyforge.system.service;

import com.studyforge.system.dto.LoginRequest;
import com.studyforge.system.dto.RegisterRequest;
import com.studyforge.system.entity.User;
import com.studyforge.system.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginRequest request);

    Long register(RegisterRequest request);

    void logout(String accessToken);

    User requireUser(String authorization);

    Long currentUserId(String authorization);

    Long requireUserId(String authorization);

    void requireAdmin(String authorization);
}
