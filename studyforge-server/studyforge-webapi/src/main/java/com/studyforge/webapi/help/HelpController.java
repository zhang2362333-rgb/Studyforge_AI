package com.studyforge.webapi.help;

import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.help.dto.CreateHelpAnswerRequest;
import com.studyforge.help.dto.CreateHelpRequest;
import com.studyforge.help.service.HelpRequestService;
import com.studyforge.help.vo.HelpAnswerVO;
import com.studyforge.help.vo.HelpRequestVO;
import com.studyforge.system.service.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/help")
public class HelpController {
    private final HelpRequestService helpRequestService;
    private final AuthService authService;

    public HelpController(HelpRequestService helpRequestService, AuthService authService) {
        this.helpRequestService = helpRequestService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<HelpRequestVO>> list(@RequestParam(name = "status", required = false) String status,
                                                 @RequestParam(name = "limit", defaultValue = "30") int limit) {
        return ApiResponse.success(helpRequestService.list(status, limit));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @RequestBody CreateHelpRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("created", helpRequestService.create(userId, request));
    }

    @GetMapping("/{helpId}")
    public ApiResponse<HelpRequestVO> detail(@PathVariable("helpId") Long helpId) {
        return ApiResponse.success(helpRequestService.detail(helpId));
    }

    @GetMapping("/{helpId}/answers")
    public ApiResponse<List<HelpAnswerVO>> answers(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                                   @PathVariable("helpId") Long helpId) {
        Long viewerId = authService.currentUserId(authorization);
        return ApiResponse.success(helpRequestService.answers(helpId, viewerId));
    }

    @PostMapping("/{helpId}/answers")
    public ApiResponse<HelpAnswerVO> answer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                            @PathVariable("helpId") Long helpId,
                                            @RequestBody CreateHelpAnswerRequest request) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success("answered", helpRequestService.answer(helpId, userId, request));
    }

    @PostMapping("/{helpId}/answers/{answerId}/likes")
    public ApiResponse<HelpAnswerVO> likeAnswer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                @PathVariable("helpId") Long helpId,
                                                @PathVariable("answerId") Long answerId) {
        Long userId = authService.requireUserId(authorization);
        return ApiResponse.success(helpRequestService.likeAnswer(helpId, answerId, userId));
    }

    @DeleteMapping("/{helpId}/answers/{answerId}")
    public ApiResponse<Void> deleteAnswer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                          @PathVariable("helpId") Long helpId,
                                          @PathVariable("answerId") Long answerId) {
        Long userId = authService.requireUserId(authorization);
        helpRequestService.deleteAnswer(helpId, answerId, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{helpId}/answers/{answerId}/accept")
    public ApiResponse<Void> accept(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                    @PathVariable("helpId") Long helpId,
                                    @PathVariable("answerId") Long answerId) {
        Long userId = authService.requireUserId(authorization);
        helpRequestService.accept(helpId, answerId, userId);
        return ApiResponse.success(null);
    }
}
