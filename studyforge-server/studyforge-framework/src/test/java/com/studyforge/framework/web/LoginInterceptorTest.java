package com.studyforge.framework.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.common.enums.RoleType;
import com.studyforge.common.enums.UserStatus;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.entity.User;
import com.studyforge.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginInterceptorTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void preHandleRequiresRealAuthenticatedUser() {
        LoginInterceptor interceptor = new LoginInterceptor(authService);
        User user = new User();
        user.setUserId(7L);
        user.setRole(RoleType.USER);
        user.setStatus(UserStatus.ACTIVE);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer token-1");
        when(authService.requireUser("Bearer token-1")).thenReturn(user);

        interceptor.preHandle(request, response, new Object());

        verify(authService).requireUser("Bearer token-1");
    }

    @Test
    void preHandleRejectsInvalidToken() {
        LoginInterceptor interceptor = new LoginInterceptor(authService);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer fake-token");
        when(authService.requireUser("Bearer fake-token"))
                .thenThrow(new BizException(ErrorCode.UNAUTHORIZED, "login has expired"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.UNAUTHORIZED.getCode());
    }
}
