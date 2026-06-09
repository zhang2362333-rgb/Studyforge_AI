package com.studyforge.webapi.admin;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.system.dto.IntegrationSettingUpdateRequest;
import com.studyforge.system.entity.IntegrationSetting;
import com.studyforge.system.entity.User;
import com.studyforge.system.service.AuthService;
import com.studyforge.system.service.IntegrationSettingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/settings")
public class AdminSettingsController {
    private final IntegrationSettingService integrationSettingService;
    private final AuthService authService;

    public AdminSettingsController(IntegrationSettingService integrationSettingService, AuthService authService) {
        this.integrationSettingService = integrationSettingService;
        this.authService = authService;
    }

    @GetMapping("/integrations")
    public ApiResponse<List<IntegrationSetting>> integrations(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.requireAdmin(authorization);
        return ApiResponse.success(integrationSettingService.list(true));
    }

    @PutMapping("/integrations")
    public ApiResponse<Void> save(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                  @RequestBody List<IntegrationSettingUpdateRequest> requests) {
        User admin = authService.requireUser(authorization);
        authService.requireAdmin(authorization);
        for (IntegrationSettingUpdateRequest request : requests) {
            integrationSettingService.save(request, admin.getUserId());
        }
        return ApiResponse.success(null);
    }
}
