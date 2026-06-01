package com.studyforge.webapi.ai;

import com.studyforge.ai.entity.AiLog;
import com.studyforge.ai.mapper.AiLogMapper;
import com.studyforge.ai.service.AiService;
import com.studyforge.ai.service.AiService.GeneratedCover;
import com.studyforge.ai.vo.AiLogVO;
import com.studyforge.ai.vo.AiResultVO;
import com.studyforge.common.api.ApiResponse;
import com.studyforge.common.constants.HttpHeaders;
import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.content.service.PostQueryService;
import com.studyforge.content.vo.PostDetailVO;
import com.studyforge.system.service.AuthService;
import com.studyforge.system.service.UploadedFileService;
import com.studyforge.webapi.support.UploadStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiService aiService;
    private final AiLogMapper aiLogMapper;
    private final PostQueryService postQueryService;
    private final AuthService authService;
    private final UploadedFileService uploadedFileService;
    private final Path imageRoot;

    public AiController(AiService aiService,
                        AiLogMapper aiLogMapper,
                        PostQueryService postQueryService,
                        AuthService authService,
                        UploadedFileService uploadedFileService) {
        this.aiService = aiService;
        this.aiLogMapper = aiLogMapper;
        this.postQueryService = postQueryService;
        this.authService = authService;
        this.uploadedFileService = uploadedFileService;
        this.imageRoot = UploadStorage.imageRoot();
    }

    @PostMapping("/posts/{postId}/summary")
    public ApiResponse<AiResultVO> summary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                           @PathVariable("postId") Long postId,
                                           @RequestBody AiLanguageRequest request) {
        Long userId = authService.requireUserId(authorization);
        String contentLanguage = contentLanguage(request);
        String promptLanguage = promptLanguage(request);
        PostDetailVO post = postQueryService.getDetail(postId, contentLanguage);
        String text = aiService.generateSummary(post.content(), promptLanguage);
        log(userId, postId, "SUMMARY", post.content(), text, 1);
        return ApiResponse.success(new AiResultVO("SUMMARY", promptLanguage, text));
    }

    @PostMapping("/posts/{postId}/review-cards")
    public ApiResponse<AiResultVO> reviewCards(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                               @PathVariable("postId") Long postId,
                                               @RequestBody AiLanguageRequest request) {
        Long userId = authService.requireUserId(authorization);
        String contentLanguage = contentLanguage(request);
        String promptLanguage = promptLanguage(request);
        PostDetailVO post = postQueryService.getDetail(postId, contentLanguage);
        String text = aiService.generateQuiz(post.content(), promptLanguage);
        log(userId, postId, "REVIEW_CARD", post.content(), text, 1);
        return ApiResponse.success(new AiResultVO("REVIEW_CARD", promptLanguage, text));
    }

    @PostMapping("/posts/{postId}/questions")
    public ApiResponse<AiResultVO> answer(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                          @PathVariable("postId") Long postId,
                                          @RequestBody AiQuestionRequest request) {
        Long userId = authService.requireUserId(authorization);
        String contentLanguage = request == null || isBlank(request.contentLanguageCode()) ? answerLanguage(request) : request.contentLanguageCode();
        String promptLanguage = request == null || isBlank(request.promptLanguageCode()) ? answerLanguage(request) : request.promptLanguageCode();
        PostDetailVO post = postQueryService.getDetail(postId, contentLanguage);
        String text = aiService.answerQuestion(post.content(), request == null ? "" : request.question(), promptLanguage);
        log(userId, postId, "QUESTION", request == null ? "" : request.question(), text, 1);
        return ApiResponse.success(new AiResultVO("QUESTION", promptLanguage, text));
    }

    @PostMapping("/markdown/format")
    public ApiResponse<AiResultVO> formatMarkdown(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                  @RequestBody AiMarkdownFormatRequest request) {
        Long userId = authService.requireUserId(authorization);
        if (request == null || isBlank(request.content())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "content is required");
        }
        String promptLanguage = isBlank(request.promptLanguageCode())
                ? (isBlank(request.languageCode()) ? "zh_CN" : request.languageCode())
                : request.promptLanguageCode();
        String source = request.content().trim();
        if (source.length() > 12000) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "content is too long");
        }
        String text = aiService.formatMarkdown(source, promptLanguage);
        log(userId, null, "MARKDOWN_FORMAT", source, text, 1);
        return ApiResponse.success(new AiResultVO("MARKDOWN_FORMAT", promptLanguage, text));
    }

    @PostMapping("/covers/generate")
    public ApiResponse<AiCoverResultVO> generateCover(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                      @RequestBody AiCoverRequest request) throws IOException {
        Long userId = authService.requireUserId(authorization);
        if (request == null || (isBlank(request.title()) && isBlank(request.summary()) && isBlank(request.content()))) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "title, summary or content is required");
        }

        String title = trim(request.title(), 300);
        String summary = trim(request.summary(), 800);
        String content = trim(request.content(), 12000);
        String language = isBlank(request.languageCode()) ? "zh_CN" : request.languageCode();
        GeneratedCover cover = aiService.generateCover(title, summary, content, language);
        if (cover == null || isBlank(cover.imageDataBase64())) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "cover generation failed");
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(cover.imageDataBase64());
        } catch (IllegalArgumentException exception) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "image data is invalid");
        }
        if (imageBytes.length == 0 || imageBytes.length > 12L * 1024L * 1024L) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "generated image size is invalid");
        }

        Files.createDirectories(imageRoot);
        String filename = UUID.randomUUID() + ".png";
        Path target = imageRoot.resolve(filename).normalize();
        if (!target.startsWith(imageRoot)) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "invalid file path");
        }
        Files.write(target, imageBytes);

        String url = "/api/v1/files/images/" + filename;
        uploadedFileService.recordImage(userId, "ai-generated-cover.png", filename, url, "image/png", imageBytes.length);
        log(userId, null, "COVER_IMAGE", cover.prompt(), cover.visualBrief() + "\n" + url, 1);
        return ApiResponse.success(new AiCoverResultVO(url, cover.visualBrief()));
    }

    @GetMapping("/me/review-cards")
    public ApiResponse<List<AiLogVO>> myReviewCards(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                                    @RequestParam(name = "limit", defaultValue = "20") int limit) {
        Long userId = authService.requireUserId(authorization);
        int normalizedLimit = limit <= 0 ? 20 : Math.min(limit, 50);
        List<AiLogVO> cards = aiLogMapper.selectByUserAndType(userId, "REVIEW_CARD", normalizedLimit)
                .stream()
                .map(log -> new AiLogVO(log.getLogId(), log.getPostId(), log.getAiType(), log.getResponseText(), log.getSuccess(), log.getCreatedTime()))
                .toList();
        return ApiResponse.success(cards);
    }

    private void log(Long userId, Long postId, String aiType, String requestText, String responseText, int success) {
        AiLog log = new AiLog();
        log.setUserId(userId);
        log.setPostId(postId);
        log.setAiType(aiType);
        log.setRequestText(trim(requestText, 4000));
        log.setResponseText(responseText);
        log.setSuccess(success);
        aiLogMapper.insert(log);
    }

    private String contentLanguage(AiLanguageRequest request) {
        if (request != null && !isBlank(request.contentLanguageCode())) {
            return request.contentLanguageCode();
        }
        return request == null || isBlank(request.languageCode()) ? "zh_CN" : request.languageCode();
    }

    private String promptLanguage(AiLanguageRequest request) {
        if (request != null && !isBlank(request.promptLanguageCode())) {
            return request.promptLanguageCode();
        }
        return request == null || isBlank(request.languageCode()) ? "zh_CN" : request.languageCode();
    }

    private String answerLanguage(AiQuestionRequest request) {
        return request == null || isBlank(request.answerLanguage()) ? "zh_CN" : request.answerLanguage();
    }

    private String trim(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record AiLanguageRequest(String languageCode, String contentLanguageCode, String promptLanguageCode) {
    }

    public record AiQuestionRequest(String question, String answerLanguage, String contentLanguageCode, String promptLanguageCode) {
    }

    public record AiMarkdownFormatRequest(String content,
                                          String languageCode,
                                          String contentLanguageCode,
                                          String promptLanguageCode) {
    }

    public record AiCoverRequest(String title,
                                 String summary,
                                 String content,
                                 String languageCode,
                                 String promptLanguageCode) {
    }

    public record AiCoverResultVO(String coverImageUrl, String visualBrief) {
    }
}
