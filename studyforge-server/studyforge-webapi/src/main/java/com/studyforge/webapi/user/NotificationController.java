package com.studyforge.webapi.user;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.system.service.AuthService;
import com.studyforge.system.service.NotificationService;
import com.studyforge.system.vo.NotificationVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final AuthService authService;
    private final NotificationService notificationService;

    public NotificationController(AuthService authService, NotificationService notificationService) {
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationVO>> list(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestParam(name = "unreadOnly", defaultValue = "false") boolean unreadOnly,
                                                  @RequestParam(name = "limit", defaultValue = "50") int limit) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(notificationService.list(userId, unreadOnly, limit));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> unreadCount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(notificationService.unreadCount(userId));
    }

    @PostMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                      @PathVariable("notificationId") Long notificationId) {
        Long userId = authService.requireUserId(authorization);
        notificationService.markRead(userId, notificationId);
        return ApiResponse.success(null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        Long userId = authService.requireUserId(authorization);
        notificationService.markAllRead(userId);
        return ApiResponse.success(null);
    }
}
