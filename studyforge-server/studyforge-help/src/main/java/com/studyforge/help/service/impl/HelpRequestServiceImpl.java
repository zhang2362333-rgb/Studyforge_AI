package com.studyforge.help.service.impl;

import com.studyforge.common.exception.BizException;
import com.studyforge.common.exception.ErrorCode;
import com.studyforge.help.dto.CreateHelpAnswerRequest;
import com.studyforge.help.dto.CreateHelpRequest;
import com.studyforge.help.entity.HelpAnswer;
import com.studyforge.help.entity.HelpRequest;
import com.studyforge.help.mapper.HelpAnswerMapper;
import com.studyforge.help.mapper.HelpRequestMapper;
import com.studyforge.help.service.HelpRequestService;
import com.studyforge.help.vo.HelpAnswerVO;
import com.studyforge.help.vo.HelpRequestVO;
import com.studyforge.system.entity.User;
import com.studyforge.system.mapper.UserMapper;
import com.studyforge.system.service.NotificationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelpRequestServiceImpl implements HelpRequestService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<![\\p{L}\\p{N}_])@([\\p{L}\\p{N}_\\-.]{2,32})");

    private final HelpRequestMapper helpRequestMapper;
    private final HelpAnswerMapper helpAnswerMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public HelpRequestServiceImpl(HelpRequestMapper helpRequestMapper,
                                  HelpAnswerMapper helpAnswerMapper,
                                  UserMapper userMapper,
                                  NotificationService notificationService) {
        this.helpRequestMapper = helpRequestMapper;
        this.helpAnswerMapper = helpAnswerMapper;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    @Override
    public Long create(Long userId, String title, String description) {
        return create(userId, new CreateHelpRequest(title, description, null, 0));
    }

    @Override
    @Transactional
    public Long create(Long userId, CreateHelpRequest request) {
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (request == null || isBlank(request.title()) || isBlank(request.description())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "title and description are required");
        }
        HelpRequest help = new HelpRequest();
        help.setUserId(userId);
        help.setTitle(request.title().trim());
        help.setDescription(request.description().trim());
        help.setCategoryId(request.categoryId());
        help.setStatus("OPEN");
        help.setRewardPoints(request.rewardPoints() == null ? 0 : Math.max(0, request.rewardPoints()));
        helpRequestMapper.insert(help);
        return help.getHelpId();
    }

    @Override
    public List<HelpRequestVO> list(String status, int limit) {
        int normalizedLimit = limit <= 0 ? 30 : Math.min(limit, 80);
        return helpRequestMapper.selectList(isBlank(status) || "ALL".equals(status) ? null : status, normalizedLimit)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public HelpRequestVO detail(Long helpId) {
        return toVO(requireHelp(helpId));
    }

    @Override
    @Transactional
    public HelpAnswerVO answer(Long helpId, Long userId, CreateHelpAnswerRequest request) {
        HelpRequest help = requireHelp(helpId);
        if (request == null || isBlank(request.content())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "answer content is required");
        }
        HelpAnswer parent = null;
        if (request.parentAnswerId() != null) {
            parent = helpAnswerMapper.selectById(request.parentAnswerId());
            if (parent == null || !helpId.equals(parent.getHelpId()) || !"VISIBLE".equals(parent.getStatus())) {
                throw new BizException(ErrorCode.NOT_FOUND, "parent answer not found");
            }
        }
        HelpAnswer answer = new HelpAnswer();
        answer.setHelpId(helpId);
        answer.setParentAnswerId(parent == null ? null : parent.getAnswerId());
        answer.setUserId(userId);
        answer.setContent(request.content().trim());
        answer.setAccepted(0);
        answer.setStatus("VISIBLE");
        answer.setFloorNo(helpAnswerMapper.nextFloorNo(helpId));
        answer.setLikeCount(0);
        helpAnswerMapper.insert(answer);
        Set<Long> notifiedRecipients = new HashSet<>();
        if (parent == null) {
            notifyHelpAnswered(notifiedRecipients, help.getUserId(), userId, helpId, answer.getAnswerId(), help.getTitle(), answer.getContent());
        } else {
            notifyHelpAnswerReplied(notifiedRecipients, parent.getUserId(), userId, helpId, answer.getAnswerId(), help.getTitle(), answer.getContent());
        }
        notifyHelpMentions(notifiedRecipients, userId, helpId, answer.getAnswerId(), help.getTitle(), answer.getContent());
        HelpAnswer created = helpAnswerMapper.selectByIdForViewer(answer.getAnswerId(), userId);
        return toVO(created == null ? answer : created, help, userId);
    }

    @Override
    public List<HelpAnswerVO> answers(Long helpId, Long viewerId) {
        HelpRequest help = requireHelp(helpId);
        return helpAnswerMapper.selectByHelpId(helpId, viewerId)
                .stream()
                .map(answer -> toVO(answer, help, viewerId))
                .toList();
    }

    @Override
    @Transactional
    public HelpAnswerVO likeAnswer(Long helpId, Long answerId, Long userId) {
        HelpRequest help = requireHelp(helpId);
        HelpAnswer answer = requireAnswer(helpId, answerId);
        if (!"VISIBLE".equals(answer.getStatus())) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "deleted answer cannot be liked");
        }
        if (helpAnswerMapper.countLike(answerId, userId) > 0) {
            helpAnswerMapper.deleteLike(answerId, userId);
            helpAnswerMapper.incrementLikeCount(answerId, -1);
        } else if (helpAnswerMapper.insertLike(answerId, userId) > 0) {
            helpAnswerMapper.incrementLikeCount(answerId, 1);
            notificationService.notifyHelpAnswerLiked(answer.getUserId(), userId, helpId, answerId, help.getTitle());
        }
        return toVO(helpAnswerMapper.selectByIdForViewer(answerId, userId), help, userId);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long helpId, Long answerId, Long userId) {
        HelpRequest help = requireHelp(helpId);
        HelpAnswer answer = requireAnswer(helpId, answerId);
        if (!userId.equals(answer.getUserId()) && !userId.equals(help.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "only the answer author or question owner can delete this answer");
        }
        helpAnswerMapper.markDeleted(answerId);
    }

    @Override
    @Transactional
    public void accept(Long helpId, Long answerId, Long userId) {
        HelpRequest help = requireHelp(helpId);
        if (!help.getUserId().equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "only the question owner can accept an answer");
        }
        HelpAnswer answer = helpAnswerMapper.selectById(answerId);
        if (answer == null || !helpId.equals(answer.getHelpId()) || !"VISIBLE".equals(answer.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "answer not found");
        }
        helpAnswerMapper.clearAccepted(helpId);
        helpAnswerMapper.accept(answerId);
        helpRequestMapper.updateStatus(helpId, "RESOLVED");
    }

    private HelpRequest requireHelp(Long helpId) {
        HelpRequest help = helpRequestMapper.selectById(helpId);
        if (help == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "help request not found");
        }
        return help;
    }

    private HelpAnswer requireAnswer(Long helpId, Long answerId) {
        if (answerId == null) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "answerId is required");
        }
        HelpAnswer answer = helpAnswerMapper.selectById(answerId);
        if (answer == null || !helpId.equals(answer.getHelpId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "answer not found");
        }
        return answer;
    }

    private HelpRequestVO toVO(HelpRequest help) {
        return new HelpRequestVO(
                help.getHelpId(),
                help.getUserId(),
                help.getTitle(),
                help.getDescription(),
                help.getCategoryId(),
                help.getStatus(),
                help.getRewardPoints(),
                help.getCreatedTime()
        );
    }

    private HelpAnswerVO toVO(HelpAnswer answer, HelpRequest help, Long viewerId) {
        boolean deleted = "DELETED".equals(answer.getStatus());
        boolean canDelete = viewerId != null
                && !deleted
                && (viewerId.equals(answer.getUserId()) || viewerId.equals(help.getUserId()));
        return new HelpAnswerVO(
                answer.getAnswerId(),
                answer.getHelpId(),
                answer.getParentAnswerId(),
                answer.getUserId(),
                fallback(answer.getAuthorUsername(), "#" + answer.getUserId()),
                fallback(answer.getAuthorName(), fallback(answer.getAuthorUsername(), "#" + answer.getUserId())),
                answer.getAuthorAvatarUrl(),
                answer.getParentUserId(),
                answer.getParentAuthorUsername(),
                answer.getParentAuthorName(),
                deleted ? "这条回答已删除" : answer.getContent(),
                answer.getAccepted(),
                answer.getStatus(),
                safeInt(answer.getFloorNo()),
                safeInt(answer.getLikeCount()),
                Boolean.TRUE.equals(answer.getLikedByViewer()),
                canDelete,
                deleted,
                answer.getCreatedTime(),
                answer.getUpdatedTime()
        );
    }

    private void notifyHelpMentions(Set<Long> notifiedRecipients,
                                    Long actorId,
                                    Long helpId,
                                    Long answerId,
                                    String helpTitle,
                                    String content) {
        for (Long recipientId : mentionedUserIds(content)) {
            if (notifiedRecipients.add(recipientId)) {
                notificationService.notifyHelpAnswerMentioned(recipientId, actorId, helpId, answerId, helpTitle, content);
            }
        }
    }

    private void notifyHelpAnswered(Set<Long> notifiedRecipients,
                                    Long recipientId,
                                    Long actorId,
                                    Long helpId,
                                    Long answerId,
                                    String helpTitle,
                                    String content) {
        if (notifiedRecipients.add(recipientId)) {
            notificationService.notifyHelpAnswered(recipientId, actorId, helpId, answerId, helpTitle, content);
        }
    }

    private void notifyHelpAnswerReplied(Set<Long> notifiedRecipients,
                                         Long recipientId,
                                         Long actorId,
                                         Long helpId,
                                         Long answerId,
                                         String helpTitle,
                                         String content) {
        if (notifiedRecipients.add(recipientId)) {
            notificationService.notifyHelpAnswerReplied(recipientId, actorId, helpId, answerId, helpTitle, content);
        }
    }

    private Set<Long> mentionedUserIds(String content) {
        Set<String> handles = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            handles.add(matcher.group(1).toLowerCase());
        }
        if (handles.isEmpty()) {
            return Set.of();
        }
        List<User> users = userMapper.selectMentionCandidates(new ArrayList<>(handles));
        Set<Long> userIds = new LinkedHashSet<>();
        for (User user : users) {
            userIds.add(user.getUserId());
        }
        return userIds;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
