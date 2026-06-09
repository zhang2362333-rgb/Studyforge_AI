package com.studyforge.framework.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminInterceptorTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void preHandleRequiresAdminAuthorization() {
        AdminInterceptor interceptor = new AdminInterceptor(authService);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer admin-token");

        interceptor.preHandle(request, response, new Object());

        verify(authService).requireAdmin("Bearer admin-token");
    }

    @Test
    void preHandleRejectsForgedAdminRequest() {
        AdminInterceptor interceptor = new AdminInterceptor(authService);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer fake-admin");
        doThrow(new BizException(ErrorCode.FORBIDDEN, "admin permission is required"))
                .when(authService)
                .requireAdmin("Bearer fake-admin");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(BizException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.FORBIDDEN.getCode());
    }
}
