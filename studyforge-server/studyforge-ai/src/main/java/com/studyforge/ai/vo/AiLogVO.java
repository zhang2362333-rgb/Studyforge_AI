package com.studyforge.ai.vo;

import java.time.LocalDateTime;

public record AiLogVO(Long logId,
                      Long postId,
                      String aiType,
                      String responseText,
                      Integer success,
                      LocalDateTime createdTime) {
}
